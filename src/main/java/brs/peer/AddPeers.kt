package brs.peer

import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class AddPeers private constructor() : PeerServlet.PeerRequestHandler {

    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        val peers = JSON.getAsJsonArray(request.get("peers"))
        if (peers != null && Peers.getMorePeers) {
            for (announcedAddress in peers) {
                Peers.addPeer(JSON.getAsString(announcedAddress))
            }
        }
        return JSON.emptyJSON
    }

    companion object {

        val instance = AddPeers()
    }

}
