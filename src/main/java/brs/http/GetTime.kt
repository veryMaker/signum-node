package brs.http

import brs.services.TimeService
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.ResultFields.TIME_RESPONSE

internal class GetTime internal constructor(private val timeService: TimeService) : APIServlet.JsonRequestHandler(arrayOf(APITag.INFO)) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val response = JsonObject()
        response.addProperty(TIME_RESPONSE, timeService.epochTime)

        return response
    }

}
