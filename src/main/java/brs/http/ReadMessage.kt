package brs.http


import brs.Account
import brs.Blockchain
import brs.Transaction
import brs.crypto.Crypto
import brs.http.JSONResponses.INCORRECT_TRANSACTION
import brs.http.JSONResponses.MISSING_TRANSACTION
import brs.http.JSONResponses.NO_MESSAGE
import brs.http.JSONResponses.UNKNOWN_TRANSACTION
import brs.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.http.common.Parameters.TRANSACTION_PARAMETER
import brs.services.AccountService
import brs.util.convert.emptyToNull
import brs.util.convert.parseUnsignedLong
import brs.util.convert.toHexString
import brs.util.convert.toUtf8String
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest

internal class ReadMessage(private val blockchain: Blockchain, private val accountService: AccountService) : APIServlet.JsonRequestHandler(arrayOf(APITag.MESSAGES), TRANSACTION_PARAMETER, SECRET_PHRASE_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        val transactionIdString = request.getParameter(TRANSACTION_PARAMETER).emptyToNull()
                ?: return MISSING_TRANSACTION

        val transaction: Transaction?
        try {
            transaction = blockchain.getTransaction(transactionIdString.parseUnsignedLong())
            if (transaction == null) {
                return UNKNOWN_TRANSACTION
            }
        } catch (e: RuntimeException) {
            return INCORRECT_TRANSACTION
        }

        val response = JsonObject()
        val senderAccount = accountService.getAccount(transaction.senderId)!!
        val message = transaction.message
        val encryptedMessage = transaction.encryptedMessage
        val encryptToSelfMessage = transaction.encryptToSelfMessage
        if (message == null && encryptedMessage == null && encryptToSelfMessage == null) {
            return NO_MESSAGE
        }
        if (message != null) {
            response.addProperty("message", if (message.isText) message.messageBytes!!.toUtf8String() else message.messageBytes.toHexString())
        }
        val secretPhrase = request.getParameter(SECRET_PHRASE_PARAMETER).emptyToNull()
        if (secretPhrase != null) {
            if (encryptedMessage != null) {
                val readerAccountId = Account.getId(Crypto.getPublicKey(secretPhrase))
                val account = if (senderAccount.id == readerAccountId) accountService.getAccount(transaction.recipientId) else senderAccount
                if (account != null) {
                    try {
                        val decrypted = account.decryptFrom(encryptedMessage.encryptedData, secretPhrase)
                        response.addProperty("decryptedMessage", if (encryptedMessage.isText) decrypted.toUtf8String() else decrypted.toHexString())
                    } catch (e: RuntimeException) {
                        logger.debug("Decryption of message to recipient failed: {}", e)
                    }

                }
            }
            if (encryptToSelfMessage != null) {
                val account = accountService.getAccount(Crypto.getPublicKey(secretPhrase))
                if (account != null) {
                    try {
                        val decrypted = account.decryptFrom(encryptToSelfMessage.encryptedData, secretPhrase)
                        response.addProperty("decryptedMessageToSelf", if (encryptToSelfMessage.isText) decrypted.toUtf8String() else decrypted.toHexString())
                    } catch (e: RuntimeException) {
                        logger.debug("Decryption of message to self failed: {}", e)
                    }

                }
            }
        }
        return response
    }

    companion object {

        private val logger = LoggerFactory.getLogger(ReadMessage::class.java)
    }

}
