package brs.http

import brs.BurstException
import brs.services.AccountService
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.AT_PARAMETER

internal class GetATDetails(private val parameterService: ParameterService, private val accountService: AccountService) : APIServlet.JsonRequestHandler(arrayOf(APITag.AT), AT_PARAMETER) {
    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        return JSONData.at(parameterService.getAT(req), accountService)
    }
}
