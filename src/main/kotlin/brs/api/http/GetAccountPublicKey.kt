package brs.api.http

import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.ResultFields.PUBLIC_KEY_RESPONSE
import brs.services.ParameterService
import brs.util.json.JSON
import brs.util.convert.toHexString
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAccountPublicKey internal constructor(private val parameterService: ParameterService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), ACCOUNT_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {

        val account = parameterService.getAccount(request) ?: return JSONResponses.INCORRECT_ACCOUNT

        return if (account.publicKey != null) {
            val response = JsonObject()
            response.addProperty(PUBLIC_KEY_RESPONSE, account.publicKey!!.toHexString())
            response
        } else {
            JSON.emptyJSON
        }
    }
}
