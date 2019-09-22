package brs.http

import brs.DependencyProvider
import brs.http.common.Parameters.ACTIVE_PARAMETER
import brs.http.common.Parameters.STATE_PARAMETER
import brs.peer.Peer
import brs.util.Convert
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetPeers(private val dp: DependencyProvider) : APIServlet.JsonRequestHandler(arrayOf(APITag.INFO), ACTIVE_PARAMETER, STATE_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {

        val active = "true".equals(request.getParameter(ACTIVE_PARAMETER), ignoreCase = true)
        val stateValue = Convert.emptyToNull(request.getParameter(STATE_PARAMETER))

        val peers = JsonArray()
        for (peer in if (active) dp.peers.activePeers else if (stateValue != null) dp.peers.getPeers(Peer.State.valueOf(stateValue)) else dp.peers.allPeers) {
            peers.add(peer.peerAddress)
        }

        val response = JsonObject()
        response.add("peers", peers)
        return response
    }
}
