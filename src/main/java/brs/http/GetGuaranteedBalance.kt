package brs.http

import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.NUMBER_OF_CONFIRMATIONS_PARAMETER
import brs.http.common.ResultFields.GUARANTEED_BALANCE_NQT_RESPONSE
import brs.services.ParameterService
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest


@Deprecated("This call is superseded by GetBalance which does what this does and more.")
internal class GetGuaranteedBalance @Deprecated("")
internal constructor(private val parameterService: ParameterService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), ACCOUNT_PARAMETER, NUMBER_OF_CONFIRMATIONS_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        val account = parameterService.getAccount(request)
        val response = JsonObject()
        if (account == null) {
            response.addProperty(GUARANTEED_BALANCE_NQT_RESPONSE, "0")
        } else {
            response.addProperty(GUARANTEED_BALANCE_NQT_RESPONSE, account.balanceNQT.toString())
        }
        return response
    }

}