package brs.services

import brs.api.grpc.proto.PeerApi
import brs.entity.Block
import brs.entity.Transaction
import brs.peer.Peer
import brs.peer.PeerAddress
import com.google.gson.JsonElement

interface PeerService {
    /**
     * TODO
     */
    val getMorePeers: Boolean

    /**
     * All peers, regardless of state
     */
    val allPeers: Collection<Peer>

    /**
     * All peers that are connected
     */
    val activePeers: List<Peer>

    /**
     * TODO
     */
    fun getPeersToBroadcastTo(): MutableList<Peer>

    /**
     * TODO
     */
    fun isSupportedUserAgent(header: String?): Boolean

    /**
     * TODO
     */
    fun shutdown()

    /**
     * If [isConnected] is true, get any connected peers, else get any disconnected peers
     */
    fun getPeers(isConnected: Boolean): Collection<Peer>

    /**
     * TODO
     */
    fun getPeer(peerAddress: String): Peer?

    /**
     * Get or add a peer based on its [remoteAddress]. If it is added, it will not have completed handshake.
     * Intended exclusively for use by peer API servers in order to identify clients.
     * [announcedAddress] should start with a protocol identifier (http:// or grpc://)
     */
    fun getOrAddPeer(remoteAddress: String): Peer

    /**
     * Gets a known peer or adds a new peer with the address [address]
     */
    fun getOrAddPeer(address: PeerAddress)

    /**
     * TODO
     */
    fun sendToSomePeers(block: Block)

    /**
     * TODO
     */
    fun feedingTime(peer: Peer, foodDispenser: (Peer) -> Collection<Transaction>, doneFeedingLog: (Peer, Collection<Transaction>) -> Unit)

    /**
     * If [isConnected] is true, get any connected peer, else get any disconnected peer
     */
    fun getAnyPeer(isConnected: Boolean): Peer?

    /**
     * TODO
     */
    fun notifyListeners(peer: Peer, eventType: Event)

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
    val blacklistingPeriod: Int

    /**
     * TODO
     */
    val configuredBlacklistedPeers: Set<PeerAddress>

    /**
     * TODO
     */
    val myJsonPeerInfoRequest: JsonElement

    /**
     * TODO
     */
    val myProtoPeerInfo: PeerApi.PeerInfo

    /**
     * TODO
     */
    enum class Event {
        BLACKLIST,
        UNBLACKLIST,
        REMOVE,
        ADDED_ACTIVE_PEER,
        NEW_PEER
    }

    val myPlatform: String
    val myAddress: String
    val announcedAddress: PeerAddress?
    val shareMyAddress: Boolean
}
