package brs.http

import brs.Account
import brs.BurstException
import brs.services.AccountService
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.ACCOUNTS_RESPONSE
import brs.http.common.Parameters.ACCOUNT_PARAMETER

internal class GetAccountsWithRewardRecipient internal constructor(private val parameterService: ParameterService, private val accountService: AccountService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS, APITag.MINING, APITag.INFO), ACCOUNT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val response = JsonObject()

        val targetAccount = parameterService.getAccount(req)

        val accounts = JsonArray()

        for (assignment in accountService.getAccountsWithRewardRecipient(targetAccount.id)) {
            accounts.add(Convert.toUnsignedLong(assignment.getAccountId()))
        }

        if (accountService.getRewardRecipientAssignment(targetAccount) == null) {
            accounts.add(Convert.toUnsignedLong(targetAccount.id))
        }

        response.add(ACCOUNTS_RESPONSE, accounts)

        return response
    }
}
