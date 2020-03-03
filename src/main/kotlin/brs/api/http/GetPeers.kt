package brs.api.http

import brs.api.http.common.Parameters.ACTIVE_PARAMETER
import brs.api.http.common.Parameters.STATE_PARAMETER
import brs.entity.DependencyProvider
import brs.util.convert.emptyToNull
import brs.util.jetty.get
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetPeers(private val dp: DependencyProvider) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.INFO), ACTIVE_PARAMETER, STATE_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val active = "true".equals(request[ACTIVE_PARAMETER], ignoreCase = true)
        val stateValue = request[STATE_PARAMETER].emptyToNull()?.toLowerCase()
        val isConnected = stateValue == "connected"

        val peers = JsonArray()
        for (peer in when {
            active -> dp.peerService.activePeers
            stateValue != null -> dp.peerService.getPeers(isConnected)
            else -> dp.peerService.allPeers
        }) {
            peers.add(peer.announcedAddress?.toString() ?: peer.remoteAddress)
        }

        val response = JsonObject()
        response.add("peers", peers)
        return response
    }
}
