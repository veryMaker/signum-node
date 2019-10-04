package brs.http

import brs.http.JSONResponses.INCORRECT_ENCRYPTED_MESSAGE
import brs.http.JSONResponses.INCORRECT_RECIPIENT
import brs.http.common.Parameters.MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER
import brs.http.common.Parameters.MESSAGE_TO_ENCRYPT_PARAMETER
import brs.http.common.Parameters.RECIPIENT_PARAMETER
import brs.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.services.AccountService
import brs.services.ParameterService
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class EncryptTo(private val parameterService: ParameterService, private val accountService: AccountService) : APIServlet.JsonRequestHandler(arrayOf(APITag.MESSAGES), RECIPIENT_PARAMETER, MESSAGE_TO_ENCRYPT_PARAMETER, MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER, SECRET_PHRASE_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {

        val recipientId = ParameterParser.getRecipientId(request)
        val recipientAccount = accountService.getAccount(recipientId)
        if (recipientAccount?.publicKey == null) {
            return INCORRECT_RECIPIENT
        }

        val encryptedData = parameterService.getEncryptedMessage(request, recipientAccount, recipientAccount.publicKey!!) ?: return INCORRECT_ENCRYPTED_MESSAGE
        return JSONData.encryptedData(encryptedData)
    }
}
