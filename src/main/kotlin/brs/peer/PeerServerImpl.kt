package brs.peer

import brs.entity.DependencyProvider
import brs.objects.Props
import brs.util.logging.safeInfo
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.servlet.FilterMapping
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.servlets.DoSFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PeerServerImpl(dp: DependencyProvider) : PeerServer {
    private val peerServer: Server?

    init {
        val shareMyAddress = dp.propertyService.get(Props.P2P_SHARE_MY_ADDRESS) && !dp.propertyService.get(Props.DEV_OFFLINE)
        if (shareMyAddress) {
            peerServer = Server()
            val connector = ServerConnector(peerServer)
            connector.port = if (dp.propertyService.get(Props.DEV_TESTNET)) 7123 else dp.propertyService.get(Props.P2P_PORT)
            val host = dp.propertyService.get(Props.P2P_LISTEN)
            connector.host = host
            connector.idleTimeout = dp.propertyService.get(Props.P2P_TIMEOUT_IDLE_MS).toLong()
            connector.reuseAddress = true
            peerServer.addConnector(connector)

            val peerServletHolder = ServletHolder(PeerServlet(dp))
            val isGzipEnabled = dp.propertyService.get(Props.JETTY_P2P_GZIP_FILTER)
            peerServletHolder.setInitParameter("isGzipEnabled", isGzipEnabled.toString())

            val peerHandler = ServletHandler()
            peerHandler.addServletWithMapping(peerServletHolder, "/*")

            if (dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER)) {
                val dosFilterHolder = peerHandler.addFilterWithMapping(DoSFilter::class.java, "/*", FilterMapping.DEFAULT)
                dosFilterHolder.setInitParameter(
                    "maxRequestsPerSec",
                    dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_MAX_REQUESTS_PER_SEC)
                )
                dosFilterHolder.setInitParameter(
                    "throttledRequests",
                    dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_THROTTLED_REQUESTS)
                )
                dosFilterHolder.setInitParameter("delayMs", dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_DELAY_MS))
                dosFilterHolder.setInitParameter(
                    "maxWaitMs",
                    dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_MAX_WAIT_MS)
                )
                dosFilterHolder.setInitParameter(
                    "maxRequestMs",
                    dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_MAX_REQUEST_MS)
                )
                dosFilterHolder.setInitParameter(
                    "maxthrottleMs",
                    dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_THROTTLE_MS)
                )
                dosFilterHolder.setInitParameter(
                    "maxIdleTrackerMs",
                    dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_MAX_IDLE_TRACKER_MS)
                )
                dosFilterHolder.setInitParameter(
                    "trackSessions",
                    dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_TRACK_SESSIONS)
                )
                dosFilterHolder.setInitParameter(
                    "insertHeaders",
                    dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_INSERT_HEADERS)
                )
                dosFilterHolder.setInitParameter(
                    "remotePort",
                    dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_REMOTE_PORT)
                )
                dosFilterHolder.setInitParameter(
                    "ipWhitelist",
                    dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_IP_WHITELIST)
                )
                dosFilterHolder.setInitParameter(
                    "managedAttr",
                    dp.propertyService.get(Props.JETTY_P2P_DOS_FILTER_MANAGED_ATTR)
                )
                dosFilterHolder.isAsyncSupported = true
            }

            if (isGzipEnabled) {
                val gzipHandler = GzipHandler()
                gzipHandler.setIncludedMethods(dp.propertyService.get(Props.JETTY_P2P_GZIP_FILTER_METHODS))
                gzipHandler.inflateBufferSize = dp.propertyService.get(Props.JETTY_P2P_GZIP_FILTER_BUFFER_SIZE)
                gzipHandler.minGzipSize = dp.propertyService.get(Props.JETTY_P2P_GZIP_FILTER_MIN_GZIP_SIZE)
                gzipHandler.setIncludedMimeTypes("text/plain")
                gzipHandler.handler = peerHandler
                peerServer.handler = gzipHandler
            } else {
                peerServer.handler = peerHandler
            }
            peerServer.stopAtShutdown = true
            dp.taskSchedulerService.runBeforeStart {
                peerServer.start()
                logger.safeInfo { "Started peer networking server at $host:${connector.port}" }
            }
        } else {
            peerServer = null
            logger.safeInfo { "shareMyAddress is disabled, will not start peer networking server" }
        }
    }

    override fun shutdown() {
        if (peerServer != null) {
            try {
                peerServer.stop()
            } catch (e: Exception) {
                logger.safeInfo(e) { "Failed to stop peer server" }
            }
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(PeerServerImpl::class.java)
    }
}
