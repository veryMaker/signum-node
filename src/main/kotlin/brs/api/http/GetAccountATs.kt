package brs.api.http

import brs.api.http.common.JSONResponses.INCORRECT_AT
import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.ResultFields.ATS_RESPONSE
import brs.api.http.common.JSONData
import brs.services.ATService
import brs.services.AccountService
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetAccountATs internal constructor(
    private val parameterService: ParameterService,
    private val atService: ATService,
    private val accountService: AccountService
) : APIServlet.JsonRequestHandler(arrayOf(APITag.AT, APITag.ACCOUNTS), ACCOUNT_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val account = parameterService.getAccount(request)

        val atIds = atService.getATsIssuedBy(account.id)
        val ats = JsonArray()
        for (atId in atIds) {
            ats.add(JSONData.at(atService.getAT(atId) ?: return INCORRECT_AT, accountService))
        }

        val response = JsonObject()
        response.add(ATS_RESPONSE, ats)
        return response
    }
}
