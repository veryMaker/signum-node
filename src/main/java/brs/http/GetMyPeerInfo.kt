package brs.http

import brs.TransactionProcessor
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

internal class GetMyPeerInfo(private val transactionProcessor: TransactionProcessor) : APIServlet.JsonRequestHandler(arrayOf(APITag.PEER_INFO)) {
    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        val response = JsonObject()
        response.addProperty("utsInStore", transactionProcessor.amountUnconfirmedTransactions)
        return response
    }
}
