package brs.http

import brs.http.common.Parameters.ID_PARAMETER
import brs.util.Convert
import brs.util.JSON
import brs.util.parseUnsignedLong
import brs.util.toUnsignedString
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal object LongConvert : APIServlet.JsonRequestHandler(arrayOf(APITag.UTILS), ID_PARAMETER) {
    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        val id = Convert.emptyToNull(request.getParameter(ID_PARAMETER)) ?: return JSON.emptyJSON
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
