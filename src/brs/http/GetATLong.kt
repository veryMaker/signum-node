package brs.http

import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.HEX_STRING_PARAMETER

internal class GetATLong private constructor() : APIServlet.JsonRequestHandler(arrayOf(APITag.AT), HEX_STRING_PARAMETER) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        return JSONData.hex2long(ParameterParser.getATLong(req))
    }

    companion object {

        val instance = GetATLong()
    }

}
