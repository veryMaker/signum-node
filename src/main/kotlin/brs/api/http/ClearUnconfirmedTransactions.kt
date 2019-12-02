package brs.api.http

import brs.api.http.common.ResultFields.DONE_RESPONSE
import brs.api.http.common.ResultFields.ERROR_RESPONSE
import brs.services.TransactionProcessorService
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class ClearUnconfirmedTransactions internal constructor(private val transactionProcessorService: TransactionProcessorService) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.DEBUG)) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val response = JsonObject()
        try {
            transactionProcessorService.clearUnconfirmedTransactions()
            response.addProperty(DONE_RESPONSE, true)
        } catch (e: Exception) {
            response.addProperty(ERROR_RESPONSE, e.toString())
        }

        return response
    }

    override fun requirePost(): Boolean {
        return true
    }

}
