package brs.api.http
import brs.util.jetty.get
import com.google.gson.JsonElement
import brs.util.jetty.get
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal object GetMyInfo : APIServlet.JsonRequestHandler(arrayOf(APITag.INFO)) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val response = JsonObject()
        response.addProperty("host", request.remoteHost)
        response.addProperty("address", request.remoteAddr)
        return response
    }
}
