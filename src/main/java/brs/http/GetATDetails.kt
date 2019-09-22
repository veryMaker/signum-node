package brs.http

import brs.BurstException
import brs.http.common.Parameters.AT_PARAMETER
import brs.services.AccountService
import brs.services.ParameterService
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class GetATDetails(private val parameterService: ParameterService, private val accountService: AccountService) : APIServlet.JsonRequestHandler(arrayOf(APITag.AT), AT_PARAMETER) {
    internal override fun processRequest(request: HttpServletRequest): JsonElement {
        return JSONData.at(parameterService.getAT(request), accountService)
    }
}
