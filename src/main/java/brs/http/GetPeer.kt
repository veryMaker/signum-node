package brs.http

import brs.DependencyProvider
import brs.http.JSONResponses.MISSING_PEER
import brs.http.JSONResponses.UNKNOWN_PEER
import brs.http.common.Parameters.PEER_PARAMETER
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class GetPeer(private val dp: DependencyProvider) : APIServlet.JsonRequestHandler(arrayOf(APITag.INFO), PEER_PARAMETER) {
    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        val peerAddress = request.getParameter(PEER_PARAMETER) ?: return MISSING_PEER
        val peer = dp.peers.getPeer(peerAddress) ?: return UNKNOWN_PEER
        return JSONData.peer(peer)
    }
}
