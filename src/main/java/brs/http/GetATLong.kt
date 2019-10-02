package brs.http

import brs.http.common.Parameters.HEX_STRING_PARAMETER
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal object GetATLong: APIServlet.JsonRequestHandler(arrayOf(APITag.AT), HEX_STRING_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        return JSONData.hex2long(ParameterParser.getATLong(request))
    }
}
