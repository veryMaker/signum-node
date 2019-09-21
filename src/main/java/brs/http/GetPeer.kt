package brs.http

import brs.http.JSONResponses.MISSING_PEER
import brs.http.JSONResponses.UNKNOWN_PEER
import brs.http.common.Parameters.PEER_PARAMETER
import brs.peer.Peers
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal object GetPeer : APIServlet.JsonRequestHandler(arrayOf(APITag.INFO), PEER_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val peerAddress = request.getParameter(PEER_PARAMETER) ?: return MISSING_PEER
        val peer = Peers.getPeer(peerAddress) ?: return UNKNOWN_PEER
        return JSONData.peer(peer)
    }
}
