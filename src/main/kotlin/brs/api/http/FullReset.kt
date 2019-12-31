package brs.api.http

import brs.api.http.common.ResultFields.DONE_RESPONSE
import brs.api.http.common.ResultFields.ERROR_RESPONSE
import brs.services.BlockchainProcessorService
import brs.util.jetty.get
import com.google.gson.JsonElement
import brs.util.jetty.get
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class FullReset internal constructor(private val blockchainProcessorService: BlockchainProcessorService) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.DEBUG)) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val response = JsonObject()
        try {
            blockchainProcessorService.fullReset()
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
