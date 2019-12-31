package brs.peer

import brs.entity.DependencyProvider
import com.google.gson.JsonArray
import brs.util.jetty.get
import com.google.gson.JsonElement
import brs.util.jetty.get
import com.google.gson.JsonObject

internal class GetPeers(private val dp: DependencyProvider) : PeerServlet.PeerRequestHandler {
    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        val response = JsonObject()
        val peers = JsonArray()
        for (otherPeer in dp.peerService.allPeers) {
            if (!otherPeer.isBlacklisted && otherPeer.announcedAddress != null && otherPeer.state == Peer.State.CONNECTED && otherPeer.shareAddress) {
                peers.add(otherPeer.announcedAddress)
            }
        }
        response.add("peers", peers)

        return response
    }
}
