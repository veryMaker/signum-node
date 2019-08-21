package brs.http

import brs.BurstException
import brs.http.common.Parameters
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

internal class GetBalance(private val parameterService: ParameterService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), Parameters.ACCOUNT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        return JSONData.accountBalance(parameterService.getAccount(req))
    }

}
