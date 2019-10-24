package brs.http

import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

internal object GetMyInfo : APIServlet.JsonRequestHandler(arrayOf(APITag.INFO)) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val response = JsonObject()
        response.addProperty("host", request.remoteHost)
        response.addProperty("address", request.remoteAddr)
        return response
    }
}
