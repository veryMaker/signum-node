package brs.http

import brs.Account
import brs.BurstException
import brs.services.ParameterService
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.NUMBER_OF_CONFIRMATIONS_PARAMETER
import brs.http.common.ResultFields.GUARANTEED_BALANCE_NQT_RESPONSE


@Deprecated("This call is superseded by GetBalance which does what this does and more.")
internal class GetGuaranteedBalance @Deprecated("")
internal constructor(private val parameterService: ParameterService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), ACCOUNT_PARAMETER, NUMBER_OF_CONFIRMATIONS_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val account = parameterService.getAccount(req)
        val response = JsonObject()
        if (account == null) {
            response.addProperty(GUARANTEED_BALANCE_NQT_RESPONSE, "0")
        } else {
            response.addProperty(GUARANTEED_BALANCE_NQT_RESPONSE, account.balanceNQT.toString())
        }
        return response
    }

}