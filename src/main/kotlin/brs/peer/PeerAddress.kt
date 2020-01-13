package brs.peer

import brs.entity.DependencyProvider
import brs.objects.Props
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
        fun parse(dp: DependencyProvider, address: String, defaultProtocol: Protocol = Protocol.HTTP): PeerAddress? {
            try {
                val uri = if (address.startsWith("http://") || address.startsWith("grpc://")) {
                    URI(address.trim())
                } else {
                    URI("${defaultProtocol.schemeName}://${address.trim()}")
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
                if (port <= 0) port = if (dp.propertyService.get(Props.DEV_TESTNET)) {
                    when (protocol) {
                        Protocol.HTTP -> 7123
                        Protocol.GRPC -> Props.DEV_P2P_V2_PORT.defaultValue
                    }
                } else {
                    when (protocol) {
                        Protocol.HTTP -> Props.P2P_PORT.defaultValue
                        Protocol.GRPC -> Props.P2P_V2_PORT.defaultValue
                    }
                }
                return PeerAddress(protocol, host, port)
            } catch (e: Exception) {
                return null
            }
        }
    }
}