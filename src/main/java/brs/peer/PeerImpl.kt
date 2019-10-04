package brs.peer

import brs.*
import brs.props.Props
import brs.util.*
import brs.util.convert.emptyToNull
import brs.util.convert.truncate
import brs.util.delegates.Atomic
import brs.util.delegates.AtomicWithOverride
import brs.util.logging.safeDebug
import brs.util.logging.safeError
import brs.util.logging.safeInfo
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.io.*
import java.net.*
import java.nio.charset.StandardCharsets
import java.sql.SQLException
import java.util.zip.GZIPInputStream

internal class PeerImpl(private val dp: DependencyProvider, override val peerAddress: String, announcedAddress: String?) : Peer {
    override var announcedAddress by AtomicWithOverride<String?>(setValueDelegate = { announcedAddress, set ->
        val announcedPeerAddress = dp.peers.normalizeHostAndPort(announcedAddress)
        if (announcedPeerAddress != null) {
            set(announcedPeerAddress)
            try {
                this.port = URL(Constants.HTTP + announcedPeerAddress).port
            } catch (ignored: MalformedURLException) {
            }
        }
    })
    override var port by Atomic<Int>()
    override var shareAddress by Atomic(true)
    override var platform by Atomic<String>()
    override var application by Atomic<String>()
    override var version by Atomic<Version>(Version.EMPTY)
    private var isOldVersion by Atomic(false)
    private var blacklistingTime by Atomic<Long>(0)
    override var state: Peer.State by AtomicWithOverride(initialValue = Peer.State.NON_CONNECTED, setValueDelegate = { newState, set ->
        if (state != newState) {
            if (state == Peer.State.NON_CONNECTED) {
                set(newState)
                runBlocking {
                    dp.peers.notifyListeners(this@PeerImpl, Peers.Event.ADDED_ACTIVE_PEER)
                }
            } else if (newState != Peer.State.NON_CONNECTED) {
                set(newState)
                runBlocking {
                    dp.peers.notifyListeners(this@PeerImpl, Peers.Event.CHANGED_ACTIVE_PEER)
                }
            }
        }
    })
    override var downloadedVolume by Atomic<Long>(0L)
    override var uploadedVolume by Atomic<Long>(0L)
    override var lastUpdated by Atomic<Int>()
    private val mutex = Mutex()

    override val isAtLeastMyVersion: Boolean
        get() = isHigherOrEqualVersionThan(Burst.VERSION)

    override val software: String
        get() = (application.truncate("?", 10, false)
                + " (" + version.toString().truncate("?", 10, false) + ")"
                + " @ " + platform.truncate("?", 10, false))

    override val isWellKnown: Boolean
        get() = announcedAddress != null && dp.peers.wellKnownPeers.contains(announcedAddress!!)

    override val isRebroadcastTarget: Boolean
        get() = announcedAddress != null && dp.peers.rebroadcastPeers.contains(announcedAddress!!)

    override val isBlacklisted: Boolean
        get() = blacklistingTime > 0 || isOldVersion || dp.peers.knownBlacklistedPeers.contains(peerAddress)

    init {
        this.announcedAddress = announcedAddress
        try {
            this.port = URL(Constants.HTTP + announcedAddress).port
        } catch (ignored: MalformedURLException) {
        }
    }

    override suspend fun updateDownloadedVolume(volume: Long) {
        mutex.withLock {
            downloadedVolume += volume
        }
        dp.peers.notifyListeners(this, Peers.Event.DOWNLOADED_VOLUME)
    }

    override suspend fun updateUploadedVolume(volume: Long) {
        mutex.withLock {
            uploadedVolume += volume
        }
        dp.peers.notifyListeners(this, Peers.Event.UPLOADED_VOLUME)
    }

    override fun isHigherOrEqualVersionThan(version: Version): Boolean {
        return Peer.isHigherOrEqualVersion(version, this.version)
    }

    override fun setVersion(version: String?) {
        this.version = Version.EMPTY
        isOldVersion = false
        if (Burst.APPLICATION == application && version != null) {
            try {
                this.version = Version.parse(version)
                isOldVersion = Constants.MIN_VERSION.isGreaterThan(this.version)
            } catch (e: IllegalArgumentException) {
                isOldVersion = true
            }
        }
    }

    override suspend fun blacklist(cause: Exception, description: String) {
        if (cause is BurstException.NotCurrentlyValidException || cause is BlockchainProcessor.BlockOutOfOrderException
                || cause is SQLException || cause.cause is SQLException) {
            // don't blacklist peers just because a feature is not yet enabled, or because of database timeouts
            // prevents erroneous blacklisting during loading of blockchain from scratch
            return
        }
        if (cause is IOException) {
            // don't trigger verbose logging, if we had an IO Exception (eg. network stuff)
            blacklist()
        } else {
            val alreadyBlacklisted = isBlacklisted
            logger.safeError(cause) { "Reason for following blacklist: ${cause.message}" }
            blacklist(description) // refresh blacklist expiry
            if (!alreadyBlacklisted) {
                logger.safeDebug(cause) { "... because of: $cause" }
            }
        }
    }

    override suspend fun blacklist(description: String) {
        if (!isBlacklisted) {
            logger.safeInfo { "Blacklisting $peerAddress ($version) because of: $description" }
        }
        blacklist()
    }

    override suspend fun blacklist() {
        blacklistingTime = System.currentTimeMillis()
        state = Peer.State.NON_CONNECTED
        dp.peers.notifyListeners(this, Peers.Event.BLACKLIST)
    }

    override suspend fun unBlacklist() {
        state = Peer.State.NON_CONNECTED
        blacklistingTime = 0
        dp.peers.notifyListeners(this, Peers.Event.UNBLACKLIST)
    }

    override suspend fun updateBlacklistedStatus(curTime: Long) {
        if (blacklistingTime > 0 && blacklistingTime + dp.peers.blacklistingPeriod <= curTime) {
            unBlacklist()
        }
    }

    override suspend fun remove() {
        dp.peers.removePeer(this)
        dp.peers.notifyListeners(this, Peers.Event.REMOVE)
    }

    override suspend fun send(request: JsonElement): JsonObject? {
        var response: JsonObject? = null
        var log: String? = null
        var showLog = false
        var connection: HttpURLConnection? = null

        try {
            val address = if (announcedAddress != null) announcedAddress else peerAddress
            val buf = StringBuilder(Constants.HTTP)
            buf.append(address)
            if (port <= 0) {
                buf.append(':')
                buf.append(if (dp.propertyService.get(Props.DEV_TESTNET)) Peers.TESTNET_PEER_PORT else Peers.DEFAULT_PEER_PORT)
            }
            buf.append("/burst")
            val url = URL(buf.toString())

            if (dp.peers.communicationLoggingMask != 0) {
                val stringWriter = StringWriter()
                request.writeTo(stringWriter)
                log = "\"$url\": $stringWriter"
            }

            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = dp.peers.connectTimeout
            connection.readTimeout = dp.peers.readTimeout
            connection.addRequestProperty("User-Agent", "BRS/" + Burst.VERSION.toString())
            connection.setRequestProperty("Accept-Encoding", "gzip")
            connection.setRequestProperty("Connection", "close")

            val cos = CountingOutputStream(connection.outputStream)
            BufferedWriter(OutputStreamWriter(cos, StandardCharsets.UTF_8)).use { writer -> request.writeTo(writer) } // rico666: no catch?
            updateUploadedVolume(cos.count)

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val cis = CountingInputStream(connection.inputStream)
                var responseStream: InputStream = cis
                if ("gzip" == connection.getHeaderField("Content-Encoding")) {
                    responseStream = GZIPInputStream(cis)
                }
                if (dp.peers.communicationLoggingMask and Peers.LOGGING_MASK_200_RESPONSES != 0) {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    responseStream.use { inputStream -> inputStream.copyTo(byteArrayOutputStream, 1024) }
                    val responseValue = byteArrayOutputStream.toString("UTF-8")
                    if (responseValue.isNotEmpty() && responseStream is GZIPInputStream) {
                        log += String.format("[length: %d, compression ratio: %.2f]", cis.count, cis.count.toDouble() / responseValue.length.toDouble())
                    }
                    log += " >>> $responseValue"
                    showLog = true
                    response = JSON.getAsJsonObject(responseValue.parseJson())
                } else {
                    BufferedReader(InputStreamReader(responseStream, StandardCharsets.UTF_8)).use { reader -> response = JSON.getAsJsonObject(reader.parseJson()) }
                }
                updateDownloadedVolume(cis.count)
            } else {
                if (dp.peers.communicationLoggingMask and Peers.LOGGING_MASK_NON200_RESPONSES != 0) {
                    log += " >>> Peer responded with HTTP " + connection.responseCode + " code!"
                    showLog = true
                }
                state = if (state == Peer.State.CONNECTED) {
                    Peer.State.DISCONNECTED
                } else {
                    Peer.State.NON_CONNECTED
                }
                response = null
            }
        } catch (e: RuntimeException) {
            if (!isConnectionException(e)) {
                logger.safeDebug(e) { "Error sending JSON request" }
            }
            if (dp.peers.communicationLoggingMask and Peers.LOGGING_MASK_EXCEPTIONS != 0) {
                log += " >>> $e"
                showLog = true
            }
            if (state == Peer.State.CONNECTED) {
                state = Peer.State.DISCONNECTED
            }
            response = null
        } catch (e: IOException) {
            if (!isConnectionException(e)) {
                logger.safeDebug(e) { "Error sending JSON request" }
            }
            if (dp.peers.communicationLoggingMask and Peers.LOGGING_MASK_EXCEPTIONS != 0) {
                log += " >>> $e"
                showLog = true
            }
            if (state == Peer.State.CONNECTED) {
                state = Peer.State.DISCONNECTED
            }
            response = null
        }

        if (showLog && log != null) {
            logger.safeInfo { log }
        }

        connection?.disconnect()

        return response
    }

    private fun isConnectionException(e: Throwable): Boolean {
        if (e is UnknownHostException || e is SocketTimeoutException || e is SocketException) return true
        val cause = e.cause
        return cause != null && isConnectionException(cause)
    }

    override fun compareTo(other: Peer): Int {
        return 0
    }

    override suspend fun connect(currentTime: Int) {
        val response = send(dp.peers.myPeerInfoRequest)
        if (response != null) {
            application = JSON.getAsString(response.get("application"))
            setVersion(JSON.getAsString(response.get("version")))
            platform = JSON.getAsString(response.get("platform"))
            shareAddress = JSON.getAsBoolean(response.get("shareAddress")) == true
            val newAnnouncedAddress = JSON.getAsString(response.get("announcedAddress")).emptyToNull()
            if (newAnnouncedAddress != null && newAnnouncedAddress != announcedAddress) {
                // force verification of changed announced address
                state = Peer.State.NON_CONNECTED
                announcedAddress = newAnnouncedAddress
                return
            }
            if (announcedAddress == null) {
                announcedAddress = peerAddress
            }

            state = Peer.State.CONNECTED
            dp.peers.updateAddress(this)
            lastUpdated = currentTime
        } else {
            state = Peer.State.NON_CONNECTED
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PeerImpl::class.java)
    }
}
