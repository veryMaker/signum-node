package brs.http

import brs.peer.Peer
import brs.peer.Peers
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.MISSING_PEER
import brs.http.JSONResponses.UNKNOWN_PEER
import brs.http.common.Parameters.PEER_PARAMETER

internal class GetPeer private constructor() : APIServlet.JsonRequestHandler(arrayOf(APITag.INFO), PEER_PARAMETER) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val peerAddress = req.getParameter(PEER_PARAMETER) ?: return MISSING_PEER

        val peer = Peers.getPeer(peerAddress) ?: return UNKNOWN_PEER

        return JSONData.peer(peer)

    }

    companion object {

        val instance = GetPeer()
    }

}
