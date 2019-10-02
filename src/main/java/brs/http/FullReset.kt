package brs.http

import brs.BlockchainProcessor
import brs.http.common.ResultFields.DONE_RESPONSE
import brs.http.common.ResultFields.ERROR_RESPONSE
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class FullReset internal constructor(private val blockchainProcessor: BlockchainProcessor) : APIServlet.JsonRequestHandler(arrayOf(APITag.DEBUG)) {
    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        val response = JsonObject()
        try {
            blockchainProcessor.fullReset()
            response.addProperty(DONE_RESPONSE, true)
        } catch (e: RuntimeException) {
            response.addProperty(ERROR_RESPONSE, e.toString())
        }

        return response
    }

    internal override fun requirePost(): Boolean {
        return true
    }
}
