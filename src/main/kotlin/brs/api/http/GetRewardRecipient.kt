package brs.api.http

import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.ResultFields.REWARD_RECIPIENT_RESPONSE
import brs.services.AccountService
import brs.services.BlockchainService
import brs.services.ParameterService
import brs.util.convert.toUnsignedString
import brs.util.jetty.get
import com.google.gson.JsonElement
import brs.util.jetty.get
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetRewardRecipient(
    private val parameterService: ParameterService,
    private val blockchainService: BlockchainService,
    private val accountService: AccountService
) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS, APITag.MINING, APITag.INFO), ACCOUNT_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val response = JsonObject()

        val account = parameterService.getAccount(request)
        val assignment = accountService.getRewardRecipientAssignment(account)
        val height = blockchainService.lastBlock.height.toLong()
        when {
            assignment == null -> response.addProperty(REWARD_RECIPIENT_RESPONSE, account.id.toUnsignedString())
            assignment.fromHeight > height + 1 -> response.addProperty(
                REWARD_RECIPIENT_RESPONSE,
                assignment.prevRecipientId.toUnsignedString()
            )
            else -> response.addProperty(REWARD_RECIPIENT_RESPONSE, assignment.recipientId.toUnsignedString())
        }

        return response
    }
}
