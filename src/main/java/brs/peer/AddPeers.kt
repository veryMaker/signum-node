package brs.peer

import brs.DependencyProvider
import brs.util.JSON
import brs.util.isEmpty
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class AddPeers(private val dp: DependencyProvider) : PeerServlet.PeerRequestHandler {
    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        val peers = JSON.getAsJsonArray(request.get("peers"))
        if (!peers.isEmpty() && dp.peers.getMorePeers) {
            for (announcedAddress in peers) {
                dp.peers.addPeer(JSON.getAsString(announcedAddress))
            }
        }
        return JSON.emptyJSON
    }
}
