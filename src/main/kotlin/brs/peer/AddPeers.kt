package brs.peer

import brs.entity.DependencyProvider
import brs.util.json.isEmpty
import brs.util.json.safeGetAsJsonArray
import brs.util.json.safeGetAsString
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class AddPeers(private val dp: DependencyProvider) : PeerServlet.PeerRequestHandler {
    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        val peers = request.get("peers").safeGetAsJsonArray()
        if (peers != null && !peers.isEmpty() && dp.peerService.getMorePeers) {
            for (announcedAddress in peers) {
                dp.peerService.addPeer(announcedAddress.safeGetAsString())
            }
        }
        return JsonObject()
    }
}
