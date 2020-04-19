package brs.services.impl

import brs.Burst
import brs.api.grpc.proto.PeerApi
import brs.entity.Block
import brs.entity.DependencyProvider
import brs.entity.Transaction
import brs.objects.Constants.MIN_VERSION
import brs.objects.Props
import brs.objects.Props.P2P_SEND_TO_LIMIT
import brs.peer.GrpcPeerImpl
import brs.peer.HttpPeerImpl
import brs.peer.Peer
import brs.peer.Peer.Companion.isHigherOrEqualVersion
import brs.peer.PeerAddress
import brs.services.PeerService
import brs.services.RepeatingTask
import brs.services.Task
import brs.services.TaskType
import brs.util.Listeners
import brs.util.UPnPUtils
import brs.util.Version
import brs.util.delegates.Atomic
import brs.util.json.JSON.prepareRequest
import brs.util.json.toJsonString
import brs.util.logging.safeDebug
import brs.util.logging.safeInfo
import brs.util.logging.safeTrace
import brs.util.misc.countFilterResults
import brs.util.sync.Mutex
import brs.util.sync.withLock
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.bitlet.weupnp.GatewayDevice
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.ThreadLocalRandom

class PeerServiceImpl(private val dp: DependencyProvider) : PeerService {
    private val bootstrapPeers : Set<PeerAddress> = if (dp.propertyService.get(Props.DEV_OFFLINE)) emptySet() else
        dp.propertyService.get(if (dp.propertyService.get(Props.DEV_TESTNET)) Props.DEV_P2P_BOOTSTRAP_PEERS else Props.P2P_BOOTSTRAP_PEERS)
            .mapNotNull { PeerAddress.parse(dp, it) }
            .toMutableSet()

    override val configuredBlacklistedPeers = dp.propertyService.get(Props.P2P_BLACKLISTED_PEERS)
        .mapNotNull { PeerAddress.parse(dp, it) }
        .toSet()

    override val shareMyAddress = dp.propertyService.get(Props.P2P_SHARE_MY_ADDRESS) && !dp.propertyService.get(Props.DEV_OFFLINE)
    private val httpPort = if (dp.propertyService.get(Props.DEV_TESTNET)) 7123 else dp.propertyService.get(Props.P2P_PORT)
    private val grpcPort = dp.propertyService.get(if (dp.propertyService.get(Props.DEV_TESTNET)) Props.DEV_P2P_V2_PORT else Props.P2P_V2_PORT)
    private val gateway: GatewayDevice? = if (shareMyAddress && dp.propertyService.get(Props.P2P_UPNP)) UPnPUtils.setupUpnp(dp, httpPort, grpcPort) else null

    private val numberOfBootstrapPeersToConnect = dp.propertyService.get(Props.P2P_NUM_BOOTSTRAP_CONNECTIONS).coerceAtMost(bootstrapPeers.size)
    private var connectToBootstrapPeersFinished by Atomic(numberOfBootstrapPeersToConnect == 0)

    override val blacklistingPeriod = dp.propertyService.get(Props.P2P_BLACKLISTING_TIME_MS)
    override val getMorePeers = dp.propertyService.get(Props.P2P_GET_MORE_PEERS)
    override val myPlatform = dp.propertyService.get(Props.P2P_MY_PLATFORM)

    override val myAddress: String = if (gateway != null && dp.propertyService.get(Props.P2P_MY_ADDRESS).isBlank()) {
        try {
            gateway.externalIPAddress
        } catch (e: Exception) {
            logger.safeInfo { "Can't get Gateway's IP address" }
            ""
        }
    } else dp.propertyService.get(Props.P2P_MY_ADDRESS)

    override val myAnnouncedAddress = PeerAddress.parse(dp, myAddress.trim(), defaultProtocol = PeerAddress.Protocol.GRPC)
    private val maxNumberOfConnectedPeers = dp.propertyService.get(Props.P2P_MAX_CONNECTIONS)
    private val sendToPeersLimit = dp.propertyService.get(P2P_SEND_TO_LIMIT)
    private val savePeers: Boolean
    private val getMorePeersThreshold = dp.propertyService.get(Props.P2P_GET_MORE_PEERS_THRESHOLD).coerceAtLeast(maxNumberOfConnectedPeers)

    override val myJsonPeerInfoRequest: JsonElement = run {
        val json = JsonObject()
        if (myAnnouncedAddress != null) {
            json.addProperty("announcedAddress", myAnnouncedAddress.toString())
        }
        json.addProperty("application", Burst.APPLICATION)
        json.addProperty("version", Burst.VERSION.toString())
        json.addProperty("platform", this.myPlatform)
        json.addProperty("shareAddress", this.shareMyAddress)
        logger.safeDebug { "My peer info: ${json.toJsonString()}" }
        json.addProperty("requestType", "getInfo")
        prepareRequest(json)
    }

    override val myProtoPeerInfo: PeerApi.PeerInfo = PeerApi.PeerInfo.newBuilder()
        .setApplication(Burst.APPLICATION)
        .setVersion(Burst.VERSION.toString())
        .setPlatform(this.myPlatform)
        .setShareAddress(this.shareMyAddress)
        .setAnnouncedAddress(myAnnouncedAddress?.toString() ?: "")
        .build()

    private val listeners = Listeners<Peer, PeerService.Event>()

    /**
     * All peers, identified by their actual remote address as they may not announce an address
     */
    private val peers = ConcurrentHashMap<String, Peer>()

    /**
     * A directory mapping parsed peer addresses to their remote address
     */
    private val remoteAddressCache = ConcurrentHashMap<PeerAddress, String>()

    override val allPeers get() = peers.values

    init {
        val usePeersDb = dp.propertyService.get(Props.P2P_USE_PEERS_DB) && !dp.propertyService.get(Props.DEV_OFFLINE)
        savePeers = usePeersDb && dp.propertyService.get(Props.P2P_SAVE_PEERS)

        dp.taskSchedulerService.runBeforeStart {
            if (bootstrapPeers.isNotEmpty()) {
                loadPeers(bootstrapPeers)
            }
            if (usePeersDb) {
                logger.safeDebug { "Loading known peers from the database..." }
                loadPeers(dp.db.peerDb.loadPeers().mapNotNull { PeerAddress.parse(dp, it) })
            }
        }
    }

    private val peerUnBlacklistingThread: Task = {
        try {
            val curTime = System.currentTimeMillis()
            for (peer in peers.values) {
                peer.updateBlacklistedStatus(curTime)
            }
        } catch (e: Exception) {
            logger.safeDebug(e) { "Error un-blacklisting peer" }
        }
    }

    private fun updateSavedPeers() {
        dp.db.peerDb.updatePeers(peers.values
            .filter { peer -> !peer.isBlacklisted && !bootstrapPeers.contains(peer.announcedAddress) && peer.isHigherOrEqualVersionThan(MIN_VERSION) }
            .map { it.announcedAddress.toString() })
    }

    private val peerValidationTask: Task = {
        val now = dp.timeService.epochTime
        for (peer in peers.values) {
            if (peer.isConnected && now - peer.lastHandshakeTime > 3600 && (!peer.connect() || !peer.isHigherOrEqualVersionThan(MIN_VERSION) || !peer.isConnected && !peer.isBlacklisted && peers.size > maxNumberOfConnectedPeers)) {
                removePeer(peer)
            }
        }
    }

    private val peerConnectingTask: RepeatingTask = task@{
        val disconnectedPeer = getAnyPeer(isConnected = false)
        // If we have a peer to connect and we are allowed to connect to more peers
        if (disconnectedPeer != null && peers.values.countFilterResults { it.isConnected } < maxNumberOfConnectedPeers) {
            logger.safeDebug { "Connecting to peer..." }
            if (disconnectedPeer.connect()) {
                // They've connected! Disconnect them if they do not meet the
                // version requirements, or if they are not actually connected.
                if (!disconnectedPeer.isHigherOrEqualVersionThan(MIN_VERSION) || !disconnectedPeer.isConnected) {
                    removePeer(disconnectedPeer)
                }
            } else {
                // We want to remove peers that cannot connect,
                // but we mustn't remove them if they are blacklisted
                // or we'll forget their blacklist and they could connect again.
                if (disconnectedPeer.isBlacklisted) {
                    logger.safeDebug { "Could not connect to peer, and it is blacklisted." }
                    disconnectedPeer.disconnect()
                } else {
                    logger.safeDebug { "Could not connect to peer, removing peer." }
                    removePeer(disconnectedPeer)
                }
            }
            return@task true
        } else {
            return@task false
        }
    }

    private val getMorePeersTask: RepeatingTask = task@{
        try {
            // Don't get more peers if we have the threshold
            if (peers.size >= getMorePeersThreshold) return@task false

            val peer = getAnyPeer(isConnected = true) ?: return@task false

            // Ask the peer for its peers
            val newPeers = peer.getPeers() ?: return@task true
            if (!newPeers.isEmpty()) {
                for (newPeerAddress in newPeers) {
                    getOrAddPeer(newPeerAddress)
                }
            }

            // Send the peer our peers
            val myPeers = allPeers.filter { !it.isBlacklisted
                && it.isConnected && it.shareAddress
                && !newPeers.contains(it.announcedAddress)
                && it.announcedAddress != peer.announcedAddress
                && it.isHigherOrEqualVersionThan(MIN_VERSION) }
                .mapNotNull { it.announcedAddress }
            if (myPeers.isNotEmpty()) {
                peer.addPeers(myPeers)
            }
        } catch (e: Exception) {
            logger.safeDebug(e) { "Error requesting peers from a peer" }
        }
        return@task true
    }

    init {
        if (!dp.propertyService.get(Props.DEV_OFFLINE)) {
            dp.taskSchedulerService.scheduleTask(TaskType.IO, peerConnectingTask)
            dp.taskSchedulerService.scheduleTaskWithDelay(TaskType.IO, 0, 1000, peerUnBlacklistingThread)
            dp.taskSchedulerService.scheduleTaskWithDelay(TaskType.IO, 0, 1000, peerValidationTask)
            if (getMorePeers) {
                dp.taskSchedulerService.scheduleTask(TaskType.IO, getMorePeersTask)
            }
        }
    }

    override val activePeers: List<Peer>
        get() = peers.values.filter { it.isConnected }

    private val processingQueue = mutableListOf<Peer>()
    private val beingProcessed = mutableListOf<Peer>()
    private val processingMutex = Mutex()

    override fun getPeersToBroadcastTo(): MutableList<Peer> {
        val peersToBroadcastTo = mutableListOf<Peer>()
        var numberOfPeersLeftToAdd = dp.propertyService.get(P2P_SEND_TO_LIMIT)

        for (peer in activePeers) {
            if (peerEligibleForSending(peer) && numberOfPeersLeftToAdd > 0) {
                peersToBroadcastTo.add(peer)
                numberOfPeersLeftToAdd--
            }
        }

        return peersToBroadcastTo
    }

    override fun isSupportedUserAgent(header: String?): Boolean {
        return if (header == null || header.isEmpty() || !header.trim().startsWith("BRS/")) {
            false
        } else {
            try {
                isHigherOrEqualVersion(MIN_VERSION, Version.parse(header.trim().substring("BRS/".length)))
            } catch (e: IllegalArgumentException) {
                false
            }
        }
    }

    /**
     * Loads peers from a list of addresses
     */
    private fun loadPeers(addresses: Collection<PeerAddress>) {
        for (address in addresses) {
            getOrAddPeer(address)
        }
    }

    override fun shutdown() {
        if (gateway != null) {
            try {
                gateway.deletePortMapping(httpPort, "TCP")
                gateway.deletePortMapping(grpcPort, "TCP")
            } catch (e: Exception) {
                logger.safeInfo(e) { "Failed to remove UPnP rules from gateway" }
            }
        }
    }

    override fun notifyListeners(peer: Peer, eventType: PeerService.Event) {
        this.listeners.accept(eventType, peer)
    }

    override fun getPeers(isConnected: Boolean): Collection<Peer> {
        val peerList = mutableListOf<Peer>()
        for (peer in peers.values) {
            if (peer.isConnected == isConnected) {
                peerList.add(peer)
            }
        }
        return peerList
    }

    override fun getPeer(peerAddress: String): Peer? {
        return peers[peerAddress]
    }

    override fun getOrAddPeer(remoteAddress: String): Peer {
        val cleanRemoteAddress = remoteAddress.trim()
        var peer = peers[cleanRemoteAddress]
        if (peer != null) return peer
        if (remoteAddressCache.containsValue(remoteAddress)) {
            peer = peers[remoteAddressCache.values.find { it == remoteAddress }]
            if (peer != null) return peer
        }
        peer = if (cleanRemoteAddress.startsWith("grpc://")) {
            GrpcPeerImpl(dp, remoteAddress, null)
        } else {
            HttpPeerImpl(dp, remoteAddress, null)
        }
        peers[cleanRemoteAddress] = peer
        listeners.accept(PeerService.Event.NEW_PEER, peer)
        updateSavedPeers()
        return peer
    }

    override fun getOrAddPeer(address: PeerAddress) {
        if (this.myAnnouncedAddress == address) return
        var remoteAddress = remoteAddressCache[address]
        if (remoteAddress != null) {
            val peer = peers[remoteAddress]
            if (peer != null) return
        }
        remoteAddress = address.toString()
        var peer = peers[remoteAddress]
        if (peer != null) return
        peer = when (address.protocol) {
            PeerAddress.Protocol.HTTP -> HttpPeerImpl(dp, remoteAddress, address)
            PeerAddress.Protocol.GRPC -> GrpcPeerImpl(dp, remoteAddress, address)
        }
        remoteAddressCache[address] = remoteAddress
        peers[remoteAddress] = peer
        listeners.accept(PeerService.Event.NEW_PEER, peer)
        updateSavedPeers()
    }

    private fun removePeer(peer: Peer) {
        peer.disconnect()
        peers.remove(peer.remoteAddress)
        notifyListeners(peer, PeerService.Event.REMOVE)
        updateSavedPeers()
    }

    override fun updateAddress(peer: Peer) {
        val oldAddress = remoteAddressCache.put(peer.announcedAddress ?: return, peer.remoteAddress)
        if (oldAddress != null && peer.remoteAddress != oldAddress) {
            val oldPeer = peers.remove(oldAddress)
            if (oldPeer != null) {
                this.notifyListeners(oldPeer, PeerService.Event.REMOVE)
            }
        }
    }

    override fun sendToSomePeers(block: Block) {
        dp.taskSchedulerService.run {
            var successful = 0
            val expectedResponses = mutableListOf<Future<Boolean?>>()
            for (peer in peers.values) {
                if (peerEligibleForSending(peer)) {
                    val deferred = dp.taskSchedulerService.async(TaskType.IO) { peer.sendBlock(block) }
                    expectedResponses.add(deferred)
                }
                if (expectedResponses.size >= sendToPeersLimit - successful) {
                    for (expectedResponse in expectedResponses) {
                        try {
                            val response = expectedResponse.get()
                            if (response == true) {
                                successful += 1
                            }
                        } catch (e: ExecutionException) {
                            logger.safeDebug(e) { "Error in sendToSomePeers" }
                        }
                    }
                    expectedResponses.clear()
                }
                if (successful >= sendToPeersLimit) {
                    return@run
                }
            }
        }
    }

    override fun feedingTime(
        peer: Peer,
        foodDispenser: (Peer) -> Collection<Transaction>,
        doneFeedingLog: (Peer, Collection<Transaction>) -> Unit
    ) {
        processingMutex.withLock<Unit> {
            when {
                !beingProcessed.contains(peer) -> {
                    beingProcessed.add(peer)
                    dp.taskSchedulerService.run { feedPeer(peer, foodDispenser, doneFeedingLog) }
                }
                !processingQueue.contains(peer) -> processingQueue.add(peer)
            }
        }
    }

    private fun feedPeer(peer: Peer, foodDispenser: (Peer) -> Collection<Transaction>, doneFeedingLog: (Peer, Collection<Transaction>) -> Unit) {
        val transactionsToSend = foodDispenser(peer)

        if (transactionsToSend.isNotEmpty()) {
            logger.safeTrace { "Feeding ${peer.announcedAddress} ${transactionsToSend.size} transactions" }
            peer.sendUnconfirmedTransactions(transactionsToSend)
        } else {
            logger.safeTrace { "No need to feed ${peer.announcedAddress}" }
        }

        processingMutex.withLock {
            beingProcessed.remove(peer)

            if (processingQueue.contains(peer)) {
                processingQueue.remove(peer)
                beingProcessed.add(peer)
                feedPeer(peer, foodDispenser, doneFeedingLog)
            }
        }
    }

    private fun peerEligibleForSending(peer: Peer): Boolean {
        return (peer.isHigherOrEqualVersionThan(MIN_VERSION)
                && !peer.isBlacklisted
                && peer.isConnected)
    }

    private fun announceConnectedToBootstrapPeers() {
        logger.safeInfo { "Finished connecting to $numberOfBootstrapPeersToConnect bootstrap peers." }
        val webSchema = if (dp.propertyService.get(Props.API_SSL)) "https" else "http"
        var webHost = dp.propertyService.get(Props.API_LISTEN)
        if (webHost == "0.0.0.0") webHost = "localhost"
        val webPort = dp.propertyService.get(if (dp.propertyService.get(Props.DEV_TESTNET)) Props.DEV_API_PORT else Props.API_PORT)
        logger.safeInfo { "You can open your Burst Wallet in your favorite browser with: $webSchema://$webHost:$webPort" }
    }

    override fun getAnyPeer(isConnected: Boolean): Peer? {
        if (!connectToBootstrapPeersFinished) {
            var bootstrapPeersConnected = 0
            for (peer in peers.values) {
                if (bootstrapPeers.contains(peer.announcedAddress) && peer.isConnected) {
                    bootstrapPeersConnected++
                }
            }
            if (bootstrapPeersConnected >= numberOfBootstrapPeersToConnect) {
                connectToBootstrapPeersFinished = true
                announceConnectedToBootstrapPeers()
            }
        }

        val selectedPeers = peers.values.filter { peer -> !peer.isBlacklisted && peer.isConnected == isConnected && peer.shareAddress && (connectToBootstrapPeersFinished || peer.isConnected || bootstrapPeers.contains(peer.announcedAddress)) }
        return if (selectedPeers.isNotEmpty()) selectedPeers[ThreadLocalRandom.current().nextInt(selectedPeers.size)] else null
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PeerServiceImpl::class.java)
    }
}
