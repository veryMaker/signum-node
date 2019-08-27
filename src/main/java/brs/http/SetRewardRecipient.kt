package brs.http

import brs.*
import brs.services.AccountService
import brs.services.ParameterService
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.RECIPIENT_PARAMETER
import brs.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE

internal class SetRewardRecipient(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.ACCOUNTS, APITag.MINING, APITag.CREATE_TRANSACTION), RECIPIENT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val account = dp.parameterService.getSenderAccount(req)
        val recipient = ParameterParser.getRecipientId(req)
        val recipientAccount = dp.accountService.getAccount(recipient)
        if (recipientAccount == null || recipientAccount.publicKey == null) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 8)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "recipient account does not have public key")
            return response
        }
        val attachment = Attachment.BurstMiningRewardRecipientAssignment(dp.blockchain.height)
        return createTransaction(req, account, recipient, 0, attachment)
    }

}
