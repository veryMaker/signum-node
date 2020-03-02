package brs.peer

import brs.Burst
import brs.entity.Block
import brs.entity.DependencyProvider
import brs.entity.PeerInfo
import brs.entity.Transaction
import brs.objects.Constants
import brs.services.BlockchainProcessorService
import brs.services.PeerService
import brs.util.BurstException
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
import brs.util.logging.safeWarn
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
    private val parsedRemoteAddress = PeerAddress.parse(dp, remoteAddress)

    override val address: PeerAddress
        get() = announcedAddress ?: parsedRemoteAddress ?: error("Could not find peer's address")

    private var announcedAddress by Atomic<PeerAddress?>(null)

    init {
        if (address != null) {
            require(address.protocol == PeerAddress.Protocol.HTTP) { "Protocol must be HTTP" }
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

    override var isConnected by Atomic(false)

    override var lastUpdated by AtomicLateinit<Int>()

    override val isBlacklisted: Boolean
        get() = blacklistingTime > 0 || isOldVersion || dp.peerService.configuredBlacklistedPeers.contains(address)

    override fun isHigherOrEqualVersionThan(version: Version): Boolean {
        return Peer.isHigherOrEqualVersion(version, this.version)
    }

    override fun blacklist(cause: Exception, description: String) {
        if (cause is BurstException.NotCurrentlyValidException || cause is BlockchainProcessorService.BlockOutOfOrderException || cause is SQLException || cause.cause is SQLException) {
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

    private fun blacklist() {
        blacklistingTime = System.currentTimeMillis()
        isConnected = false
        dp.peerService.notifyListeners(this, PeerService.Event.BLACKLIST)
    }

    override fun updateBlacklistedStatus(curTime: Long) {
        if (blacklistingTime > 0 && blacklistingTime + dp.peerService.blacklistingPeriod <= curTime) {
            isConnected = false
            blacklistingTime = 0
            dp.peerService.notifyListeners(this, PeerService.Event.UNBLACKLIST)
        }
    }

    private inline fun <T: Any> handlePeerError(errorMessage: String, action: () -> T): T? {
        return try {
            action()
        } catch (e: Exception) {
            if (e != RETURNED_JSON_NULL && !isConnectionException(e)) {
                if (isConnected) {
                    isConnected = false
                }
                logger.safeWarn(e) { errorMessage }
            }
            null
        }
    }

    private fun checkError(json: JsonObject) {
        val error = json.getMemberAsString("error")
        if (!error.isNullOrBlank()) {
            throw Exception("Peer Error: $error")
        }
    }

    override fun exchangeInfo(): PeerInfo? {
        return handlePeerError("Error exchanging info with peer") {
            val json = send(dp.peerService.myJsonPeerInfoRequest) ?: throw RETURNED_JSON_NULL
            checkError(json)
            PeerInfo.fromJson(json)
        }
    }

    override fun getCumulativeDifficulty(): Pair<BigInteger, Int>? {
        return handlePeerError("Error getting cumulative difficulty from peer") {
            val json = send(getCumulativeDifficultyRequest) ?: throw RETURNED_JSON_NULL
            checkError(json)
            Pair(BigInteger(json.mustGetMemberAsString("cumulativeDifficulty")), json.mustGetMemberAsInt("blockchainHeight"))
        }
    }

    override fun getUnconfirmedTransactions(): Collection<Transaction>? {
        return handlePeerError("Error getting unconfirmed transactions from peer") {
            val json = send(getUnconfirmedTransactionsRequest) ?: throw RETURNED_JSON_NULL
            checkError(json)
            json.mustGetMemberAsJsonArray("unconfirmedTransactions").map { Transaction.parseTransaction(dp, it.mustGetAsJsonObject("transaction")) }
        }
    }

    private fun getMilestoneBlockIds(request: JsonObject): Pair<Collection<Long>, Boolean>? {
        return handlePeerError("Error getting milestone block IDs") {
            val json = send(JSON.prepareRequest(request)) ?: throw RETURNED_JSON_NULL
            checkError(json)
            val milestoneBlockIds = json.mustGetMemberAsJsonArray("milestoneBlockIds")
                .map { it.safeGetAsString().parseUnsignedLong() }
                .filter { it != 0L }
            val last = json.getMemberAsBoolean("last") ?: false
            Pair(milestoneBlockIds, last)
        }
    }

    override fun getMilestoneBlockIds(): Pair<Collection<Long>, Boolean>? {
        val request = JsonObject()
        request.addProperty("requestType", "getMilestoneBlockIds")
        request.addProperty("lastBlockId", dp.downloadCacheService.getLastBlockId().toUnsignedString())
        return getMilestoneBlockIds(request)
    }

    override fun getMilestoneBlockIds(lastMilestoneBlockId: Long): Pair<Collection<Long>, Boolean>? {
        val request = JsonObject()
        request.addProperty("requestType", "getMilestoneBlockIds")
        request.addProperty("lastMilestoneBlockId", lastMilestoneBlockId.toUnsignedString())
        return getMilestoneBlockIds(request)
    }

    override fun sendUnconfirmedTransactions(transactions: Collection<Transaction>) {
        handlePeerError("Error sending unconfirmed transactions to peer") {
            val jsonTransactions = JsonArray()
            transactions.map { it.toJsonObject() }.forEach { jsonTransactions.add(it) }
            val request = JsonObject()
            request.addProperty("requestType", "processTransactions")
            request.add("transactions", jsonTransactions)
            val json = send(JSON.prepareRequest(request)) ?: throw RETURNED_JSON_NULL
            checkError(json)
        }
    }

    override fun getNextBlocks(lastBlockId: Long): Collection<Block>? {
        return handlePeerError("Error getting next blocks from peer") {
            val firstNewBlockHeight = (dp.downloadCacheService.getBlock(lastBlockId) ?: error("Block with ID $lastBlockId not found in cache")).height + 1
            val request = JsonObject()
            request.addProperty("requestType", "getNextBlocks")
            request.addProperty("blockId", lastBlockId.toUnsignedString())
            val json = send(JSON.prepareRequest(request)) ?: throw RETURNED_JSON_NULL
            checkError(json)
            json.mustGetMemberAsJsonArray("nextBlocks")
                .asSequence()
                .take(Constants.MAX_PEER_RECEIVED_BLOCKS)
                .map { it.mustGetAsJsonObject("block") }
                .mapIndexed { index, jsonElement -> Block.parseBlock(dp, jsonElement.mustGetAsJsonObject("block"), firstNewBlockHeight + index) }
                .toList()
        }
    }

    override fun getNextBlockIds(lastBlockId: Long): Collection<Long>? {
        return handlePeerError("Error getting next block IDs from peer") {
            val request = JsonObject()
            request.addProperty("requestType", "getNextBlockIds")
            request.addProperty("blockId", lastBlockId.toUnsignedString())
            val json = send(JSON.prepareRequest(request)) ?: throw RETURNED_JSON_NULL
            checkError(json)
            json.mustGetMemberAsJsonArray("nextBlockIds")
                .asSequence()
                .take(Constants.MAX_PEER_RECEIVED_BLOCKS)
                .map { it.safeGetAsString().parseUnsignedLong() }
                .filter { it != 0L }
                .toList()
        }
    }

    override fun addPeers(announcedAddresses: Collection<PeerAddress>) {
        handlePeerError("Error sending peers to peer") {
            val jsonAnnouncedAddresses = JsonArray()
            if (this.version.isGreaterThanOrEqualTo(Constants.NEW_PEER_API_MIN_VERSION)) {
                announcedAddresses.forEach { jsonAnnouncedAddresses.add(it.toString()) }
            } else {
                announcedAddresses.forEach { jsonAnnouncedAddresses.add("${it.host}:${it.port}") }
            }
            val request = JsonObject()
            request.addProperty("requestType", "addPeers")
            request.add("peers", jsonAnnouncedAddresses)
            val json = send(JSON.prepareRequest(request)) ?: throw RETURNED_JSON_NULL
            checkError(json)
        }
    }

    override fun getPeers(): Collection<PeerAddress>? {
        return handlePeerError("Error getting peers from peer") {
            val json = send(getPeersRequest) ?: throw RETURNED_JSON_NULL
            checkError(json)
            json.mustGetMemberAsJsonArray("peers")
                .map { it.safeGetAsString() ?: "" }
                .filter { it.isNotBlank() }
                .mapNotNull { PeerAddress.parse(dp, it) }
        }
    }

    override fun sendBlock(block: Block): Boolean {
        return handlePeerError("Error sending block to peers") {
            val request = block.toJsonObject()
            request.addProperty("requestType", "processBlock")
            val json = send(JSON.prepareRequest(request)) ?: throw RETURNED_JSON_NULL
            checkError(json)
            json.mustGetMemberAsBoolean("accepted")
        } ?: false
    }

    private fun send(request: JsonElement): JsonObject? {
        var connection: HttpURLConnection? = null
        try {
            connection = URL("$address/burst").openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = dp.peerService.connectTimeout
            connection.readTimeout = dp.peerService.readTimeout
            connection.addRequestProperty("User-Agent", "BRS/" + Burst.VERSION.toString())
            connection.setRequestProperty("Accept-Encoding", "gzip")
            connection.setRequestProperty("Connection", "close")

            BufferedWriter(OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8))
                .use { writer -> request.writeTo(writer) }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                var responseStream = connection.inputStream
                if ("gzip" == connection.getHeaderField("Content-Encoding")) responseStream = GZIPInputStream(responseStream)
                BufferedReader(InputStreamReader(responseStream, StandardCharsets.UTF_8)).use { reader ->
                    return reader.parseJson().safeGetAsJsonObject()
                }
            } else {
                isConnected = false
                throw Exception("Bad HTTP Response: ${connection.responseCode}")
            }
        } finally {
            connection?.disconnect()
        }
    }

    private fun isConnectionException(e: Throwable): Boolean {
        if (e is UnknownHostException || e is SocketTimeoutException || e is SocketException) return true
        val cause = e.cause
        return cause != null && isConnectionException(cause)
    }

    override fun connect(): Boolean {
        if (isBlacklisted) return false
        val response = exchangeInfo() ?: return false
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

        isConnected = true
        lastUpdated = dp.timeService.epochTime
        return true
    }

    override fun disconnect() {
        isConnected = false
    }

    override fun updateAddress(newAnnouncedAddress: PeerAddress) {
        if (newAnnouncedAddress.protocol != PeerAddress.Protocol.HTTP) return // TODO is this the best way to handle this?
        announcedAddress = newAnnouncedAddress
        // Force re-validate address
        isConnected = false
        dp.peerService.updateAddress(this)
    }

    override fun equals(other: Any?): Boolean {
        return other is Peer && other.address == address
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

        private val RETURNED_JSON_NULL = IllegalStateException("Returned JSON was null")
    }
}
