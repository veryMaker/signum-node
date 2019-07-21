package brs.http

import brs.Account
import brs.Blockchain
import brs.BurstException
import brs.services.AccountService
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.ResultFields.REWARD_RECIPIENT_RESPONSE

internal class GetRewardRecipient(private val parameterService: ParameterService, private val blockchain: Blockchain, private val accountService: AccountService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS, APITag.MINING, APITag.INFO), ACCOUNT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val response = JsonObject()

        val account = parameterService.getAccount(req)
        val assignment = accountService.getRewardRecipientAssignment(account)
        val height = blockchain.lastBlock.height.toLong()
        if (assignment == null) {
            response.addProperty(REWARD_RECIPIENT_RESPONSE, Convert.toUnsignedLong(account.getId()))
        } else if (assignment.fromHeight > height + 1) {
            response.addProperty(REWARD_RECIPIENT_RESPONSE, Convert.toUnsignedLong(assignment.prevRecipientId))
        } else {
            response.addProperty(REWARD_RECIPIENT_RESPONSE, Convert.toUnsignedLong(assignment.recipientId))
        }

        return response
    }

}
