package brs.http

import brs.Account
import brs.BurstException
import brs.services.ATService
import brs.services.AccountService
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.ResultFields.ATS_RESPONSE

internal class GetAccountATs internal constructor(private val parameterService: ParameterService, private val atService: ATService, private val accountService: AccountService) : APIServlet.JsonRequestHandler(arrayOf(APITag.AT, APITag.ACCOUNTS), ACCOUNT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val account = parameterService.getAccount(req) // TODO this is super redundant

        val atIds = atService.getATsIssuedBy(account.getId())
        val ats = JsonArray()
        for (atId in atIds) {
            ats.add(JSONData.at(atService.getAT(atId), accountService))
        }

        val response = JsonObject()
        response.add(ATS_RESPONSE, ats)
        return response
    }
}
