package brs.peer

import brs.Burst
import brs.api.grpc.ProtoBuilder
import brs.api.grpc.proto.BrsPeerServiceGrpc
import brs.api.grpc.proto.PeerApi
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
import brs.util.delegates.Atomic
import brs.util.delegates.AtomicLateinit
import brs.util.delegates.AtomicWithOverride
import brs.util.logging.safeDebug
import brs.util.logging.safeError
import brs.util.logging.safeInfo
import brs.util.logging.safeWarn
import com.google.protobuf.Empty
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import org.slf4j.LoggerFactory
import java.io.IOException
import java.math.BigInteger
import java.sql.SQLException
import java.util.concurrent.TimeUnit

typealias PeerConnection = BrsPeerServiceGrpc.BrsPeerServiceBlockingStub

class GrpcPeerImpl(
    private val dp: DependencyProvider,
    override val remoteAddress: String,
    address: PeerAddress?
) : Peer {
    private val parsedRemoteAddress = PeerAddress.parse(dp, remoteAddress, defaultProtocol = PeerAddress.Protocol.GRPC)

    override val address: PeerAddress
        get() = announcedAddress ?: parsedRemoteAddress ?: error("Could not find peer's address")

    private var announcedAddress by Atomic<PeerAddress?>(null)

    init {
        if (address != null) {
            require(address.protocol == PeerAddress.Protocol.GRPC) { "Protocol must be GRPC" }
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
    override val isConnected: Boolean
        get() = connection != null && connection?.second?.isShutdown == false

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
        shutdownConnection()
        dp.peerService.notifyListeners(this, PeerService.Event.BLACKLIST)
    }

    override fun updateBlacklistedStatus(curTime: Long) {
        if (blacklistingTime > 0 && blacklistingTime + dp.peerService.blacklistingPeriod <= curTime) {
            blacklistingTime = 0
            dp.peerService.notifyListeners(this, PeerService.Event.UNBLACKLIST)
        }
    }

    private var connection: Pair<PeerConnection, ManagedChannel>? by Atomic(null)

    private fun getConnection(): PeerConnection? {
        return connection?.first
    }

    private fun openConnection() {
        val channel = ManagedChannelBuilder.forAddress(address.host, address.port)
            .usePlaintext()
            .maxInboundMessageSize(1024 * 1024 * 100) // 100MB - way too big TODO reduce when alpha5 bug where peer sends too much is fixed
            .build()
        val newConnection = BrsPeerServiceGrpc.newBlockingStub(channel)
        connection = Pair(newConnection, channel)
    }

    private fun shutdownConnection() {
        connection?.second?.let { channel ->
            channel.shutdown()
            channel.awaitTermination(10, TimeUnit.SECONDS)
            while (!channel.isTerminated) {
                channel.shutdownNow()
                channel.awaitTermination(10, TimeUnit.SECONDS)
            }
        }
        connection = null
    }

    private inline fun <T: Any> handlePeerError(errorMessage: String, action: (PeerConnection) -> T): T? {
        try {
            val connection = getConnection()
            if (connection != null) {
                return action(connection)
            } else {
                logger.safeWarn { "$errorMessage: Peer was not connected" }
            }
        } catch (e: StatusRuntimeException) {
            logger.safeWarn { "$errorMessage: Peer Returned an Error: \"${e.message?.replace("ABORTED: ", "")}\"" }
        } catch (e: Exception) {
            logger.safeWarn(e) { errorMessage }
        }
        return null
    }

    override fun exchangeInfo(): PeerInfo? {
        return handlePeerError("Error exchanging info with peer") { connection ->
            PeerInfo.fromProto(connection.exchangeInfo(dp.peerService.myProtoPeerInfo))
        }
    }

    override fun getCumulativeDifficulty(): Pair<BigInteger, Int>? {
        return handlePeerError("Error getting cumulative difficulty from peer") { connection ->
            val response = connection.getCumulativeDifficulty(Empty.getDefaultInstance())
            Pair(BigInteger(response.cumulativeDifficulty.toByteArray()), response.blockchainHeight)
        }
    }

    override fun getUnconfirmedTransactions(): Collection<Transaction>? {
        return handlePeerError("Error getting unconfirmed transactions from peer") { connection ->
            connection.getUnconfirmedTransactions(Empty.getDefaultInstance()).transactionsList.map { ProtoBuilder.parseRawTransaction(dp, it) }
        }
    }

    override fun getMilestoneBlockIds(): Pair<Collection<Long>, Boolean>? {
        return handlePeerError("Error getting milestone block IDs") { connection ->
            val response = connection.getMilestoneBlockIds(PeerApi.GetMilestoneBlockIdsRequest.newBuilder()
                .setLastBlockId(dp.downloadCacheService.getLastBlockId())
                .build())
            Pair(response.milestoneBlockIdsList, response.last)
        }
    }

    override fun getMilestoneBlockIds(lastMilestoneBlockId: Long): Pair<Collection<Long>, Boolean>? {
        return handlePeerError("Error getting milestone block IDs") { connection ->
            val response = connection.getMilestoneBlockIds(PeerApi.GetMilestoneBlockIdsRequest.newBuilder()
                .setLastMilestoneBlockId(lastMilestoneBlockId)
                .build())
            Pair(response.milestoneBlockIdsList, response.last)
        }
    }

    override fun sendUnconfirmedTransactions(transactions: Collection<Transaction>) {
        handlePeerError("Error sending unconfirmed transactions to peer") { connection ->
            connection.addUnconfirmedTransactions(PeerApi.RawTransactions.newBuilder()
                .addAllTransactions(transactions.map { ProtoBuilder.buildRawTransaction(it) })
                .build())
        }
    }

    override fun getNextBlocks(lastBlockId: Long): Collection<Block>? {
        return handlePeerError("Error getting next blocks from peer") { connection ->
            connection.getNextBlocks(PeerApi.GetBlocksAfterRequest.newBuilder()
                .setBlockId(lastBlockId)
                .build())
                .blocksList
                .asSequence()
                .take(Constants.MAX_PEER_RECEIVED_BLOCKS)
                .map { ProtoBuilder.parseRawBlock(dp, it) }
                .toList()
        }
    }

    override fun getNextBlockIds(lastBlockId: Long): Collection<Long>? {
        return handlePeerError("Error getting next block IDs from peer") { connection ->
            connection.getNextBlockIds(PeerApi.GetBlocksAfterRequest.newBuilder()
                .setBlockId(lastBlockId)
                .build())
                .blockIdsList
                .asSequence()
                .take(Constants.MAX_PEER_RECEIVED_BLOCKS)
                .toList()
        }
    }

    override fun addPeers(announcedAddresses: Collection<PeerAddress>) {
        handlePeerError("Error sending peers to peer") { connection ->
            connection.addPeers(PeerApi.Peers.newBuilder()
                .addAllAddresses(announcedAddresses.map { it.toString() })
                .build())
        }
    }

    override fun getPeers(): Collection<PeerAddress>? {
        return handlePeerError("Error getting peers from peer") { connection ->
            connection.getPeers(Empty.getDefaultInstance())
                .addressesList
                .mapNotNull { PeerAddress.parse(dp, it) }
        }
    }

    override fun sendBlock(block: Block): Boolean {
        return handlePeerError("Error sending block to peers") { connection ->
            connection.addBlock(PeerApi.ProcessBlockRequest.newBuilder()
                .setPreviousBlockId(block.previousBlockId)
                .setBlock(ProtoBuilder.buildRawBlock(block))
                .build())
            true
        } ?: false
    }

    override fun connect(): Boolean {
        if (isBlacklisted) return false
        openConnection()
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
            announcedAddress = parsedRemoteAddress
        }

        lastUpdated = dp.timeService.epochTime
        return true
    }

    override fun disconnect() {
        shutdownConnection()
    }

    override fun updateAddress(newAnnouncedAddress: PeerAddress) {
        if (newAnnouncedAddress.protocol != PeerAddress.Protocol.GRPC) return // TODO is this the best way to handle this?
        announcedAddress = newAnnouncedAddress
        // Force re-validate address
        shutdownConnection()
        dp.peerService.updateAddress(this)
    }

    override fun hashCode(): Int {
        return address.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is Peer && other.address == address
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GrpcPeerImpl::class.java)
    }
}
