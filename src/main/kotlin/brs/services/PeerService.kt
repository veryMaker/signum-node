package brs.services

import brs.entity.Block
import brs.entity.Transaction
import brs.peer.Peer
import com.google.gson.JsonElement
import com.google.gson.JsonObject

interface PeerService {
    val getMorePeers: Boolean
    val allPeers: Collection<Peer>
    val activePeers: List<Peer>
    val allActivePriorityPlusSomeExtraPeers: MutableList<Peer>
    fun isSupportedUserAgent(header: String?): Boolean
    fun shutdown()
    fun getPeers(state: Peer.State): Collection<Peer>
    fun getPeer(peerAddress: String): Peer?
    fun addPeer(announcedAddress: String?): Peer?
    fun sendToSomePeers(block: Block)
    fun readUnconfirmedTransactions(peer: Peer): JsonObject?
    fun feedingTime(peer: Peer, foodDispenser: (Peer) -> Collection<Transaction>, doneFeedingLog: (Peer, Collection<Transaction>) -> Unit)
    fun getAnyPeer(state: Peer.State): Peer?
    fun notifyListeners(peer: Peer, eventType: Event)
    val myPeerInfoResponse: JsonElement
    val communicationLoggingMask: Int
    val readTimeout: Int
    val connectTimeout: Int
    fun removePeer(peer: Peer)
    fun updateAddress(peer: Peer)
    fun addPeer(address: String, announcedAddress: String?): Peer?
    val blacklistingPeriod: Int
    val knownBlacklistedPeers: Set<String>
    fun normalizeHostAndPort(address: String?): String?
    val myPeerInfoRequest: JsonElement
    val rebroadcastPeers: Set<String>
    val wellKnownPeers: Set<String>
    enum class Event {
        // TODO remove unused events
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