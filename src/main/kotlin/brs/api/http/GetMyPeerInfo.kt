package brs.api.http

import brs.entity.DependencyProvider
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetMyPeerInfo(private val dp: DependencyProvider) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.PEER_INFO)) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val response = JsonObject()
        response.addProperty("utsInStore", dp.unconfirmedTransactionService.amount)
        return response
    }
}
