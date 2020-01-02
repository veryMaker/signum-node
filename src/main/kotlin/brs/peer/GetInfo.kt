package brs.peer

import brs.Burst
import brs.entity.DependencyProvider
import brs.services.PeerService
import brs.util.json.getMemberAsBoolean
import brs.util.json.getMemberAsString
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class GetInfo(private val dp: DependencyProvider) : PeerServlet.PeerRequestHandler {
    private val myPeerInfo: JsonObject

    init {
        myPeerInfo = JsonObject()
        myPeerInfo.addProperty("application", Burst.APPLICATION)
        myPeerInfo.addProperty("version", Burst.VERSION.toString())
        myPeerInfo.addProperty("platform", dp.peerService.myPlatform)
        myPeerInfo.addProperty("shareAddress", dp.peerService.shareMyAddress)
        if (dp.peerService.announcedAddress.isNotEmpty()) {
            myPeerInfo.addProperty("announcedAddress", dp.peerService.announcedAddress)
        }
    }

    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        var announcedAddress = request.getMemberAsString("announcedAddress")
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
        var application = request.getMemberAsString("application")
        if (application.isNullOrEmpty()) {
            application = "?"
        }
        peer.application = application.trim { it <= ' ' }

        var version = request.getMemberAsString("version")
        if (version.isNullOrEmpty()) {
            version = "?"
        }
        peer.setVersion(version.trim { it <= ' ' })

        var platform = request.getMemberAsString("platform")
        if (platform.isNullOrEmpty()) {
            platform = "?"
        }
        peer.platform = platform.trim { it <= ' ' }

        peer.shareAddress = request.getMemberAsBoolean("shareAddress") ?: false
        peer.lastUpdated = dp.timeService.epochTime

        dp.peerService.notifyListeners(peer, PeerService.Event.ADDED_ACTIVE_PEER)

        return myPeerInfo
    }
}
