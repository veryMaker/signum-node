package brs.peer

import brs.DependencyProvider
import brs.util.mustGetAsBoolean
import brs.util.mustGetAsString
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class GetInfo(private val dp: DependencyProvider) : PeerServlet.PeerRequestHandler {
    override suspend fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        var announcedAddress = request.get("announcedAddress").mustGetAsString("announcedAddress")
        if (announcedAddress.isNotEmpty()) {
            announcedAddress = announcedAddress.trim { it <= ' ' }
            if (announcedAddress.isNotEmpty()) {
                if (peer.announcedAddress != null && announcedAddress != peer.announcedAddress) {
                    // force verification of changed announced address
                    peer.state = Peer.State.NON_CONNECTED
                }
                peer.announcedAddress = announcedAddress
            }
        }
        var application = request.get("application").mustGetAsString("application")
        if (application.isEmpty()) {
            application = "?"
        }
        peer.application = application.trim { it <= ' ' }

        var version = request.get("version").mustGetAsString("version")
        if (version.isEmpty()) {
            version = "?"
        }
        peer.setVersion(version.trim { it <= ' ' })

        var platform = request.get("platform").mustGetAsString("platform")
        if (platform.isEmpty()) {
            platform = "?"
        }
        peer.platform = platform.trim { it <= ' ' }

        peer.shareAddress = request.get("shareAddress").mustGetAsBoolean("shareAddress")
        peer.lastUpdated = dp.timeService.epochTime

        dp.peers.notifyListeners(peer, Peers.Event.ADDED_ACTIVE_PEER)

        return dp.peers.myPeerInfoResponse
    }
}
