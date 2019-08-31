package brs.peer

import brs.services.TimeService
import brs.util.JSON
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class GetInfo(private val timeService: TimeService) : PeerServlet.PeerRequestHandler {

    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        val peerImpl = peer as PeerImpl
        var announcedAddress = JSON.getAsString(request.get("announcedAddress"))
        if (announcedAddress != null) {
            announcedAddress = announcedAddress.trim { it <= ' ' }
            if (!announcedAddress.isEmpty()) {
                if (peerImpl.announcedAddress != null && announcedAddress != peerImpl.announcedAddress) {
                    // force verification of changed announced address
                    peerImpl.state = Peer.State.NON_CONNECTED
                }
                peerImpl.announcedAddress = announcedAddress
            }
        }
        var application = JSON.getAsString(request.get("application"))
        if (application == null) {
            application = "?"
        }
        peerImpl.application = application.trim { it <= ' ' }

        var version = JSON.getAsString(request.get("version"))
        if (version == null) {
            version = "?"
        }
        peerImpl.setVersion(version.trim { it <= ' ' })

        var platform = JSON.getAsString(request.get("platform"))
        if (platform == null) {
            platform = "?"
        }
        peerImpl.platform = platform.trim { it <= ' ' }

        peerImpl.setShareAddress(java.lang.Boolean.TRUE == JSON.getAsBoolean(request.get("shareAddress")))
        peerImpl.lastUpdated = timeService.epochTime

        Peers.notifyListeners(peerImpl, Peers.Event.ADDED_ACTIVE_PEER)

        return Peers.myPeerInfoResponse
    }
}
