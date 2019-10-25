package brs.api.http

import brs.transaction.appendix.Attachment
import brs.entity.DependencyProvider
import brs.api.http.common.Parameters.RECIPIENT_PARAMETER
import brs.api.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.api.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class SetRewardRecipient(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.ACCOUNTS, APITag.MINING, APITag.CREATE_TRANSACTION), RECIPIENT_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {
        val account = dp.parameterService.getSenderAccount(request)
        val recipient = ParameterParser.getRecipientId(request)
        val recipientAccount = dp.accountService.getAccount(recipient)
        if (recipientAccount?.publicKey == null) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 8)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "recipient account does not have public key")
            return response
        }
        val attachment = Attachment.BurstMiningRewardRecipientAssignment(dp, dp.blockchainService.height)
        return createTransaction(request, account, recipient, 0, attachment)
    }

}
