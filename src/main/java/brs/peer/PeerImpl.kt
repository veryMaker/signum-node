package brs.peer

import brs.*
import brs.crypto.Crypto
import brs.props.Props
import brs.util.Convert
import brs.util.CountingInputStream
import brs.util.CountingOutputStream
import brs.util.JSON
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.*
import java.net.*
import java.nio.charset.StandardCharsets
import java.sql.SQLException
import java.util.Arrays
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.zip.GZIPInputStream

internal class PeerImpl(private val dp: DependencyProvider, override val peerAddress: String, announcedAddress: String) : Peer {
    private val announcedAddress = AtomicReference<String>()
    private val port = AtomicInteger()
    private val shareAddress = AtomicBoolean(false)
    private val platform = AtomicReference<String>()
    private val application = AtomicReference<String>()
    private val version = AtomicReference<Version>()
    private val isOldVersion = AtomicBoolean(false)
    private val blacklistingTime = AtomicLong()
    private val state = AtomicReference<Peer.State>()
    private val downloadedVolume = AtomicLong()
    private val uploadedVolume = AtomicLong()
    private val lastUpdated = AtomicInteger()
    private var lastDownloadedTransactionsDigest: ByteArray? = null
    private val lastDownloadedTransactionsLock = Any()

    override val isAtLeastMyVersion: Boolean
        get() = isHigherOrEqualVersionThan(Burst.VERSION)

    override val software: String
        get() = (Convert.truncate(application.get(), "?", 10, false)
                + " (" + Convert.truncate(version.toString(), "?", 10, false) + ")"
                + " @ " + Convert.truncate(platform.get(), "?", 10, false))

    override val isWellKnown: Boolean
        get() = announcedAddress.get() != null && Peers.wellKnownPeers.contains(announcedAddress.get())

    override val isRebroadcastTarget: Boolean
        get() = announcedAddress.get() != null && Peers.rebroadcastPeers.contains(announcedAddress.get())

    override val isBlacklisted: Boolean
        get() = blacklistingTime.get() > 0 || isOldVersion.get() || Peers.knownBlacklistedPeers.contains(peerAddress)

    init {
        this.announcedAddress.set(announcedAddress)
        try {
            this.port.set(URL(Constants.HTTP + announcedAddress).port)
        } catch (ignored: MalformedURLException) {
        }

        this.state.set(Peer.State.NON_CONNECTED)
        this.version.set(Version.EMPTY) //not null
        this.shareAddress.set(true)
    }

    override fun getState(): Peer.State {
        return state.get()
    }

    override fun isState(cmpState: Peer.State): Boolean {
        return state.get() == cmpState
    }

    override fun setState(state: Peer.State) {
        if (this.state.get() == state) {
            return
        }
        if (this.state.get() == Peer.State.NON_CONNECTED) {
            this.state.set(state)
            Peers.notifyListeners(this, Peers.Event.ADDED_ACTIVE_PEER)
        } else if (state != Peer.State.NON_CONNECTED) {
            this.state.set(state)
            Peers.notifyListeners(this, Peers.Event.CHANGED_ACTIVE_PEER)
        }
    }

    override fun getDownloadedVolume(): Long {
        return downloadedVolume.get()
    }

    fun diffLastDownloadedTransactions(data: ByteArray): Boolean {
        synchronized(lastDownloadedTransactionsLock) {
            val newDigest = Crypto.sha256().digest(data)
            if (lastDownloadedTransactionsDigest != null && Arrays.equals(newDigest, lastDownloadedTransactionsDigest)) {
                return false
            }
            lastDownloadedTransactionsDigest = newDigest
            return true
        }
    }

    override fun updateDownloadedVolume(volume: Long) {
        synchronized(this) {
            downloadedVolume.addAndGet(volume)
        }
        Peers.notifyListeners(this, Peers.Event.DOWNLOADED_VOLUME)
    }

    override fun getUploadedVolume(): Long {
        return uploadedVolume.get()
    }

    override fun updateUploadedVolume(volume: Long) {
        synchronized(this) {
            uploadedVolume.addAndGet(volume)
        }
        Peers.notifyListeners(this, Peers.Event.UPLOADED_VOLUME)
    }

    override fun getVersion(): Version {
        return version.get()
    }

    override fun isHigherOrEqualVersionThan(ourVersion: Version): Boolean {
        return Peer.isHigherOrEqualVersion(ourVersion, version.get())
    }

    fun setVersion(version: String?) {
        this.version.set(Version.EMPTY)
        isOldVersion.set(false)
        if (Burst.APPLICATION == getApplication() && version != null) {
            try {
                this.version.set(Version.parse(version))
                isOldVersion.set(Constants.MIN_VERSION.isGreaterThan(this.version.get()))
            } catch (e: IllegalArgumentException) {
                isOldVersion.set(true)
            }

        }
    }

    override fun getApplication(): String {
        return application.get()
    }

    fun setApplication(application: String) {
        this.application.set(application)
    }

    override fun getPlatform(): String {
        return platform.get()
    }

    fun setPlatform(platform: String) {
        this.platform.set(platform)
    }

    override fun shareAddress(): Boolean {
        return shareAddress.get()
    }

    fun setShareAddress(shareAddress: Boolean) {
        this.shareAddress.set(shareAddress)
    }

    override fun getAnnouncedAddress(): String {
        return announcedAddress.get()
    }

    fun setAnnouncedAddress(announcedAddress: String) {
        val announcedPeerAddress = Peers.normalizeHostAndPort(announcedAddress)
        if (announcedPeerAddress != null) {
            this.announcedAddress.set(announcedPeerAddress)
            try {
                this.port.set(URL(Constants.HTTP + announcedPeerAddress).port)
            } catch (ignored: MalformedURLException) {
            }

        }
    }

    override fun getPort(): Int {
        return port.get()
    }

    override fun blacklist(cause: Exception, description: String) {
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
            logger.error("Reason for following blacklist: " + cause.message, cause)
            blacklist(description) // refresh blacklist expiry
            if (!alreadyBlacklisted) {
                logger.debug("... because of: {}", cause, cause)
            }
        }
    }

    override fun blacklist(description: String) {
        if (!isBlacklisted) {
            if (logger.isInfoEnabled) {
                logger.info("Blacklisting {} ({}) because of: {}", peerAddress, getVersion(), description)
            }
        }
        blacklist()
    }

    override fun blacklist() {
        blacklistingTime.set(System.currentTimeMillis())
        setState(Peer.State.NON_CONNECTED)
        Peers.notifyListeners(this, Peers.Event.BLACKLIST)
    }

    override fun unBlacklist() {
        setState(Peer.State.NON_CONNECTED)
        blacklistingTime.set(0)
        Peers.notifyListeners(this, Peers.Event.UNBLACKLIST)
    }

    override fun updateBlacklistedStatus(curTime: Long) {
        if (blacklistingTime.get() > 0 && blacklistingTime.get() + Peers.blacklistingPeriod <= curTime) {
            unBlacklist()
        }
    }

    override fun remove() {
        Peers.removePeer(this)
        Peers.notifyListeners(this, Peers.Event.REMOVE)
    }

    override fun getLastUpdated(): Int {
        return lastUpdated.get()
    }

    fun setLastUpdated(lastUpdated: Int) {
        this.lastUpdated.set(lastUpdated)
    }

    override fun send(request: JsonElement): JsonObject? {

        var response: JsonObject?

        var log: String? = null
        var showLog = false
        var connection: HttpURLConnection? = null

        try {

            val address = if (announcedAddress.get() != null) announcedAddress.get() else peerAddress
            val buf = StringBuilder(Constants.HTTP)
            buf.append(address)
            if (port.get() <= 0) {
                buf.append(':')
                buf.append(if (dp.propertyService.get(Props.DEV_TESTNET)) Peers.TESTNET_PEER_PORT else Peers.DEFAULT_PEER_PORT)
            }
            buf.append("/burst")
            val url = URL(buf.toString())

            if (Peers.communicationLoggingMask != 0) {
                val stringWriter = StringWriter()
                JSON.writeTo(request, stringWriter)
                log = "\"$url\": $stringWriter"
            }

            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = Peers.connectTimeout
            connection.readTimeout = Peers.readTimeout
            connection.addRequestProperty("User-Agent", "BRS/" + Burst.VERSION.toString())
            connection.setRequestProperty("Accept-Encoding", "gzip")
            connection.setRequestProperty("Connection", "close")

            val cos = CountingOutputStream(connection.outputStream)
            BufferedWriter(OutputStreamWriter(cos, StandardCharsets.UTF_8)).use { writer -> JSON.writeTo(request, writer) } // rico666: no catch?
            updateUploadedVolume(cos.count)

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val cis = CountingInputStream(connection.inputStream)
                var responseStream: InputStream = cis
                if ("gzip" == connection.getHeaderField("Content-Encoding")) {
                    responseStream = GZIPInputStream(cis)
                }
                if (Peers.communicationLoggingMask and Peers.LOGGING_MASK_200_RESPONSES != 0) {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    val buffer = ByteArray(1024)
                    var numberOfBytes: Int
                    responseStream.use { inputStream ->
                        while ((numberOfBytes = inputStream.read(buffer, 0, buffer.size)) > 0) {
                            byteArrayOutputStream.write(buffer, 0, numberOfBytes)
                        }
                    }
                    val responseValue = byteArrayOutputStream.toString("UTF-8")
                    if (!responseValue.isEmpty() && responseStream is GZIPInputStream) {
                        log += String.format("[length: %d, compression ratio: %.2f]", cis.count, cis.count.toDouble() / responseValue.length.toDouble())
                    }
                    log += " >>> $responseValue"
                    showLog = true
                    response = JSON.getAsJsonObject(JSON.parse(responseValue))
                } else {
                    BufferedReader(InputStreamReader(responseStream, StandardCharsets.UTF_8)).use { reader -> response = JSON.getAsJsonObject(JSON.parse(reader)) }
                }
                updateDownloadedVolume(cis.count)
            } else {

                if (Peers.communicationLoggingMask and Peers.LOGGING_MASK_NON200_RESPONSES != 0) {
                    log += " >>> Peer responded with HTTP " + connection.responseCode + " code!"
                    showLog = true
                }
                if (state.get() == Peer.State.CONNECTED) {
                    setState(Peer.State.DISCONNECTED)
                } else {
                    setState(Peer.State.NON_CONNECTED)
                }
                response = null
            }

        } catch (e: RuntimeException) {
            if (!isConnectionException(e)) {
                logger.debug("Error sending JSON request", e)
            }
            if (Peers.communicationLoggingMask and Peers.LOGGING_MASK_EXCEPTIONS != 0) {
                log += " >>> $e"
                showLog = true
            }
            if (state.get() == Peer.State.CONNECTED) {
                setState(Peer.State.DISCONNECTED)
            }
            response = null
        } catch (e: IOException) {
            if (!isConnectionException(e)) {
                logger.debug("Error sending JSON request", e)
            }
            if (Peers.communicationLoggingMask and Peers.LOGGING_MASK_EXCEPTIONS != 0) {
                log += " >>> $e"
                showLog = true
            }
            if (state.get() == Peer.State.CONNECTED) {
                setState(Peer.State.DISCONNECTED)
            }
            response = null
        }

        if (showLog) {
            logger.info(log)
        }

        connection?.disconnect()

        return response

    }

    private fun isConnectionException(e: Throwable): Boolean {
        if (e is UnknownHostException || e is SocketTimeoutException || e is SocketException) return true
        return if (e.cause == null) false else isConnectionException(e.cause)
    }

    override fun compareTo(o: Peer): Int {
        return 0
    }

    override fun connect(currentTime: Int) {
        val response = send(Peers.myPeerInfoRequest)
        if (response != null) {
            application.set(JSON.getAsString(response.get("application")))
            setVersion(JSON.getAsString(response.get("version")))
            platform.set(JSON.getAsString(response.get("platform")))
            shareAddress.set(java.lang.Boolean.TRUE == JSON.getAsBoolean(response.get("shareAddress")))
            val newAnnouncedAddress = Convert.emptyToNull(JSON.getAsString(response.get("announcedAddress")))
            if (newAnnouncedAddress != null && newAnnouncedAddress != announcedAddress.get()) {
                // force verification of changed announced address
                setState(Peer.State.NON_CONNECTED)
                setAnnouncedAddress(newAnnouncedAddress)
                return
            }
            if (announcedAddress.get() == null) {
                setAnnouncedAddress(peerAddress)
            }

            setState(Peer.State.CONNECTED)
            Peers.updateAddress(this)
            lastUpdated.set(currentTime)
        } else {
            setState(Peer.State.NON_CONNECTED)
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(PeerImpl::class.java)
    }

}
