package brs.peer

import brs.DependencyProvider
import brs.util.JSON
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class GetInfo(private val dp: DependencyProvider) : PeerServlet.PeerRequestHandler {
    override suspend fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        var announcedAddress = JSON.getAsString(request.get("announcedAddress"))
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
        var application = JSON.getAsString(request.get("application"))
        if (application.isEmpty()) {
            application = "?"
        }
        peer.application = application.trim { it <= ' ' }

        var version = JSON.getAsString(request.get("version"))
        if (version.isEmpty()) {
            version = "?"
        }
        peer.setVersion(version.trim { it <= ' ' })

        var platform = JSON.getAsString(request.get("platform"))
        if (platform.isEmpty()) {
            platform = "?"
        }
        peer.platform = platform.trim { it <= ' ' }

        peer.shareAddress = JSON.getAsBoolean(request.get("shareAddress"))
        peer.lastUpdated = dp.timeService.epochTime

        dp.peers.notifyListeners(peer, Peers.Event.ADDED_ACTIVE_PEER)

        return dp.peers.myPeerInfoResponse
    }
}
