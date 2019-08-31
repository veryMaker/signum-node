package brs.peer

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class GetPeers private constructor() : PeerServlet.PeerRequestHandler {


    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {

        val response = JsonObject()

        val peers = JsonArray()
        for (otherPeer in Peers.allPeers) {

            if (!otherPeer.isBlacklisted && otherPeer.announcedAddress != null
                    && otherPeer.state == Peer.State.CONNECTED && otherPeer.shareAddress()) {

                peers.add(otherPeer.announcedAddress)

            }

        }
        response.add("peers", peers)

        return response
    }

    companion object {

        val instance = GetPeers()
    }

}
