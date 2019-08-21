package brs.http

import brs.Account
import brs.BurstException
import brs.services.AccountService
import brs.util.Convert
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.ACCOUNTS_RESPONSE
import brs.http.common.Parameters.NAME_PARAMETER

internal class GetAccountsWithName internal constructor(private val accountService: AccountService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), NAME_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(request: HttpServletRequest): JsonElement {
        val accounts = accountService.getAccountsWithName(request.getParameter(NAME_PARAMETER))
        val accountIds = JsonArray()

        for (account in accounts) {
            accountIds.add(Convert.toUnsignedLong(account.id))
        }

        val response = JsonObject()
        response.add(ACCOUNTS_RESPONSE, accountIds)
        return response
    }
}
