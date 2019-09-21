package brs.peer

import brs.util.JSON
import brs.util.isEmpty
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal object AddPeers : PeerServlet.PeerRequestHandler {
    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        val peers = JSON.getAsJsonArray(request.get("peers"))
        if (!peers.isEmpty() && Peers.getMorePeers) {
            for (announcedAddress in peers) {
                Peers.addPeer(JSON.getAsString(announcedAddress))
            }
        }
        return JSON.emptyJSON
    }
}
