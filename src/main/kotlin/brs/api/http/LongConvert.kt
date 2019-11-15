package brs.api.http

import brs.api.http.JSONResponses.MISSING_ID
import brs.api.http.common.Parameters.ID_PARAMETER
import brs.util.convert.emptyToNull
import brs.util.convert.parseUnsignedLong
import brs.util.convert.toUnsignedString
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal object LongConvert : APIServlet.JsonRequestHandler(arrayOf(APITag.UTILS), ID_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val id = request.getParameter(ID_PARAMETER).emptyToNull() ?: return MISSING_ID
        val response = JsonObject()
        val long: Long
        try {
            long = id.parseUnsignedLong()
        } catch (e: NumberFormatException) {
            response.addProperty("error", "overflow")
            return response
        }
        response.addProperty("stringId", long.toUnsignedString())
        response.addProperty("longId", long.toString())
        return response
    }
}
