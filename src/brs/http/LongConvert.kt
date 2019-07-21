package brs.http

import brs.util.Convert
import brs.util.JSON
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest
import java.math.BigInteger

import brs.http.common.Parameters.ID_PARAMETER

internal class LongConvert private constructor() : APIServlet.JsonRequestHandler(arrayOf(APITag.UTILS), ID_PARAMETER) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val id = Convert.emptyToNull(req.getParameter(ID_PARAMETER)) ?: return JSON.emptyJSON
        val response = JsonObject()
        val bigInteger = BigInteger(id)
        if (bigInteger.signum() < 0) {
            if (bigInteger.negate().compareTo(Convert.two64) > 0) {
                response.addProperty("error", "overflow")
            } else {
                response.addProperty("stringId", bigInteger.add(Convert.two64).toString())
                response.addProperty("longId", bigInteger.toLong().toString())
            }
        } else {
            if (bigInteger.compareTo(Convert.two64) >= 0) {
                response.addProperty("error", "overflow")
            } else {
                response.addProperty("stringId", bigInteger.toString())
                response.addProperty("longId", bigInteger.toLong().toString())
            }
        }
        return response
    }

    companion object {

        val instance = LongConvert()
    }

}
