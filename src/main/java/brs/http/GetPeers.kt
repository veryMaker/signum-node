package brs.http

import brs.peer.Peer
import brs.peer.Peers
import brs.util.Convert
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.ACTIVE_PARAMETER
import brs.http.common.Parameters.STATE_PARAMETER

internal class GetPeers private constructor() : APIServlet.JsonRequestHandler(arrayOf(APITag.INFO), ACTIVE_PARAMETER, STATE_PARAMETER) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val active = "true".equals(req.getParameter(ACTIVE_PARAMETER), ignoreCase = true)
        val stateValue = Convert.emptyToNull(req.getParameter(STATE_PARAMETER))

        val peers = JsonArray()
        for (peer in if (active) Peers.getActivePeers() else if (stateValue != null) Peers.getPeers(Peer.State.valueOf(stateValue)) else Peers.getAllPeers()) {
            peers.add(peer.peerAddress)
        }

        val response = JsonObject()
        response.add("peers", peers)
        return response
    }

    companion object {

        val instance = GetPeers()
    }

}
