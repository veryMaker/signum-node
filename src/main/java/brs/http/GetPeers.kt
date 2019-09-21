package brs.http

import brs.http.common.Parameters.ACTIVE_PARAMETER
import brs.http.common.Parameters.STATE_PARAMETER
import brs.peer.Peer
import brs.peer.Peers
import brs.util.Convert
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal object GetPeers : APIServlet.JsonRequestHandler(arrayOf(APITag.INFO), ACTIVE_PARAMETER, STATE_PARAMETER) {

    internal override fun processRequest(request: HttpServletRequest): JsonElement {

        val active = "true".equals(request.getParameter(ACTIVE_PARAMETER), ignoreCase = true)
        val stateValue = Convert.emptyToNull(request.getParameter(STATE_PARAMETER))

        val peers = JsonArray()
        for (peer in if (active) Peers.activePeers else if (stateValue != null) Peers.getPeers(Peer.State.valueOf(stateValue)) else Peers.allPeers) {
            peers.add(peer.peerAddress)
        }

        val response = JsonObject()
        response.add("peers", peers)
        return response
    }
}
