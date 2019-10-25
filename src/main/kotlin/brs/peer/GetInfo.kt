package brs.peer

import brs.entity.DependencyProvider
import brs.services.PeerService
import brs.util.json.mustGetAsBoolean
import brs.util.json.mustGetAsString
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class GetInfo(private val dp: DependencyProvider) : PeerServlet.PeerRequestHandler {
    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
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

        dp.peerService.notifyListeners(peer, PeerService.Event.ADDED_ACTIVE_PEER)

        return dp.peerService.myPeerInfoResponse
    }
}
