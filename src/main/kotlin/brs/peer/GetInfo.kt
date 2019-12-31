package brs.peer

import brs.entity.DependencyProvider
import brs.services.PeerService
import brs.util.json.safeGetAsBoolean
import brs.util.json.safeGetAsString
import brs.util.jetty.get
import com.google.gson.JsonElement
import brs.util.jetty.get
import com.google.gson.JsonObject

internal class GetInfo(private val dp: DependencyProvider) : PeerServlet.PeerRequestHandler {
    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        var announcedAddress = request.get("announcedAddress").safeGetAsString()
        if (!announcedAddress.isNullOrEmpty()) {
            announcedAddress = announcedAddress.trim { it <= ' ' }
            if (announcedAddress.isNotEmpty()) {
                if (peer.announcedAddress != null && announcedAddress != peer.announcedAddress) {
                    // force verification of changed announced address
                    peer.state = Peer.State.NON_CONNECTED
                }
                peer.announcedAddress = announcedAddress
            }
        }
        var application = request.get("application").safeGetAsString()
        if (application.isNullOrEmpty()) {
            application = "?"
        }
        peer.application = application.trim { it <= ' ' }

        var version = request.get("version").safeGetAsString()
        if (version.isNullOrEmpty()) {
            version = "?"
        }
        peer.setVersion(version.trim { it <= ' ' })

        var platform = request.get("platform").safeGetAsString()
        if (platform.isNullOrEmpty()) {
            platform = "?"
        }
        peer.platform = platform.trim { it <= ' ' }

        peer.shareAddress = request.get("shareAddress").safeGetAsBoolean() ?: false
        peer.lastUpdated = dp.timeService.epochTime

        dp.peerService.notifyListeners(peer, PeerService.Event.ADDED_ACTIVE_PEER)

        return dp.peerService.myPeerInfoResponse
    }
}
