package brs.http

import brs.BurstException
import brs.http.common.Parameters.ACCOUNTS_RESPONSE
import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.services.AccountService
import brs.services.ParameterService
import brs.util.toUnsignedString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAccountsWithRewardRecipient internal constructor(private val parameterService: ParameterService, private val accountService: AccountService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS, APITag.MINING, APITag.INFO), ACCOUNT_PARAMETER) {
    internal override fun processRequest(request: HttpServletRequest): JsonElement {
        val response = JsonObject()

        val targetAccount = parameterService.getAccount(request)

        val accounts = JsonArray()

        for (assignment in accountService.getAccountsWithRewardRecipient(targetAccount.id)) {
            accounts.add(assignment.accountId.toUnsignedString())
        }

        if (accountService.getRewardRecipientAssignment(targetAccount) == null) {
            accounts.add(targetAccount.id.toUnsignedString())
        }

        response.add(ACCOUNTS_RESPONSE, accounts)

        return response
    }
}
