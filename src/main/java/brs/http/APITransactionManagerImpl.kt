package brs.http

import brs.*
import brs.Appendix.EncryptToSelfMessage
import brs.Appendix.EncryptedMessage
import brs.Appendix.Message
import brs.Appendix.PublicKeyAnnouncement
import brs.Transaction.Builder
import brs.crypto.Crypto
import brs.crypto.EncryptedData
import brs.fluxcapacitor.FluxValues
import brs.http.JSONResponses.FEATURE_NOT_AVAILABLE
import brs.http.JSONResponses.INCORRECT_ARBITRARY_MESSAGE
import brs.http.JSONResponses.INCORRECT_DEADLINE
import brs.http.JSONResponses.INCORRECT_FEE
import brs.http.JSONResponses.INCORRECT_REFERENCED_TRANSACTION
import brs.http.JSONResponses.MISSING_DEADLINE
import brs.http.JSONResponses.MISSING_SECRET_PHRASE
import brs.http.JSONResponses.NOT_ENOUGH_FUNDS
import brs.http.common.Parameters
import brs.services.AccountService
import brs.services.ParameterService
import brs.services.TransactionService
import brs.util.Convert
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest
import brs.http.common.ResultFields.*
import brs.http.common.ResultFields.FULL_HASH_RESPONSE

class APITransactionManagerImpl(private val dp: DependencyProvider) : APITransactionManager {

    @Throws(BurstException::class)
    override fun createTransaction(req: HttpServletRequest, senderAccount: Account, recipientId: Long?, amountNQT: Long, attachment: Attachment, minimumFeeNQT: Long): JsonElement {
        val blockchainHeight = dp.blockchain.height
        val deadlineValue = req.getParameter(DEADLINE_PARAMETER)
        val referencedTransactionFullHash = Convert.emptyToNull(req.getParameter(REFERENCED_TRANSACTION_FULL_HASH_PARAMETER))
        val referencedTransactionId = Convert.emptyToNull(req.getParameter(REFERENCED_TRANSACTION_PARAMETER))
        val secretPhrase = Convert.emptyToNull(req.getParameter(SECRET_PHRASE_PARAMETER))
        val publicKeyValue = Convert.emptyToNull(req.getParameter(PUBLIC_KEY_PARAMETER))
        val recipientPublicKeyValue = Convert.emptyToNull(req.getParameter(RECIPIENT_PUBLIC_KEY_PARAMETER))
        val broadcast = !Parameters.isFalse(req.getParameter(BROADCAST_PARAMETER))

        var encryptedMessage: EncryptedMessage? = null

        if (attachment.transactionType.hasRecipient()) {
            val encryptedData = dp.parameterService.getEncryptedMessage(req, dp.accountService.getAccount(recipientId!!), Convert.parseHexString(recipientPublicKeyValue))
            if (encryptedData != null) {
                encryptedMessage = EncryptedMessage(encryptedData, !Parameters.isFalse(req.getParameter(MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER)), blockchainHeight)
            }
        }

        var encryptToSelfMessage: EncryptToSelfMessage? = null
        val encryptedToSelfData = dp.parameterService.getEncryptToSelfMessage(req)
        if (encryptedToSelfData != null) {
            encryptToSelfMessage = EncryptToSelfMessage(encryptedToSelfData, !Parameters.isFalse(req.getParameter(MESSAGE_TO_ENCRYPT_TO_SELF_IS_TEXT_PARAMETER)), blockchainHeight)
        }
        var message: Message? = null
        val messageValue = Convert.emptyToNull(req.getParameter(MESSAGE_PARAMETER))
        if (messageValue != null) {
            val messageIsText = dp.fluxCapacitor.getValue(FluxValues.DIGITAL_GOODS_STORE, blockchainHeight) && !Parameters.isFalse(req.getParameter(MESSAGE_IS_TEXT_PARAMETER))
            try {
                message = if (messageIsText) Message(messageValue, blockchainHeight) else Message(Convert.parseHexString(messageValue)!!, blockchainHeight)
            } catch (e: RuntimeException) {
                throw ParameterException(INCORRECT_ARBITRARY_MESSAGE)
            }

        } else if (attachment is Attachment.ColoredCoinsAssetTransfer && dp.fluxCapacitor.getValue(FluxValues.DIGITAL_GOODS_STORE, blockchainHeight)) {
            val commentValue = Convert.emptyToNull(req.getParameter(COMMENT_PARAMETER))
            if (commentValue != null) {
                message = Message(commentValue, blockchainHeight)
            }
        } else if (attachment === Attachment.ARBITRARY_MESSAGE && !dp.fluxCapacitor.getValue(FluxValues.DIGITAL_GOODS_STORE, blockchainHeight)) {
            message = Message(ByteArray(0), blockchainHeight)
        }
        var publicKeyAnnouncement: PublicKeyAnnouncement? = null
        val recipientPublicKey = Convert.emptyToNull(req.getParameter(RECIPIENT_PUBLIC_KEY_PARAMETER))
        if (recipientPublicKey != null && dp.fluxCapacitor.getValue(FluxValues.DIGITAL_GOODS_STORE, blockchainHeight)) {
            publicKeyAnnouncement = PublicKeyAnnouncement(Convert.parseHexString(recipientPublicKey)!!, blockchainHeight)
        }

        if (secretPhrase == null && publicKeyValue == null) {
            return MISSING_SECRET_PHRASE
        } else if (deadlineValue == null) {
            return MISSING_DEADLINE
        }

        val deadline: Short
        try {
            deadline = java.lang.Short.parseShort(deadlineValue)
            if (deadline < 1 || deadline > 1440) {
                return INCORRECT_DEADLINE
            }
        } catch (e: NumberFormatException) {
            return INCORRECT_DEADLINE
        }

        val feeNQT = ParameterParser.getFeeNQT(req)
        if (feeNQT < minimumFeeNQT) {
            return INCORRECT_FEE
        }

        try {
            if (Convert.safeAdd(amountNQT, feeNQT) > senderAccount.unconfirmedBalanceNQT) {
                return NOT_ENOUGH_FUNDS
            }
        } catch (e: ArithmeticException) {
            return NOT_ENOUGH_FUNDS
        }

        if (referencedTransactionId != null) {
            return INCORRECT_REFERENCED_TRANSACTION
        }

        val response = JsonObject()

        // shouldn't try to get publicKey from senderAccount as it may have not been set yet
        val publicKey = if (secretPhrase != null) Crypto.getPublicKey(secretPhrase) else Convert.parseHexString(publicKeyValue)

        try {
            val builder = dp.transactionProcessor.newTransactionBuilder(publicKey!!, amountNQT, feeNQT, deadline, attachment).referencedTransactionFullHash(referencedTransactionFullHash)
            if (attachment.transactionType.hasRecipient()) {
                builder.recipientId(recipientId!!)
            }
            if (encryptedMessage != null) {
                builder.encryptedMessage(encryptedMessage)
            }
            if (message != null) {
                builder.message(message)
            }
            if (publicKeyAnnouncement != null) {
                builder.publicKeyAnnouncement(publicKeyAnnouncement)
            }
            if (encryptToSelfMessage != null) {
                builder.encryptToSelfMessage(encryptToSelfMessage)
            }
            val transaction = builder.build()
            dp.transactionService.validate(transaction)

            if (secretPhrase != null) {
                transaction.sign(secretPhrase)
                dp.transactionService.validate(transaction) // 2nd validate may be needed if validation requires id to be known
                response.addProperty(TRANSACTION_RESPONSE, transaction.stringId)
                response.addProperty(FULL_HASH_RESPONSE, transaction.fullHash)
                response.addProperty(TRANSACTION_BYTES_RESPONSE, Convert.toHexString(transaction.bytes))
                response.addProperty(SIGNATURE_HASH_RESPONSE, Convert.toHexString(Crypto.sha256().digest(transaction.signature)))
                if (broadcast) {
                    response.addProperty(NUMBER_PEERS_SENT_TO_RESPONSE, dp.transactionProcessor.broadcast(transaction))
                    response.addProperty(BROADCASTED_RESPONSE, true)
                } else {
                    response.addProperty(BROADCASTED_RESPONSE, false)
                }
            } else {
                response.addProperty(BROADCASTED_RESPONSE, false)
            }
            response.addProperty(UNSIGNED_TRANSACTION_BYTES_RESPONSE, Convert.toHexString(transaction.unsignedBytes))
            response.add(TRANSACTION_JSON_RESPONSE, JSONData.unconfirmedTransaction(transaction))

        } catch (e: BurstException.NotYetEnabledException) {
            return FEATURE_NOT_AVAILABLE
        } catch (e: BurstException.ValidationException) {
            response.addProperty(ERROR_RESPONSE, e.message)
        }

        return response
    }
}
