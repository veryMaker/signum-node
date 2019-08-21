package brs.http

import brs.services.ATService
import brs.util.Convert
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.ResultFields.AT_IDS_RESPONSE

internal class GetATIds(private val atService: ATService) : APIServlet.JsonRequestHandler(arrayOf(APITag.AT)) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val atIds = JsonArray()
        for (id in atService.allATIds) {
            atIds.add(Convert.toUnsignedLong(id!!))
        }

        val response = JsonObject()
        response.add(AT_IDS_RESPONSE, atIds)
        return response
    }

}
