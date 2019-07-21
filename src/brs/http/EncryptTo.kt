package brs.http

import brs.Account
import brs.BurstException
import brs.crypto.EncryptedData
import brs.services.AccountService
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.INCORRECT_RECIPIENT
import brs.http.common.Parameters.*

internal class EncryptTo(private val parameterService: ParameterService, private val accountService: AccountService) : APIServlet.JsonRequestHandler(arrayOf(APITag.MESSAGES), RECIPIENT_PARAMETER, MESSAGE_TO_ENCRYPT_PARAMETER, MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER, SECRET_PHRASE_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val recipientId = ParameterParser.getRecipientId(req)
        val recipientAccount = accountService.getAccount(recipientId)
        if (recipientAccount == null || recipientAccount.publicKey == null) {
            return INCORRECT_RECIPIENT
        }

        val encryptedData = parameterService.getEncryptedMessage(req, recipientAccount, recipientAccount.publicKey)
        return JSONData.encryptedData(encryptedData)

    }

}
