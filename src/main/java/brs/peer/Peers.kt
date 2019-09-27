package brs.peer

import brs.*
import brs.Constants.MIN_VERSION
import brs.db.sql.Db
import brs.peer.Peer.Companion.isHigherOrEqualVersion
import brs.props.Props
import brs.props.Props.P2P_ENABLE_TX_REBROADCAST
import brs.props.Props.P2P_SEND_TO_LIMIT
import brs.util.*
import brs.util.JSON.prepareRequest
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.bitlet.weupnp.GatewayDevice
import org.bitlet.weupnp.GatewayDiscover
import org.bitlet.weupnp.PortMappingEntry
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.servlet.FilterMapping
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.servlets.DoSFilter
import org.slf4j.LoggerFactory
import org.xml.sax.SAXException
import java.io.IOException
import java.net.InetAddress
import java.net.URI
import java.net.URISyntaxException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Supplier
import javax.xml.parsers.ParserConfigurationException

// TODO this whole class needs refactoring.
// TODO what about next-gen P2P network?
class Peers(private val dp: DependencyProvider) { // TODO interface
    internal var communicationLoggingMask: Int = 0

    private val random = Random()

    internal var wellKnownPeers: Set<String>
    internal var knownBlacklistedPeers: Set<String>

    internal var peerServer: Server? = null
    internal var gateway: GatewayDevice? = null
    internal var port: Int = -1

    private var connectWellKnownFirst: Int = 0
    private var connectWellKnownFinished: Boolean = false

    internal var rebroadcastPeers: Set<String>

    internal var connectTimeout: Int = 0
    internal var readTimeout: Int = 0
    internal var blacklistingPeriod: Int = 0
    internal var getMorePeers: Boolean = false

    private var myPlatform: String? = null
    private var myAddress: String? = null
    private var myPeerServerPort: Int = 0
    private var useUpnp: Boolean = false
    private var shareMyAddress: Boolean = false
    private var maxNumberOfConnectedPublicPeers: Int = 0
    private var sendToPeersLimit: Int = 0
    private var usePeersDb: Boolean = false
    private var savePeers: Boolean = false
    private var getMorePeersThreshold: Int = 0
    private var dumpPeersVersion: String? = null
    private var lastSavedPeers: Int = 0

    internal var myPeerInfoRequest: JsonElement
    internal var myPeerInfoResponse: JsonElement

    private val listeners = Listeners<Peer, Event>()

    private val peers = ConcurrentHashMap<String, Peer>() // Remember, this map type cannot take null keys.
    private val announcedAddresses: MutableMap<String?, String> = ConcurrentHashMap() // Remember, this map type cannot take null keys.

    val allPeers: Collection<Peer> = peers.values

    private val sendBlocksToPeersService = Executors.newCachedThreadPool()
    private val blocksSendingService = Executors.newFixedThreadPool(10)

    private val peerUnBlacklistingThread = {
        try {

            val curTime = System.currentTimeMillis()
            for (peer in peers.values) {
                peer.updateBlacklistedStatus(curTime)
            }

        } catch (e: Exception) {
            logger.debug("Error un-blacklisting peer", e)
        }
    }

    private val peerConnectingThread = object : () -> Unit {
        private val numberOfConnectedPublicPeers: Int
            get() {
                var numberOfConnectedPeers = 0
                for (peer in peers.values) {
                    if (peer.state == Peer.State.CONNECTED) {
                        numberOfConnectedPeers++
                    }
                }
                return numberOfConnectedPeers
            }

        override fun invoke() {
            try {
                var numConnectedPeers = numberOfConnectedPublicPeers
                /*
         * aggressive connection with while loop.
         * if we have connected to our target amount we can exit loop.
         * if peers size is equal or below connected value we have nothing to connect to
         */
                while (!Thread.currentThread().isInterrupted && ThreadPool.running.get()
                        && numConnectedPeers < maxNumberOfConnectedPublicPeers && peers.size > numConnectedPeers) {
                    val peer = getAnyPeer(if (ThreadLocalRandom.current().nextInt(2) == 0) Peer.State.NON_CONNECTED else Peer.State.DISCONNECTED)
                    if (peer != null) {
                        peer.connect(dp.timeService.epochTime)
                        /*
             * remove non connected peer. if peer is blacklisted, keep it to maintain blacklist time.
             * Peers should never be removed if total peers are below our target to prevent total erase of peers
             * if we loose Internet connection
             */

                        if (!peer.isHigherOrEqualVersionThan(MIN_VERSION) || peer.state != Peer.State.CONNECTED && !peer.isBlacklisted && peers.size > maxNumberOfConnectedPublicPeers) {
                            removePeer(peer)
                        } else {
                            numConnectedPeers++
                        }
                    }

                    try {
                        Thread.sleep(1000)
                    } catch (ex: InterruptedException) {
                        Thread.currentThread().interrupt()
                        return
                    }

                }

                val now = dp.timeService.epochTime
                for (peer in peers.values) {
                    if (peer.state == Peer.State.CONNECTED && now - peer.lastUpdated > 3600) {
                        peer.connect(dp.timeService.epochTime)
                        if (!peer.isHigherOrEqualVersionThan(MIN_VERSION) || peer.state != Peer.State.CONNECTED && !peer.isBlacklisted && peers.size > maxNumberOfConnectedPublicPeers) {
                            removePeer(peer)
                        }
                    }
                }

                if (lastSavedPeers != peers.size) {
                    lastSavedPeers = peers.size
                    updateSavedPeers()
                }

            } catch (e: Exception) {
                logger.debug("Error connecting to peer", e)
            }

        }

        private fun updateSavedPeers() {
            val oldPeers = dp.peerDb.loadPeers().toSet()
            val currentPeers = mutableSetOf<String>()
            for (peer in peers.values) {
                if (peer.announcedAddress != null
                        && !peer.isBlacklisted
                        && !peer.isWellKnown
                        && peer.isHigherOrEqualVersionThan(MIN_VERSION)) {
                    currentPeers.add(peer.announcedAddress!!)
                }
            }
            val toDelete = oldPeers.toMutableSet()
            toDelete.removeAll(currentPeers)
            try {
                Db.beginTransaction()
                dp.peerDb.deletePeers(toDelete)
                currentPeers.removeAll(oldPeers)
                dp.peerDb.addPeers(currentPeers)
                Db.commitTransaction()
            } catch (e: Exception) {
                Db.rollbackTransaction()
                throw e
            } finally {
                Db.endTransaction()
            }
        }
    }

    private val getMorePeersThread = object : () -> Unit {

        private val getPeersRequest: JsonElement

        private val addedNewPeer = AtomicBoolean(false)

        init {
            val request = JsonObject()
            request.addProperty("requestType", "getPeers")
            getPeersRequest = prepareRequest(request)
        }

        init {
            addListener({ peer -> addedNewPeer.set(true) }, Event.NEW_PEER)
        }

        private fun addListener(listener: (Peer) -> Unit, eventType: Event): Boolean {
            return listeners.addListener(listener, eventType)
        }

        override fun invoke() {
            try {
                /* We do not want more peers if above Threshold but we need enough to
         * connect to selected number of peers
         */
                if (peers.size >= getMorePeersThreshold && peers.size > maxNumberOfConnectedPublicPeers) {
                    return
                }

                val peer = getAnyPeer(Peer.State.CONNECTED) ?: return
                val response = peer.send(getPeersRequest) ?: return
                val peersJson = JSON.getAsJsonArray(response.get("peers"))
                val addedAddresses = HashSet<String>()
                if (peersJson != null && !peersJson.isEmpty()) {
                    for (announcedAddress in peersJson) {
                        if (addPeer(JSON.getAsString(announcedAddress)) != null) {
                            addedAddresses.add(JSON.getAsString(announcedAddress))
                        }
                    }
                    if (savePeers && addedNewPeer.get()) {
                        addedNewPeer.set(false)
                    }
                }

                val myPeers = JsonArray()
                for (myPeer in allPeers) {
                    if (!myPeer.isBlacklisted && myPeer.announcedAddress != null
                            && myPeer.state == Peer.State.CONNECTED && myPeer.shareAddress
                            && !addedAddresses.contains(myPeer.announcedAddress!!)
                            && myPeer.announcedAddress != peer.announcedAddress
                            && myPeer.isHigherOrEqualVersionThan(MIN_VERSION)) {
                        myPeers.add(myPeer.announcedAddress)
                    }
                }
                //executor shutdown?
                if (Thread.currentThread().isInterrupted) {
                    return
                }

                if (myPeers.size() > 0) {
                    val request = JsonObject()
                    request.addProperty("requestType", "addPeers")
                    request.add("peers", myPeers)
                    peer.send(prepareRequest(request))
                }

            } catch (e: Exception) {
                logger.debug("Error requesting peers from a peer", e)
            }

        }
    }

    val activePeers: List<Peer>
        get() {
            val activePeers = mutableListOf<Peer>()
            for (peer in peers.values) {
                if (peer.state != Peer.State.NON_CONNECTED) {
                    activePeers.add(peer)
                }
            }
            return activePeers
        }

    private val getUnconfirmedTransactionsRequest: JsonElement

    private val utReceivingService = Executors.newCachedThreadPool()

    private val utSendingService = Executors.newCachedThreadPool()

    private val processingQueue = mutableListOf<Peer>()
    private val beingProcessed = mutableListOf<Peer>()

    val allActivePriorityPlusSomeExtraPeers: MutableList<Peer>
        get() {
            val peersActivePriorityPlusSomeExtraPeers = mutableListOf<Peer>()
            var amountExtrasLeft = dp.propertyService.get(P2P_SEND_TO_LIMIT)

            for (peer in peers.values) {
                if (peerEligibleForSending(peer, true)) {
                    if (peer.isRebroadcastTarget) {
                        peersActivePriorityPlusSomeExtraPeers.add(peer)
                    } else if (amountExtrasLeft > 0) {
                        peersActivePriorityPlusSomeExtraPeers.add(peer)
                        amountExtrasLeft--
                    }
                }
            }

            return peersActivePriorityPlusSomeExtraPeers
        }

    fun isSupportedUserAgent(header: String?): Boolean {
        return if (header == null || header.isEmpty() || !header.trim { it <= ' ' }.startsWith("BRS/")) {
            false
        } else {
            try {
                isHigherOrEqualVersion(MIN_VERSION, Version.parse(header.trim { it <= ' ' }.substring("BRS/".length)))
            } catch (e: IllegalArgumentException) {
                false
            }

        }
    }

    enum class Event {
        // TODO remove unused events
        BLACKLIST,
        UNBLACKLIST,
        DEACTIVATE,
        REMOVE,
        DOWNLOADED_VOLUME,
        UPLOADED_VOLUME,
        WEIGHT,
        ADDED_ACTIVE_PEER,
        CHANGED_ACTIVE_PEER,
        NEW_PEER
    }

    init {
        myPlatform = dp.propertyService.get(Props.P2P_MY_PLATFORM)
        myAddress = if (dp.propertyService.get(Props.P2P_MY_ADDRESS).isNotBlank()
            && dp.propertyService.get(Props.P2P_MY_ADDRESS).trim { it <= ' ' }.isEmpty()
            && gateway != null) {
            var externalIPAddress: String? = null
            try {
                externalIPAddress = gateway!!.externalIPAddress
            } catch (e: IOException) {
                logger.info("Can't get gateways IP adress")
            } catch (e: SAXException) {
                logger.info("Can't get gateways IP adress")
            }

            externalIPAddress
        } else dp.propertyService.get(Props.P2P_MY_ADDRESS)

        if (myAddress != null && myAddress!!.endsWith(":$TESTNET_PEER_PORT") && !dp.propertyService.get(Props.DEV_TESTNET)) {
            throw RuntimeException("Port $TESTNET_PEER_PORT should only be used for testnet!!!")
        }
        myPeerServerPort = dp.propertyService.get(Props.P2P_PORT)
        if (myPeerServerPort == TESTNET_PEER_PORT && !dp.propertyService.get(Props.DEV_TESTNET)) {
            throw RuntimeException("Port $TESTNET_PEER_PORT should only be used for testnet!!!")
        }
        useUpnp = dp.propertyService.get(Props.P2P_UPNP)
        shareMyAddress = dp.propertyService.get(Props.P2P_SHARE_MY_ADDRESS) && !dp.propertyService.get(Props.DEV_OFFLINE)

        val json = JsonObject()
        if (myAddress != null && !myAddress!!.isEmpty()) {
            try {
                val uri = URI("http://" + myAddress!!.trim { it <= ' ' })
                val host = uri.host
                val port = uri.port
                if (!dp.propertyService.get(Props.DEV_TESTNET)) {
                    if (port >= 0) {
                        json.addProperty("announcedAddress", myAddress)
                    } else {
                        json.addProperty("announcedAddress", host + if (myPeerServerPort != DEFAULT_PEER_PORT) ":$myPeerServerPort" else "")
                    }
                } else {
                    json.addProperty("announcedAddress", host)
                }
            } catch (e: URISyntaxException) {
                logger.info("Your announce address is invalid: {}", myAddress)
                throw RuntimeException(e.toString(), e)
            }

        }

        json.addProperty("application", Burst.APPLICATION)
        json.addProperty("version", Burst.VERSION.toString())
        json.addProperty("platform", this.myPlatform)
        json.addProperty("shareAddress", this.shareMyAddress)
        if (logger.isDebugEnabled) {
            logger.debug("My peer info: {}", json.toJsonString())
        }
        myPeerInfoResponse = json.cloneJson()
        json.addProperty("requestType", "getInfo")
        myPeerInfoRequest = prepareRequest(json)


        rebroadcastPeers = if (dp.propertyService.get(P2P_ENABLE_TX_REBROADCAST)) {
            dp.propertyService.get(if (dp.propertyService.get(Props.DEV_TESTNET)) Props.DEV_P2P_REBROADCAST_TO else Props.P2P_REBROADCAST_TO).toSet()
        } else {
            emptySet()
        }

        val wellKnownPeersList = dp.propertyService.get(if (dp.propertyService.get(Props.DEV_TESTNET)) Props.DEV_P2P_BOOTSTRAP_PEERS else Props.P2P_BOOTSTRAP_PEERS).toMutableSet()

        for (rePeer in rebroadcastPeers) {
            if (!wellKnownPeersList.contains(rePeer)) {
                wellKnownPeersList.add(rePeer)
            }
        }
        wellKnownPeers = if (wellKnownPeersList.isEmpty() || dp.propertyService.get(Props.DEV_OFFLINE)) {
            emptySet()
        } else {
            wellKnownPeersList.toSet()
        }

        connectWellKnownFirst = dp.propertyService.get(Props.P2P_NUM_BOOTSTRAP_CONNECTIONS)
        connectWellKnownFinished = connectWellKnownFirst == 0

        val knownBlacklistedPeersList = dp.propertyService.get(Props.P2P_BLACKLISTED_PEERS)
        if (knownBlacklistedPeersList.isEmpty()) {
            knownBlacklistedPeers = emptySet()
        } else {
            knownBlacklistedPeers = knownBlacklistedPeersList.toSet()
        }

        maxNumberOfConnectedPublicPeers = dp.propertyService.get(Props.P2P_MAX_CONNECTIONS)
        connectTimeout = dp.propertyService.get(Props.P2P_TIMEOUT_CONNECT_MS)
        readTimeout = dp.propertyService.get(Props.P2P_TIMEOUT_READ_MS)

        blacklistingPeriod = dp.propertyService.get(Props.P2P_BLACKLISTING_TIME_MS)
        communicationLoggingMask = dp.propertyService.get(Props.BRS_COMMUNICATION_LOGGING_MASK)
        sendToPeersLimit = dp.propertyService.get(P2P_SEND_TO_LIMIT)
        usePeersDb = dp.propertyService.get(Props.P2P_USE_PEERS_DB) && !dp.propertyService.get(Props.DEV_OFFLINE)
        savePeers = usePeersDb && dp.propertyService.get(Props.P2P_SAVE_PEERS)
        getMorePeers = dp.propertyService.get(Props.P2P_GET_MORE_PEERS)
        getMorePeersThreshold = dp.propertyService.get(Props.P2P_GET_MORE_PEERS_THRESHOLD)
        dumpPeersVersion = dp.propertyService.get(Props.DEV_DUMP_PEERS_VERSION)

        val unresolvedPeers = Collections.synchronizedList(mutableListOf<Future<String>>())

        dp.threadPool.runBeforeStart(object : () -> Unit {
            private fun loadPeers(addresses: Collection<String>) {
                for (address in addresses) {
                    val unresolvedAddress = sendBlocksToPeersService.submit<String> {
                        val peer = addPeer(address)
                        if (peer == null) address else null
                    }
                    unresolvedPeers.add(unresolvedAddress)
                }
            }

            override fun invoke() {
                if (wellKnownPeers.isNotEmpty()) {
                    loadPeers(wellKnownPeers)
                }
                if (usePeersDb) {
                    logger.debug("Loading known peers from the database...")
                    loadPeers(dp.peerDb.loadPeers())
                }
                lastSavedPeers = peers.size
            }
        }, false)

        dp.threadPool.runAfterStart {
            for (unresolvedPeer in unresolvedPeers) {
                try {
                    val badAddress = unresolvedPeer.get(5, TimeUnit.SECONDS)
                    if (badAddress != null) {
                        logger.debug("Failed to resolve peer address: {}", badAddress)
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                } catch (e: ExecutionException) {
                    logger.debug("Failed to add peer", e)
                } catch (ignored: TimeoutException) {
                }

            }
            if (logger.isDebugEnabled) {
                logger.debug("Known peers: {}", peers.size)
            }
        }

        port = if (dp.propertyService.get(Props.DEV_TESTNET)) TESTNET_PEER_PORT else myPeerServerPort
        if (shareMyAddress) {
            if (useUpnp) {
                port = dp.propertyService.get(Props.P2P_PORT)
                val gatewayDiscover = GatewayDiscover()
                gatewayDiscover.timeout = 2000
                try {
                    gatewayDiscover.discover()
                } catch (ignored: IOException) {
                } catch (ignored: SAXException) {
                } catch (ignored: ParserConfigurationException) {
                }

                logger.trace("Looking for Gateway Devices")
                if (gatewayDiscover.validGateway != null) {
                    gateway = gatewayDiscover.validGateway
                }

                val gwDiscover = {
                    if (this.gateway != null) {
                        GatewayDevice.setHttpReadTimeout(2000)
                        try {
                            val localAddress = gateway!!.localAddress
                            val externalIPAddress = gateway!!.externalIPAddress
                            if (logger.isInfoEnabled) {
                                logger.info("Attempting to map {}:{} -> {}:{} on Gateway {} ({})", externalIPAddress, port, localAddress, port, gateway!!.modelName, gateway!!.modelDescription)
                            }
                            val portMapping = PortMappingEntry()
                            if (gateway!!.getSpecificPortMappingEntry(port, "TCP", portMapping)) {
                                logger.info("Port was already mapped. Aborting test.")
                            } else {
                                if (gateway!!.addPortMapping(port!!, port!!, localAddress.hostAddress, "TCP", "burstcoin")) {
                                    logger.info("UPnP Mapping successful")
                                } else {
                                    logger.warn("UPnP Mapping was denied!")
                                }
                            }
                        } catch (e: IOException) {
                            logger.error("Can't start UPnP", e)
                        } catch (e: SAXException) {
                            logger.error("Can't start UPnP", e)
                        }

                    }
                }
                if (this.gateway != null) {
                    Thread(gwDiscover).start()
                } else {
                    logger.warn("Tried to establish UPnP, but it was denied by the network.")
                }
            }

            peerServer = Server()
            val connector = ServerConnector(peerServer)
            connector.port = port
            val host = dp.propertyService.get(Props.P2P_LISTEN)
            connector.host = host
            connector.idleTimeout = dp.propertyService.get(Props.P2P_TIMEOUT_IDLE_MS).toLong()
            connector.reuseAddress = true
            peerServer!!.addConnector(connector)

            val peerServletHolder = ServletHolder(PeerServlet(dp))
            val isGzipEnabled = dp.propertyService.get(Props.JETTY_P2P_GZIP_FILTER)
            peerServletHolder.setInitParameter("isGzipEnabled", isGzipEnabled.toString())

            val peerHandler = ServletHandler()
            peerHandler.addServletWithMapping(peerServletHolder, "/*")

            if (dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER)) {
                val dosFilterHolder = peerHandler.addFilterWithMapping(DoSFilter::class.java, "/*", FilterMapping.DEFAULT)
                dosFilterHolder.setInitParameter("maxRequestsPerSec", dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_MAX_REQUESTS_PER_SEC))
                dosFilterHolder.setInitParameter("throttledRequests", dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_THROTTLED_REQUESTS))
                dosFilterHolder.setInitParameter("delayMs", dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_DELAY_MS))
                dosFilterHolder.setInitParameter("maxWaitMs", dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_MAX_WAIT_MS))
                dosFilterHolder.setInitParameter("maxRequestMs", dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_MAX_REQUEST_MS))
                dosFilterHolder.setInitParameter("maxthrottleMs", dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_THROTTLE_MS))
                dosFilterHolder.setInitParameter("maxIdleTrackerMs", dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_MAX_IDLE_TRACKER_MS))
                dosFilterHolder.setInitParameter("trackSessions", dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_TRACK_SESSIONS))
                dosFilterHolder.setInitParameter("insertHeaders", dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_INSERT_HEADERS))
                dosFilterHolder.setInitParameter("remotePort", dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_REMOTE_PORT))
                dosFilterHolder.setInitParameter("ipWhitelist", dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_IP_WHITELIST))
                dosFilterHolder.setInitParameter("managedAttr", dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_MANAGED_ATTR))
                dosFilterHolder.isAsyncSupported = true
            }

            if (isGzipEnabled) {
                val gzipHandler = GzipHandler()
                gzipHandler.setIncludedMethods(dp.propertyService.get(Props.JETTY_P2P_GZIP_FILTER_METHODS))
                gzipHandler.inflateBufferSize = dp.propertyService.get(Props.JETTY_P2P_GZIP_FILTER_BUFFER_SIZE)
                gzipHandler.minGzipSize = dp.propertyService.get(Props.JETTY_P2P_GZIP_FILTER_MIN_GZIP_SIZE)
                gzipHandler.setIncludedMimeTypes("text/plain")
                gzipHandler.handler = peerHandler
                peerServer!!.handler = gzipHandler
            } else {
                peerServer!!.handler = peerHandler
            }
            peerServer!!.stopAtShutdown = true
            dp.threadPool.runBeforeStart({
                try {
                    peerServer!!.start()
                    logger.info("Started peer networking server at {}:{}", host, port)
                } catch (e: Exception) {
                    logger.error("Failed to start peer networking server", e)
                    throw RuntimeException(e.toString(), e)
                }
            }, true)
        } else {
            logger.info("shareMyAddress is disabled, will not start peer networking server")
        }

        if (!dp.propertyService.get(Props.DEV_OFFLINE)) {
            dp.threadPool.scheduleThread("PeerConnecting", this.peerConnectingThread, 5)
            dp.threadPool.scheduleThread("PeerUnBlacklisting", this.peerUnBlacklistingThread, 1)
            if (getMorePeers) {
                dp.threadPool.scheduleThread("GetMorePeers", this.getMorePeersThread, 5)
            }
        }

    }

    fun shutdown(threadPool: ThreadPool) {
        if (peerServer != null) {
            try {
                peerServer!!.stop()
            } catch (e: Exception) {
                logger.info("Failed to stop peer server", e)
            }

        }
        if (gateway != null) {
            try {
                gateway!!.deletePortMapping(port!!, "TCP")
            } catch (e: Exception) {
                logger.info("Failed to remove UPNP rule from gateway", e)
            }

        }
        if (dumpPeersVersion != null) {
            val buf = StringBuilder()
            for ((key, value) in announcedAddresses) {
                val peer = peers[value]
                if (peer != null && peer.state == Peer.State.CONNECTED && peer.shareAddress && !peer.isBlacklisted
                        && peer.version != null
                        && peer.version.toString().startsWith(dumpPeersVersion!!)) {
                    buf.append("('").append(key).append("'), ")
                }
            }
            if (logger.isInfoEnabled) {
                logger.info(buf.toString())
            }
        }

        threadPool.shutdownExecutor(sendBlocksToPeersService)
    }

    fun removeListener(listener: (Peer) -> Unit, eventType: Event): Boolean {
        return this.listeners.removeListener(listener, eventType)
    }

    internal fun notifyListeners(peer: Peer, eventType: Event) {
        this.listeners.accept(peer, eventType)
    }

    fun getPeers(state: Peer.State): Collection<Peer> {
        val peerList = mutableListOf<Peer>()
        for (peer in peers.values) {
            if (peer.state == state) {
                peerList.add(peer)
            }
        }
        return peerList
    }

    fun getPeer(peerAddress: String): Peer? {
        return peers[peerAddress]
    }

    fun addPeer(announcedAddress: String?): Peer? {
        if (announcedAddress == null) return null
        var cleanAddress = announcedAddress.trim { it <= ' ' }
        var peer = peers[cleanAddress]
        if (peer != null) {
            return peer
        }
        val address = announcedAddresses[cleanAddress]
        if (address != null) {
            peer = peers[address]
            if (peer != null) {
                return peer
            }
        }
        try {
            val uri = URI("http://$cleanAddress")
            val host = uri.host
            peer = peers[host]
            if (peer != null) {
                return peer
            }
            val inetAddress = InetAddress.getByName(host)
            return addPeer(inetAddress.hostAddress, cleanAddress)
        } catch (e: URISyntaxException) {
            return null
        } catch (e: UnknownHostException) {
            return null
        }

    }

    internal fun addPeer(address: String, announcedAddress: String?): Peer? {
        //re-add the [] to ipv6 addresses lost in getHostAddress() above
        var cleanAddress = address
        if (cleanAddress.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size > 2) {
            cleanAddress = "[$cleanAddress]"
        }
        var peer = peers[cleanAddress]
        if (peer != null) {
            return peer
        }
        val peerAddress = normalizeHostAndPort(cleanAddress) ?: return null
        peer = peers[peerAddress]
        if (peer != null) {
            return peer
        }

        val announcedPeerAddress = if (address == announcedAddress) peerAddress else normalizeHostAndPort(announcedAddress)

        if (!myAddress.isNullOrEmpty() && myAddress!!.equals(announcedPeerAddress, ignoreCase = true)) {
            return null
        }

        peer = PeerImpl(dp, peerAddress, announcedPeerAddress)
        if (dp.propertyService.get(Props.DEV_TESTNET) && peer.port > 0 && peer.port != TESTNET_PEER_PORT) {
            logger.debug("Peer {} on testnet port is not using port {}, ignoring", peerAddress, TESTNET_PEER_PORT)
            return null
        }
        peers[peerAddress] = peer
        if (announcedAddress != null) {
            updateAddress(peer)
        }
        listeners.accept(peer, Event.NEW_PEER)
        return peer
    }

    internal fun removePeer(peer: Peer) {
        if (peer.announcedAddress != null) {
            announcedAddresses.remove(peer.announcedAddress!!)
        }
        peers.remove(peer.peerAddress)
    }

    internal fun updateAddress(peer: Peer) {
        val oldAddress = announcedAddresses.put(peer.announcedAddress, peer.peerAddress)
        if (oldAddress != null && peer.peerAddress != oldAddress) {
            val oldPeer = peers.remove(oldAddress)
            if (oldPeer != null) {
                this.notifyListeners(oldPeer, Event.REMOVE)
            }
        }
    }

    fun sendToSomePeers(block: Block) {
        val request = block.jsonObject
        request.addProperty("requestType", "processBlock")

        blocksSendingService.submit {
            val jsonRequest = prepareRequest(request)

            var successful = 0
            val expectedResponses = mutableListOf<Future<JsonObject>>()
            for (peer in peers.values) {

                if (peerEligibleForSending(peer, false)) {
                    val futureResponse = sendBlocksToPeersService.submit<JsonObject> { peer.send(jsonRequest) }
                    expectedResponses.add(futureResponse)
                }
                if (expectedResponses.size >= this.sendToPeersLimit - successful) {
                    for (future in expectedResponses) {
                        try {
                            val response = future.get()
                            if (response != null && response.get("error") == null) {
                                successful += 1
                            }
                        } catch (e: InterruptedException) {
                            Thread.currentThread().interrupt()
                        } catch (e: ExecutionException) {
                            logger.debug("Error in sendToSomePeers", e)
                        }

                    }
                    expectedResponses.clear()
                }
                if (successful >= this.sendToPeersLimit) {
                    return@submit
                }
            }
        }
    }

    init {
        val request = JsonObject()
        request.addProperty("requestType", "getUnconfirmedTransactions")
        getUnconfirmedTransactionsRequest = prepareRequest(request)
    }

    fun readUnconfirmedTransactionsNonBlocking(peer: Peer): CompletableFuture<JsonObject?> {
        // TODO replace CompletableFuture calls
        return CompletableFuture.supplyAsync(Supplier { peer.send(getUnconfirmedTransactionsRequest) }, utReceivingService)
    }

    @Synchronized // TODO not synchronized
    fun feedingTime(peer: Peer, foodDispenser: (Peer) -> Collection<Transaction>, doneFeedingLog: (Peer, Collection<Transaction>) -> Unit) {
        if (!beingProcessed.contains(peer)) {
            beingProcessed.add(peer)
            // TODO replace CompletableFuture calls
            CompletableFuture.runAsync(Runnable { feedPeer(peer, foodDispenser, doneFeedingLog) }, utSendingService)
        } else if (!processingQueue.contains(peer)) {
            processingQueue.add(peer)
        }
    }

    private fun feedPeer(peer: Peer, foodDispenser: (Peer) -> Collection<Transaction>, doneFeedingLog: (Peer, Collection<Transaction>) -> Unit) {
        val transactionsToSend = foodDispenser(peer)

        if (transactionsToSend.isNotEmpty()) {
            logger.trace("Feeding {} {} transactions", peer.peerAddress, transactionsToSend.size)
            val response = peer.send(sendUnconfirmedTransactionsRequest(transactionsToSend))

            if (response != null && response.get("error") == null) {
                doneFeedingLog(peer, transactionsToSend)
            } else {
                // TODO why does this keep coming up??
                logger.warn("Error feeding {} transactions: {} error: {}", peer.peerAddress, transactionsToSend.map { it.id }.toList(), response)
            }
        } else {
            logger.trace("No need to feed {}", peer.peerAddress)
        }

        beingProcessed.remove(peer)

        if (processingQueue.contains(peer)) {
            processingQueue.remove(peer)
            beingProcessed.add(peer)
            feedPeer(peer, foodDispenser, doneFeedingLog)
        }
    }

    private fun sendUnconfirmedTransactionsRequest(transactions: Collection<Transaction>): JsonElement {
        val request = JsonObject()
        val transactionsData = JsonArray()

        for (transaction in transactions) {
            transactionsData.add(transaction.jsonObject)
        }

        request.addProperty("requestType", "processTransactions")
        request.add("transactions", transactionsData)

        return prepareRequest(request)
    }

    private fun peerEligibleForSending(peer: Peer, sendSameBRSclass: Boolean): Boolean {
        return (peer.isHigherOrEqualVersionThan(MIN_VERSION)
                && (!sendSameBRSclass || peer.isAtLeastMyVersion)
                && !peer.isBlacklisted
                && peer.state == Peer.State.CONNECTED
                && peer.announcedAddress != null)
    }

    fun getAnyPeer(state: Peer.State): Peer? {
        if (!connectWellKnownFinished) {
            var wellKnownConnected = 0
            for (peer in peers.values) {
                if (peer.isWellKnown && peer.state == Peer.State.CONNECTED) {
                    wellKnownConnected++
                }
            }
            if (wellKnownConnected >= connectWellKnownFirst) {
                connectWellKnownFinished = true
                logger.info("Finished connecting to {} well known peers.", connectWellKnownFirst)
                // TODO should we remove this?
                logger.info("You can open your Burst Wallet in your favorite browser with: http://127.0.0.1:8125 or http://localhost:8125")
            }
        }

        val selectedPeers = mutableListOf<Peer>()
        for (peer in peers.values) {
            if (!peer.isBlacklisted && peer.state == state && peer.shareAddress
                    && (connectWellKnownFinished || peer.state == Peer.State.CONNECTED || peer.isWellKnown)) {
                selectedPeers.add(peer)
            }
        }

        return if (selectedPeers.isNotEmpty()) {
            selectedPeers[random.nextInt(selectedPeers.size)]
        } else null
    }

    internal fun normalizeHostAndPort(address: String?): String? {
        try {
            if (address == null) {
                return null
            }
            val uri = URI("http://" + address.trim { it <= ' ' })
            val host = uri.host
            if (host == null || host.isEmpty()) {
                return null
            }
            val inetAddress = InetAddress.getByName(host)
            if (inetAddress.isAnyLocalAddress || inetAddress.isLoopbackAddress ||
                    inetAddress.isLinkLocalAddress) {
                return null
            }
            val port = uri.port
            return if (port == -1) host else "$host:$port"
        } catch (e: URISyntaxException) {
            return null
        } catch (e: UnknownHostException) {
            return null
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Peers::class.java)
        internal const val LOGGING_MASK_EXCEPTIONS = 1
        internal const val LOGGING_MASK_NON200_RESPONSES = 2
        internal const val LOGGING_MASK_200_RESPONSES = 4
        internal const val DEFAULT_PEER_PORT = 8123
        internal const val TESTNET_PEER_PORT = 7123
    }
}
