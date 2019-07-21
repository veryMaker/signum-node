package brs.http

import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

internal class GetMyInfo private constructor() : APIServlet.JsonRequestHandler(arrayOf(APITag.INFO)) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val response = JsonObject()
        response.addProperty("host", req.remoteHost)
        response.addProperty("address", req.remoteAddr)
        return response
    }

    companion object {

        val instance = GetMyInfo()
    }

}
