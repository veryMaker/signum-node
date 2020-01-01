package brs.services

import brs.entity.Block
import brs.entity.Transaction
import brs.peer.Peer
import com.google.gson.JsonElement
import com.google.gson.JsonObject

interface PeerService {
    /**
     * TODO
     */
    val getMorePeers: Boolean

    /**
     * TODO
     */
    val allPeers: Collection<Peer>

    /**
     * TODO
     */
    val activePeers: List<Peer>

    /**
     * TODO
     */
    val allActivePriorityPlusSomeExtraPeers: MutableList<Peer>

    /**
     * TODO
     */
    fun isSupportedUserAgent(header: String?): Boolean

    /**
     * TODO
     */
    fun shutdown()

    /**
     * TODO
     */
    fun getPeers(state: Peer.State): Collection<Peer>

    /**
     * TODO
     */
    fun getPeer(peerAddress: String): Peer?

    /**
     * TODO
     */
    fun addPeer(announcedAddress: String?): Peer?

    /**
     * TODO
     */
    fun sendToSomePeers(block: Block)

    /**
     * TODO
     */
    fun readUnconfirmedTransactions(peer: Peer): JsonObject?

    /**
     * TODO
     */
    fun feedingTime(
        peer: Peer,
        foodDispenser: (Peer) -> Collection<Transaction>,
        doneFeedingLog: (Peer, Collection<Transaction>) -> Unit
    )

    /**
     * TODO
     */
    fun getAnyPeer(state: Peer.State): Peer?

    /**
     * TODO
     */
    fun notifyListeners(peer: Peer, eventType: Event)

    /**
     * TODO
     */
    val myPeerInfoResponse: JsonElement

    /**
     * TODO
     */
    val communicationLoggingMask: Int

    /**
     * TODO
     */
    val readTimeout: Int

    /**
     * TODO
     */
    val connectTimeout: Int

    /**
     * TODO
     */
    fun removePeer(peer: Peer)

    /**
     * TODO
     */
    fun updateAddress(peer: Peer)

    /**
     * TODO
     */
    fun addPeer(address: String, announcedAddress: String?): Peer?

    /**
     * TODO
     */
    val blacklistingPeriod: Int

    /**
     * TODO
     */
    val knownBlacklistedPeers: Set<String>

    /**
     * TODO
     */
    fun normalizeHostAndPort(address: String?): String?

    /**
     * TODO
     */
    val myPeerInfoRequest: JsonElement

    /**
     * TODO
     */
    val rebroadcastPeers: Set<String>

    /**
     * TODO
     */
    val wellKnownPeers: Set<String>

    /**
     * TODO
     */
    enum class Event {
        BLACKLIST,
        UNBLACKLIST,
        REMOVE,
        DOWNLOADED_VOLUME,
        UPLOADED_VOLUME,
        ADDED_ACTIVE_PEER,
        CHANGED_ACTIVE_PEER,
        NEW_PEER
    }
}