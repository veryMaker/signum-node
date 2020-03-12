package brs.util

import brs.entity.DependencyProvider
import brs.util.logging.*
import org.bitlet.weupnp.GatewayDevice
import org.bitlet.weupnp.GatewayDiscover
import org.bitlet.weupnp.PortMappingEntry
import org.slf4j.LoggerFactory

object UPnPUtils {
    private val logger = LoggerFactory.getLogger(UPnPUtils::class.java)

    private fun mapUpnp(gateway: GatewayDevice, port: Int) {
        val localAddress = gateway.localAddress
        val externalIPAddress = gateway.externalIPAddress
        logger.safeInfo { "Attempting to map $externalIPAddress:$port -> $localAddress:$port via UPnP" }
        when {
            gateway.getSpecificPortMappingEntry(port, "TCP", PortMappingEntry().apply { externalPort = port; internalPort = port }) -> logger.safeInfo { "Port was already mapped." }
            gateway.addPortMapping(port, port, localAddress.hostAddress, "TCP", "burstcoin") -> logger.safeInfo { "UPnP Mapping successful." }
            else -> logger.safeWarn { "UPnP Mapping was denied!" }
        }
    }

    fun setupUpnp(dp: DependencyProvider, httpPort: Int, grpcPort: Int): GatewayDevice? {
        val gatewayDiscover = GatewayDiscover()
        gatewayDiscover.timeout = 2000
        try {
            gatewayDiscover.discover()
        } catch (e: Exception) {
            logger.safeTrace(e) { "Error discovering gateway" }
            return null
        }

        logger.safeDebug { "Looking for Gateway Devices" }
        val gateway: GatewayDevice? = gatewayDiscover.validGateway
        if (gateway != null) {
            logger.safeDebug { "Gateway Device: ${gateway.modelName} (${gateway.modelDescription}" }
        } else {
            logger.safeDebug { "No Gateway Device Found" }
        }

        if (gateway != null) {
            dp.taskSchedulerService.runBeforeStart {
                GatewayDevice.setHttpReadTimeout(2000)
                try {
                    mapUpnp(gateway, httpPort)
                    mapUpnp(gateway, grpcPort)
                } catch (e: Exception) {
                    logger.safeError(e) { "Can't start UPnP" }
                }
            }
        } else {
            logger.safeWarn { "Tried to establish UPnP, but it was denied by the network." }
        }
        return gateway
    }
}
