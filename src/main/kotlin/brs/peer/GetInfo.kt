package brs.peer

import brs.Burst
import brs.entity.DependencyProvider
import brs.services.PeerService
import brs.util.Version
import brs.util.json.getMemberAsBoolean
import brs.util.json.getMemberAsString
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class GetInfo(private val dp: DependencyProvider) : PeerServlet.PeerRequestHandler {
    private val myPeerInfo by lazy {
        val info = JsonObject()
        info.addProperty("application", Burst.APPLICATION)
        info.addProperty("version", Burst.VERSION.toString())
        info.addProperty("platform", dp.peerService.myPlatform)
        info.addProperty("shareAddress", dp.peerService.shareMyAddress)
        if (dp.peerService.myAnnouncedAddress != null) {
            info.addProperty("announcedAddress", dp.peerService.myAnnouncedAddress.toString())
        }
        info
    }

    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        val announcedAddress = request.getMemberAsString("announcedAddress")
        if (!announcedAddress.isNullOrBlank()) {
            val newAddress = PeerAddress.parse(dp, announcedAddress.trim())
            if (newAddress != null && newAddress != peer.address) {
                peer.updateAddress(newAddress)
            }
        }
        var application = request.getMemberAsString("application")
        if (application.isNullOrEmpty()) {
            application = "?"
        }
        peer.application = application.trim()

        var version = request.getMemberAsString("version")
        if (version.isNullOrEmpty()) {
            version = "?"
        }
        peer.version = try {
            Version.parse(version.trim())
        } catch (e: Exception) {
            Version.EMPTY
        }

        var platform = request.getMemberAsString("platform")
        if (platform.isNullOrEmpty()) {
            platform = "?"
        }
        peer.platform = platform.trim()

        peer.shareAddress = request.getMemberAsBoolean("shareAddress") ?: false
        peer.lastUpdated = dp.timeService.epochTime

        dp.peerService.notifyListeners(peer, PeerService.Event.ADDED_ACTIVE_PEER)

        return myPeerInfo
    }
}
