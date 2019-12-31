package brs.api.http

import brs.api.http.common.Parameters.HEX_STRING_PARAMETER
import brs.api.http.common.JSONData
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal object GetATLong : APIServlet.JsonRequestHandler(arrayOf(APITag.AT), HEX_STRING_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        return JSONData.hex2long(ParameterParser.getATLong(request))
    }
}
