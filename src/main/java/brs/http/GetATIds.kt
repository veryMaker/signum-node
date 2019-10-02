package brs.http

import brs.http.common.ResultFields.AT_IDS_RESPONSE
import brs.services.ATService
import brs.util.toUnsignedString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetATIds(private val atService: ATService) : APIServlet.JsonRequestHandler(arrayOf(APITag.AT)) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {

        val atIds = JsonArray()
        for (id in atService.allATIds) {
            atIds.add(id.toUnsignedString())
        }

        val response = JsonObject()
        response.add(AT_IDS_RESPONSE, atIds)
        return response
    }

}
