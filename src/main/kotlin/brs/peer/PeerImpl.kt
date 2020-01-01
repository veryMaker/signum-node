package brs.peer

import brs.Burst
import brs.entity.DependencyProvider
import brs.objects.Constants
import brs.objects.Props
import brs.services.BlockchainProcessorService
import brs.services.PeerService
import brs.services.impl.PeerServiceImpl
import brs.util.BurstException
import brs.util.CountingInputStream
import brs.util.CountingOutputStream
import brs.util.Version
import brs.util.convert.emptyToNull
import brs.util.convert.truncate
import brs.util.delegates.Atomic
import brs.util.delegates.AtomicLateinit
import brs.util.delegates.AtomicWithOverride
import brs.util.json.*
import brs.util.logging.safeDebug
import brs.util.logging.safeError
import brs.util.logging.safeInfo
import brs.util.sync.Mutex
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import java.io.*
import java.net.*
import java.nio.charset.StandardCharsets
import java.sql.SQLException
import java.util.zip.GZIPInputStream

internal class PeerImpl(
    private val dp: DependencyProvider,
    override val peerAddress: String,
    announcedAddress: String?
) : Peer {
    override var announcedAddress by AtomicWithOverride<String?>(
        initialValue = null,
        setValueDelegate = { announcedAddress, set ->
            val announcedPeerAddress = dp.peerService.normalizeHostAndPort(announcedAddress)
            if (announcedPeerAddress != null) {
                set(announcedPeerAddress)
                try {
                    this.port = URL(Constants.HTTP + announcedPeerAddress).port
                } catch (ignored: MalformedURLException) {
                }
            }
        })
    override var port by AtomicLateinit<Int>()
    override var shareAddress by Atomic(true)
    override var platform by AtomicLateinit<String>()
    override var application by AtomicLateinit<String>()
    override var version by Atomic(Version.EMPTY)
    private var isOldVersion by Atomic(false)
    private var blacklistingTime by Atomic<Long>(0)
    override var state: Peer.State by AtomicWithOverride(
        initialValue = Peer.State.NON_CONNECTED,
        setValueDelegate = { newState, set ->
            if (state != newState) {
                if (state == Peer.State.NON_CONNECTED) {
                    set(newState)
                    dp.peerService.notifyListeners(this@PeerImpl, PeerService.Event.ADDED_ACTIVE_PEER)
                } else if (newState != Peer.State.NON_CONNECTED) {
                    set(newState)
                    dp.peerService.notifyListeners(this@PeerImpl, PeerService.Event.CHANGED_ACTIVE_PEER)
                }
            }
        })
    override var downloadedVolume by Atomic(0L)
    override var uploadedVolume by Atomic(0L)
    override var lastUpdated by AtomicLateinit<Int>()
    private val mutex = Mutex()

    override val isAtLeastMyVersion: Boolean
        get() = isHigherOrEqualVersionThan(Burst.VERSION)

    override val software: String
        get() = (application.truncate("?", 10, false)
                + " (" + version.toString().truncate("?", 10, false) + ")"
                + " @ " + platform.truncate("?", 10, false))

    override val isWellKnown: Boolean
        get() = announcedAddress != null && dp.peerService.wellKnownPeers.contains(announcedAddress!!)

    override val isRebroadcastTarget: Boolean
        get() = announcedAddress != null && dp.peerService.rebroadcastPeers.contains(announcedAddress!!)

    override val isBlacklisted: Boolean
        get() = blacklistingTime > 0 || isOldVersion || dp.peerService.knownBlacklistedPeers.contains(peerAddress)

    init {
        this.announcedAddress = announcedAddress
        try {
            this.port = URL(Constants.HTTP + announcedAddress).port
        } catch (ignored: MalformedURLException) {
        }
    }

    override fun updateDownloadedVolume(volume: Long) {
        mutex.withLock {
            downloadedVolume += volume
        }
        dp.peerService.notifyListeners(this, PeerService.Event.DOWNLOADED_VOLUME)
    }

    override fun updateUploadedVolume(volume: Long) {
        mutex.withLock {
            uploadedVolume += volume
        }
        dp.peerService.notifyListeners(this, PeerService.Event.UPLOADED_VOLUME)
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

    override fun blacklist(cause: Exception, description: String) {
        if (cause is BurstException.NotCurrentlyValidException || cause is BlockchainProcessorService.BlockOutOfOrderException
            || cause is SQLException || cause.cause is SQLException
        ) {
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

    override fun blacklist(description: String) {
        if (!isBlacklisted) {
            logger.safeInfo { "Blacklisting $peerAddress ($version) because of: $description" }
        }
        blacklist()
    }

    override fun blacklist() {
        blacklistingTime = System.currentTimeMillis()
        state = Peer.State.NON_CONNECTED
        dp.peerService.notifyListeners(this, PeerService.Event.BLACKLIST)
    }

    override fun unBlacklist() {
        state = Peer.State.NON_CONNECTED
        blacklistingTime = 0
        dp.peerService.notifyListeners(this, PeerService.Event.UNBLACKLIST)
    }

    override fun updateBlacklistedStatus(curTime: Long) {
        if (blacklistingTime > 0 && blacklistingTime + dp.peerService.blacklistingPeriod <= curTime) {
            unBlacklist()
        }
    }

    override fun remove() {
        dp.peerService.removePeer(this)
    }

    override fun send(request: JsonElement): JsonObject? {
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
                buf.append(if (dp.propertyService.get(Props.DEV_TESTNET)) PeerServiceImpl.TESTNET_PEER_PORT else PeerServiceImpl.DEFAULT_PEER_PORT)
            }
            buf.append("/burst")
            val url = URL(buf.toString())

            if (dp.peerService.communicationLoggingMask != 0) {
                val stringWriter = StringWriter()
                request.writeTo(stringWriter)
                log = "\"$url\": $stringWriter"
            }

            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = dp.peerService.connectTimeout
            connection.readTimeout = dp.peerService.readTimeout
            connection.addRequestProperty("User-Agent", "BRS/" + Burst.VERSION.toString())
            connection.setRequestProperty("Accept-Encoding", "gzip")
            connection.setRequestProperty("Connection", "close")

            val cos = CountingOutputStream(connection.outputStream)
            BufferedWriter(
                OutputStreamWriter(
                    cos,
                    StandardCharsets.UTF_8
                )
            ).use { writer -> request.writeTo(writer) } // rico666: no catch?
            updateUploadedVolume(cos.count)

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val cis = CountingInputStream(connection.inputStream)
                var responseStream: InputStream = cis
                if ("gzip" == connection.getHeaderField("Content-Encoding")) {
                    responseStream = GZIPInputStream(cis)
                }
                if (dp.peerService.communicationLoggingMask and PeerServiceImpl.LOGGING_MASK_200_RESPONSES != 0) {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    responseStream.use { inputStream -> inputStream.copyTo(byteArrayOutputStream, 1024) }
                    val responseValue = byteArrayOutputStream.toString("UTF-8")
                    if (responseValue.isNotEmpty() && responseStream is GZIPInputStream) {
                        log += String.format(
                            "[length: %d, compression ratio: %.2f]",
                            cis.count,
                            cis.count.toDouble() / responseValue.length.toDouble()
                        )
                    }
                    log += " >>> $responseValue"
                    showLog = true
                    response = responseValue.parseJson().safeGetAsJsonObject()
                } else {
                    BufferedReader(InputStreamReader(responseStream, StandardCharsets.UTF_8)).use { reader ->
                        response = reader.parseJson().safeGetAsJsonObject()
                    }
                }
                updateDownloadedVolume(cis.count)
            } else {
                if (dp.peerService.communicationLoggingMask and PeerServiceImpl.LOGGING_MASK_NON200_RESPONSES != 0) {
                    log += " >>> Peer responded with HTTP " + connection.responseCode + " code!"
                    showLog = true
                }
                state = if (state == Peer.State.CONNECTED) {
                    Peer.State.DISCONNECTED
                } else {
                    Peer.State.NON_CONNECTED
                }
                response = error("Peer responded with HTTP " + connection.responseCode)
            }
        } catch (e: Exception) {
            if (!isConnectionException(e)) {
                logger.safeDebug(e) { "Error sending JSON request" }
            }
            if (dp.peerService.communicationLoggingMask and PeerServiceImpl.LOGGING_MASK_EXCEPTIONS != 0) {
                log += " >>> $e"
                showLog = true
            }
            if (state == Peer.State.CONNECTED) {
                state = Peer.State.DISCONNECTED
            }
            response = error("Error getting response from peer: ${e.javaClass}: ${e.message}")
        } catch (e: IOException) {
            if (!isConnectionException(e)) {
                logger.safeDebug(e) { "Error sending JSON request" }
            }
            if (dp.peerService.communicationLoggingMask and PeerServiceImpl.LOGGING_MASK_EXCEPTIONS != 0) {
                log += " >>> $e"
                showLog = true
            }
            if (state == Peer.State.CONNECTED) {
                state = Peer.State.DISCONNECTED
            }
            response = error("Error getting response from peer: ${e.javaClass}: ${e.message}")
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

    override fun connect(currentTime: Int) {
        val response = send(dp.peerService.myPeerInfoRequest)
        if (response != null && response.get("error") == null) {
            application = response.mustGetMemberAsString("application")
            setVersion(response.mustGetMemberAsString("version"))
            platform = response.mustGetMemberAsString("platform")
            shareAddress = response.getMemberAsBoolean("shareAddress") == true
            val newAnnouncedAddress = response.getMemberAsString("announcedAddress").emptyToNull()
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
            dp.peerService.updateAddress(this)
            lastUpdated = currentTime
        } else {
            state = Peer.State.NON_CONNECTED
        }
    }

    private fun error(message: String): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("error", message)
        return jsonObject
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PeerImpl::class.java)
    }
}
