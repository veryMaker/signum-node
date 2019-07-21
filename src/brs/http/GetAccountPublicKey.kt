package brs.http

import brs.Account
import brs.BurstException
import brs.services.ParameterService
import brs.util.Convert
import brs.util.JSON
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.ResultFields.PUBLIC_KEY_RESPONSE

internal class GetAccountPublicKey internal constructor(private val parameterService: ParameterService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), ACCOUNT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val account = parameterService.getAccount(req)

        if (account.publicKey != null) {
            val response = JsonObject()
            response.addProperty(PUBLIC_KEY_RESPONSE, Convert.toHexString(account.publicKey))
            return response
        } else {
            return JSON.emptyJSON
        }
    }
}
