package brs.peer

import brs.Burst
import brs.entity.Block
import brs.entity.DependencyProvider
import brs.entity.PeerInfo
import brs.entity.Transaction
import brs.objects.Constants
import brs.services.BlockchainProcessorService
import brs.services.PeerService
import brs.services.impl.PeerServiceImpl
import brs.util.BurstException
import brs.util.CountingInputStream
import brs.util.CountingOutputStream
import brs.util.Version
import brs.util.convert.emptyToNull
import brs.util.convert.parseUnsignedLong
import brs.util.convert.toUnsignedString
import brs.util.delegates.Atomic
import brs.util.delegates.AtomicLateinit
import brs.util.delegates.AtomicWithOverride
import brs.util.json.*
import brs.util.logging.safeDebug
import brs.util.logging.safeError
import brs.util.logging.safeInfo
import brs.util.misc.filteringMap
import brs.util.sync.Mutex
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import java.io.*
import java.math.BigInteger
import java.net.*
import java.nio.charset.StandardCharsets
import java.sql.SQLException
import java.util.zip.GZIPInputStream

internal class HttpPeerImpl(
    private val dp: DependencyProvider,
    override val remoteAddress: String,
    address: PeerAddress?
) : Peer {
    override val address: PeerAddress
        get() = announcedAddress ?: PeerAddress.parse(dp, remoteAddress) ?: error("Could not find peer's address")

    private var announcedAddress by Atomic<PeerAddress?>(null)

    init {
        if (address != null) {
            this.announcedAddress = address
        }
    }

    override var shareAddress by Atomic(true)
    override var platform by AtomicLateinit<String>()
    override var application by AtomicLateinit<String>()
    override var version by AtomicWithOverride(
        initialValue = Version.EMPTY,
        setValueDelegate = { newVersion, set ->
            set(Version.EMPTY)
            isOldVersion = false
            if (Burst.APPLICATION == application) {
                isOldVersion = try {
                    set(newVersion)
                    Constants.MIN_VERSION.isGreaterThan(newVersion)
                } catch (e: Exception) {
                    true
                }
            }
        })
    private var isOldVersion by Atomic(false)
    private var blacklistingTime by Atomic<Long>(0)
    override var state: Peer.State by AtomicWithOverride(
        initialValue = Peer.State.NON_CONNECTED,
        setValueDelegate = { newState, set ->
            if (state != newState) {
                if (state == Peer.State.NON_CONNECTED) {
                    set(newState)
                    dp.peerService.notifyListeners(this@HttpPeerImpl, PeerService.Event.ADDED_ACTIVE_PEER)
                } else if (newState != Peer.State.NON_CONNECTED) {
                    set(newState)
                    dp.peerService.notifyListeners(this@HttpPeerImpl, PeerService.Event.CHANGED_ACTIVE_PEER)
                }
            }
        })
    override var downloadedVolume by Atomic(0L)
    override var uploadedVolume by Atomic(0L)
    override var lastUpdated by AtomicLateinit<Int>()
    private val mutex = Mutex()

    override val isAtLeastMyVersion: Boolean
        get() = isHigherOrEqualVersionThan(Burst.VERSION)

    override val isWellKnown: Boolean
        get() = dp.peerService.wellKnownPeers.contains(address)

    override val isRebroadcastTarget: Boolean
        get() = dp.peerService.rebroadcastPeers.contains(address)

    override val isBlacklisted: Boolean
        get() = blacklistingTime > 0 || isOldVersion || dp.peerService.knownBlacklistedPeers.contains(address)

    override val readyToSend: Boolean
        get() = TODO()

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
            logger.safeInfo { "Blacklisting $remoteAddress ($version) because of: $description" }
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

    private fun onTimeout() {
        error("Timeout")
    }

    private fun couldNotConnect() {
        error("Could not connect")
    }

    private fun checkError(json: JsonObject) {
        val error = json.getMemberAsString("error")
        if (error != null) {
            when {
                error.contains("connect timed out") -> onTimeout()
                error.contains("Connection refused") -> couldNotConnect()
                else -> error(error)
            }
        }
    }

    override fun exchangeInfo(): PeerInfo {
        val json = send(dp.peerService.myPeerInfoRequest) ?: error("Returned JSON was null")
        checkError(json)
        return PeerInfo.fromJson(json)
    }

    override fun getCumulativeDifficulty(): Pair<BigInteger, Int> {
        val json = send(getCumulativeDifficultyRequest) ?: error("Returned JSON was null")
        checkError(json)
        return Pair(BigInteger(json.mustGetMemberAsString("cumulativeDifficulty")), json.mustGetMemberAsInt("blockchainHeight"))
    }

    override fun getUnconfirmedTransactions(): Collection<Transaction> {
        val json = send(getUnconfirmedTransactionsRequest) ?: error("Returned JSON was null")
        checkError(json)
        return json.mustGetMemberAsJsonArray("unconfirmedTransactions").map { Transaction.parseTransaction(dp, it.mustGetAsJsonObject("transaction")) }
    }

    private fun getMilestoneBlockIds(request: JsonObject): Pair<Collection<Long>, Boolean> {
        val json = send(JSON.prepareRequest(request)) ?: error("Returned JSON was null")
        checkError(json)
        val milestoneBlockIds = json.mustGetMemberAsJsonArray("milestoneBlockIds")
            .map { it.safeGetAsString().parseUnsignedLong() }
            .filter { it != 0L }
        val last = json.getMemberAsBoolean("last") ?: false
        return Pair(milestoneBlockIds, last)
    }

    override fun getMilestoneBlockIds(): Pair<Collection<Long>, Boolean> {
        val request = JsonObject()
        request.addProperty("requestType", "getMilestoneBlockIds")
        request.addProperty("lastBlockId", dp.downloadCacheService.getLastBlockId().toUnsignedString())
        return getMilestoneBlockIds(request)
    }

    override fun getMilestoneBlockIds(lastMilestoneBlockId: Long): Pair<Collection<Long>, Boolean> {
        val request = JsonObject()
        request.addProperty("requestType", "getMilestoneBlockIds")
        request.addProperty("lastMilestoneBlockId", lastMilestoneBlockId)
        return getMilestoneBlockIds(request)
    }

    override fun sendUnconfirmedTransactions(transactions: Collection<Transaction>) {
        val jsonTransactions = JsonArray()
        transactions.map { it.toJsonObject() }.forEach { jsonTransactions.add(it) }
        val request = JsonObject()
        request.addProperty("requestType", "processTransactions")
        request.add("transactions", jsonTransactions)
        val json = send(JSON.prepareRequest(request)) ?: error("Returned JSON was null")
        checkError(json)
    }

    override fun getNextBlocks(lastBlockId: Long): Collection<Block> {
        val firstNewBlockHeight = (dp.downloadCacheService.getBlock(lastBlockId) ?: error("Block with ID $lastBlockId not found in cache")).height + 1
        val request = JsonObject()
        request.addProperty("requestType", "getNextBlocks")
        request.addProperty("blockId", lastBlockId.toUnsignedString())
        val json = send(JSON.prepareRequest(request)) ?: error("Returned JSON was null")
        checkError(json)
        return json.mustGetMemberAsJsonArray("nextBlocks")
            .asSequence()
            .take(Constants.MAX_PEER_RECEIVED_BLOCKS)
            .map { it.mustGetAsJsonObject("block") }
            .mapIndexed { index, jsonElement -> Block.parseBlock(dp, jsonElement.mustGetAsJsonObject("block"), firstNewBlockHeight + index) }
            .toList()
    }

    override fun getNextBlockIds(lastBlockId: Long): Collection<Long> {
        val request = JsonObject()
        request.addProperty("requestType", "getNextBlockIds")
        request.addProperty("blockId", lastBlockId.toUnsignedString())
        val json = send(JSON.prepareRequest(request)) ?: error("Returned JSON was null")
        checkError(json)
        return json.mustGetMemberAsJsonArray("nextBlockIds")
            .asSequence()
            .take(Constants.MAX_PEER_RECEIVED_BLOCKS)
            .map { it.safeGetAsString().parseUnsignedLong() }
            .filter { it != 0L }
            .toList()
    }

    override fun addPeers(announcedAddresses: Collection<PeerAddress>) {
        val jsonAnnouncedAddresses = JsonArray()
        if (this.version.isGreaterThanOrEqualTo(Version.parse("v3.0.0-dev"))) { // TODO don't parse version
            announcedAddresses.forEach { jsonAnnouncedAddresses.add(it.toString()) }
        } else {
            announcedAddresses.forEach { jsonAnnouncedAddresses.add("${it.host}:${it.port}") }
        }
        val request = JsonObject()
        request.addProperty("requestType", "getNextBlockIds")
        request.add("peers", jsonAnnouncedAddresses)
        val json = send(JSON.prepareRequest(request)) ?: error("Returned JSON was null")
        checkError(json)
    }

    override fun getPeers(): Collection<PeerAddress> {
        val json = send(getPeersRequest) ?: error("Returned JSON was null")
        checkError(json)
        return json.mustGetMemberAsJsonArray("peers")
            .map { it.safeGetAsString() ?: "" }
            .filter { it.isNotBlank() }
            .filteringMap { PeerAddress.parse(dp, it) }
    }

    override fun sendBlock(block: Block): Boolean {
        val request = block.toJsonObject()
        request.addProperty("requestType", "processBlock")
        val json = send(JSON.prepareRequest(request)) ?: error("Returned JSON was null")
        checkError(json)
        return json.mustGetMemberAsBoolean("accepted")
    }

    private fun send(request: JsonElement): JsonObject? {
        var response: JsonObject? = null
        var log: String? = null
        var showLog = false
        var connection: HttpURLConnection? = null

        try {
            val buf = StringBuilder(address.toString())
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
                response = jsonError("Peer responded with HTTP " + connection.responseCode)
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
            response = jsonError("Error getting response from peer: ${e.javaClass}: ${e.message}")
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
        val response = exchangeInfo()
        application = response.application
        version = response.version
        platform = response.platform
        shareAddress = response.shareAddress
        val newAnnouncedAddress = response.announcedAddress.emptyToNull()
        if (newAnnouncedAddress != null) {
            val parsedAddress = PeerAddress.parse(dp, newAnnouncedAddress)
            if (parsedAddress != null && parsedAddress != announcedAddress) {
                updateAddress(parsedAddress)
            }
        }

        if (announcedAddress == null) {
            announcedAddress = PeerAddress.parse(dp, remoteAddress) ?: error("Could not find peer's address")
        }

        state = Peer.State.CONNECTED
        lastUpdated = currentTime
    }

    private fun jsonError(message: String): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("error", message)
        return jsonObject
    }

    override fun updateAddress(newAnnouncedAddress: PeerAddress) {
        announcedAddress = newAnnouncedAddress
        // Force re-validate address
        state = Peer.State.NON_CONNECTED
        dp.peerService.updateAddress(this)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HttpPeerImpl::class.java)

        private val getCumulativeDifficultyRequest = run {
            val request = JsonObject()
            request.addProperty("requestType", "getCumulativeDifficulty")
            JSON.prepareRequest(request)
        }

        private val getUnconfirmedTransactionsRequest = run {
            val request = JsonObject()
            request.addProperty("requestType", "getUnconfirmedTransactions")
            JSON.prepareRequest(request)
        }

        private val getPeersRequest = run {
            val request = JsonObject()
            request.addProperty("requestType", "getPeers")
            JSON.prepareRequest(request)
        }
    }
}
