package brs.http

import brs.http.common.Parameters.ID_PARAMETER
import brs.util.Convert
import brs.util.JSON
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.math.BigInteger
import javax.servlet.http.HttpServletRequest

internal object LongConvert : APIServlet.JsonRequestHandler(arrayOf(APITag.UTILS), ID_PARAMETER) {
    internal override fun processRequest(request: HttpServletRequest): JsonElement {
        val id = Convert.emptyToNull(request.getParameter(ID_PARAMETER)) ?: return JSON.emptyJSON
        val response = JsonObject()
        val bigInteger = BigInteger(id)
        if (bigInteger.signum() < 0) {
            if (bigInteger.negate() > Convert.two64) {
                response.addProperty("error", "overflow")
            } else {
                response.addProperty("stringId", bigInteger.add(Convert.two64).toString())
                response.addProperty("longId", bigInteger.toLong().toString())
            }
        } else {
            if (bigInteger >= Convert.two64) {
                response.addProperty("error", "overflow")
            } else {
                response.addProperty("stringId", bigInteger.toString())
                response.addProperty("longId", bigInteger.toLong().toString())
            }
        }
        return response
    }
}
