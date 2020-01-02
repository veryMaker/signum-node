package brs.peer

import brs.api.grpc.proto.BrsApi
import brs.entity.Block
import brs.entity.PeerInfo
import brs.entity.Transaction
import brs.util.Version
import java.math.BigInteger

interface Peer : Comparable<Peer> {
    val peerAddress: String

    var announcedAddress: String?

    var state: State

    var version: Version

    var application: String

    var platform: String

    val software: String

    val port: Int

    val isWellKnown: Boolean

    val isRebroadcastTarget: Boolean

    val isBlacklisted: Boolean

    val isAtLeastMyVersion: Boolean

    val downloadedVolume: Long

    val uploadedVolume: Long

    var lastUpdated: Int

    fun connect(currentTime: Int)

    fun updateUploadedVolume(volume: Long)

    fun isHigherOrEqualVersionThan(version: Version): Boolean

    fun blacklist(cause: Exception, description: String)

    fun blacklist(description: String)

    fun blacklist()

    fun unBlacklist()

    fun updateBlacklistedStatus(curTime: Long)

    fun remove()

    fun updateDownloadedVolume(volume: Long)

    /**
     * Send the peer our [PeerInfo] and returns theirs
     * @throws Exception if unsuccessful
     */
    fun exchangeInfo(): PeerInfo

    /**
     * Get the peer's cumulative difficulty and current blockchain height
     * @return A pair with first value of the Cumulative Difficulty and second value of the Blockchain Height
     * @throws Exception if unsuccessful
     */
    fun getCumulativeDifficulty(): Pair<BigInteger, Int>

    /**
     * Get any unconfirmed transactions the peer has for us
     * @return A list of unconfirmed transactions
     * @throws Exception if unsuccessful
     */
    fun getUnconfirmedTransactions(): Collection<Transaction>

    /**
     * TODO improve doc
     * Get milestone block IDs from a peer since the last block ID in the download cache
     * @return A pair with first value of the milestone block IDs and second value being whether this is the last block ID
     * @throws Exception if unsuccessful
     */
    fun getMilestoneBlockIds(): Pair<Collection<Long>, Boolean>

    /**
     * TODO improve doc
     * Get milestone block IDs from a peer since [lastMilestoneBlockId]
     * @return A pair with first value of the milestone block IDs and second value being whether this is the last block ID
     * @throws Exception if unsuccessful
     */
    fun getMilestoneBlockIds(lastMilestoneBlockId: Long): Pair<Collection<Long>, Boolean>

    /**
     * Sends the unconfirmed transactions to the peer.
     * limited to the first [brs.objects.Constants.MAX_PEER_RECEIVED_BLOCKS] that the peer returns.
     * @throws Exception if unsuccessful
     */
    fun sendUnconfirmedTransactions(transactions: Collection<Transaction>)

    /**
     * Gets the blocks after [lastBlockId] from the peer,
     * limited to the first [brs.objects.Constants.MAX_PEER_RECEIVED_BLOCKS] that the peer returns.
     * @throws Exception if unsuccessful
     */
    fun getNextBlocks(lastBlockId: Long): Collection<Block>

    /**
     * Gets the block IDs after [lastBlockId] from the peer
     * @throws Exception if unsuccessful
     */
    fun getNextBlockIds(lastBlockId: Long): Collection<Long>

    /**
     * Notifies this peer of the other peers
     * @param announcedAddresses The announced addresses to notify this peer of
     * @throws Exception if unsuccessful
     */
    fun addPeers(announcedAddresses: Collection<String>)

    /**
     * Get new peer addresses from this peer
     * @throws Exception if unsuccessful
     */
    fun getPeers(): Collection<String>

    /**
     * Sends [block] to the peer to be added
     * @return Whether the peer accepted the [block]
     * @throws Exception if unsuccessful
     */
    fun sendBlock(block: Block): Boolean

    var shareAddress: Boolean

    enum class State {
        NON_CONNECTED, CONNECTED, DISCONNECTED;

        fun toProtobuf(): BrsApi.PeerState {
            return when (this) {
                NON_CONNECTED -> BrsApi.PeerState.NON_CONNECTED
                CONNECTED -> BrsApi.PeerState.CONNECTED
                DISCONNECTED -> BrsApi.PeerState.NON_CONNECTED
            }
        }

        companion object {
            fun fromProtobuf(peer: BrsApi.PeerState): State? {
                return when (peer) {
                    BrsApi.PeerState.NON_CONNECTED -> NON_CONNECTED
                    BrsApi.PeerState.CONNECTED -> CONNECTED
                    BrsApi.PeerState.DISCONNECTED -> DISCONNECTED
                    else -> null
                }
            }
        }
    }

    companion object {
        fun isHigherOrEqualVersion(ourVersion: Version?, possiblyLowerVersion: Version?): Boolean {
            return ourVersion != null && possiblyLowerVersion?.isGreaterThanOrEqualTo(ourVersion) ?: false
        }
    }
}
