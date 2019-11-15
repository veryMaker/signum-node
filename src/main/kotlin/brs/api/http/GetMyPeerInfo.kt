package brs.api.http

import brs.services.TransactionProcessorService
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetMyPeerInfo(private val transactionProcessorService: TransactionProcessorService) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.PEER_INFO)) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val response = JsonObject()
        response.addProperty("utsInStore", transactionProcessorService.amountUnconfirmedTransactions)
        return response
    }
}
