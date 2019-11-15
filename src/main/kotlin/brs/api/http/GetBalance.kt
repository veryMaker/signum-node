package brs.api.http

import brs.api.http.common.Parameters
import brs.services.ParameterService
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetBalance(private val parameterService: ParameterService) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), Parameters.ACCOUNT_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        return JSONData.accountBalance(parameterService.getAccount(request))
    }
}
