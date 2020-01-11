package brs.peer

import brs.entity.DependencyProvider
import brs.objects.Props
import brs.services.impl.PeerServiceImpl
import java.net.InetAddress
import java.net.URI

data class PeerAddress(
    val protocol: Protocol,
    val host: String,
    val port: Int
) {
    override fun toString(): String {
        return "${protocol.schemeName}://$host:$port"
    }

    enum class Protocol(internal val schemeName: String) {
        HTTP("http"),
        GRPC("grpc"),
    }

    companion object {
        fun parse(dp: DependencyProvider, address: String): PeerAddress? {
            try {
                val uri = if (address.startsWith("http://") || address.startsWith("grpc://")) {
                    URI(address.trim())
                } else {
                    URI("http://" + address.trim())
                }
                val protocol = when (uri.scheme.toLowerCase()) {
                    "grpc" -> Protocol.GRPC
                    "http" -> Protocol.HTTP
                    else -> return null
                }
                val host = uri.host
                if (host == null || host.isEmpty()) return null
                val inetAddress = InetAddress.getByName(host)
                if (inetAddress.isAnyLocalAddress || inetAddress.isLoopbackAddress || inetAddress.isLinkLocalAddress) return null
                var port = uri.port
                if (port <= 0) port = if (dp.propertyService.get(Props.DEV_TESTNET)) PeerServiceImpl.TESTNET_PEER_PORT else PeerServiceImpl.DEFAULT_PEER_PORT
                return PeerAddress(protocol, host, port)
            } catch (e: Exception) {
                return null
            }
        }
    }
}