package brs.http

import brs.Account
import brs.Attachment
import brs.Blockchain
import brs.BurstException
import brs.services.AccountService
import brs.services.ParameterService
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.RECIPIENT_PARAMETER
import brs.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE

internal class SetRewardRecipient(private val parameterService: ParameterService, private val blockchain: Blockchain, private val accountService: AccountService, apiTransactionManager: APITransactionManager) : CreateTransaction(arrayOf(APITag.ACCOUNTS, APITag.MINING, APITag.CREATE_TRANSACTION), apiTransactionManager, RECIPIENT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val account = parameterService.getSenderAccount(req)
        val recipient = ParameterParser.getRecipientId(req)
        val recipientAccount = accountService.getAccount(recipient)
        if (recipientAccount == null || recipientAccount.publicKey == null) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 8)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "recipient account does not have public key")
            return response
        }
        val attachment = Attachment.BurstMiningRewardRecipientAssignment(blockchain.height)
        return createTransaction(req, account, recipient, 0, attachment)
    }

}
