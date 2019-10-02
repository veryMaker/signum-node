package brs.peer

import brs.DependencyProvider
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class GetPeers(private val dp: DependencyProvider) : PeerServlet.PeerRequestHandler {
    override suspend fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        val response = JsonObject()
        val peers = JsonArray()
        for (otherPeer in dp.peers.allPeers) {
            if (!otherPeer.isBlacklisted && otherPeer.announcedAddress != null && otherPeer.state == Peer.State.CONNECTED && otherPeer.shareAddress) {
                peers.add(otherPeer.announcedAddress)
            }
        }
        response.add("peers", peers)

        return response
    }
}
