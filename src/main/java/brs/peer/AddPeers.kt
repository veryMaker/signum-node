package brs.peer

import brs.DependencyProvider
import brs.util.*
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class AddPeers(private val dp: DependencyProvider) : PeerServlet.PeerRequestHandler {
    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        val peers = request.get("peers").mustGetAsJsonArray("peers")
        if (!peers.isEmpty() && dp.peers.getMorePeers) {
            for (announcedAddress in peers) {
                dp.peers.addPeer(announcedAddress.safeGetAsString())
            }
        }
        return JSON.emptyJSON
    }
}
