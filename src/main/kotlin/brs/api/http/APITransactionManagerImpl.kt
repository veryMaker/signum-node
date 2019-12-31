package brs.api.http

import brs.api.http.common.JSONData
import brs.api.http.common.JSONResponses.FEATURE_NOT_AVAILABLE
import brs.api.http.common.JSONResponses.INCORRECT_ARBITRARY_MESSAGE
import brs.api.http.common.JSONResponses.INCORRECT_DEADLINE
import brs.api.http.common.JSONResponses.INCORRECT_FEE
import brs.api.http.common.JSONResponses.INCORRECT_REFERENCED_TRANSACTION
import brs.api.http.common.JSONResponses.MISSING_DEADLINE
import brs.api.http.common.JSONResponses.MISSING_SECRET_PHRASE
import brs.api.http.common.JSONResponses.NOT_ENOUGH_FUNDS
import brs.api.http.common.Parameters
import brs.api.http.common.Parameters.BROADCAST_PARAMETER
import brs.api.http.common.Parameters.COMMENT_PARAMETER
import brs.api.http.common.Parameters.DEADLINE_PARAMETER
import brs.api.http.common.Parameters.MESSAGE_IS_TEXT_PARAMETER
import brs.api.http.common.Parameters.MESSAGE_PARAMETER
import brs.api.http.common.Parameters.PUBLIC_KEY_PARAMETER
import brs.api.http.common.Parameters.RECIPIENT_PUBLIC_KEY_PARAMETER
import brs.api.http.common.Parameters.REFERENCED_TRANSACTION_FULL_HASH_PARAMETER
import brs.api.http.common.Parameters.REFERENCED_TRANSACTION_PARAMETER
import brs.api.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.api.http.common.ResultFields.BROADCASTED_RESPONSE
import brs.api.http.common.ResultFields.ERROR_RESPONSE
import brs.api.http.common.ResultFields.FULL_HASH_RESPONSE
import brs.api.http.common.ResultFields.NUMBER_PEERS_SENT_TO_RESPONSE
import brs.api.http.common.ResultFields.SIGNATURE_HASH_RESPONSE
import brs.api.http.common.ResultFields.TRANSACTION_BYTES_RESPONSE
import brs.api.http.common.ResultFields.TRANSACTION_JSON_RESPONSE
import brs.api.http.common.ResultFields.TRANSACTION_RESPONSE
import brs.api.http.common.ResultFields.UNSIGNED_TRANSACTION_BYTES_RESPONSE
import brs.entity.Account
import brs.entity.DependencyProvider
import brs.objects.Constants.EMPTY_BYTE_ARRAY
import brs.objects.FluxValues
import brs.transaction.appendix.Appendix.*
import brs.transaction.appendix.Attachment
import brs.util.BurstException
import brs.util.convert.emptyToNull
import brs.util.convert.parseHexString
import brs.util.convert.safeAdd
import brs.util.convert.toHexString
import brs.util.crypto.Crypto
import brs.util.jetty.get
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

class APITransactionManagerImpl(private val dp: DependencyProvider) : APITransactionManager {
    override fun createTransaction(
        request: HttpServletRequest,
        senderAccount: Account,
        recipientId: Long?,
        amountPlanck: Long,
        attachment: Attachment,
        minimumFeePlanck: Long
    ): JsonElement {
        val blockchainHeight = dp.blockchainService.height
        val deadlineValue = request[DEADLINE_PARAMETER]
        val referencedTransactionFullHash =
            request[REFERENCED_TRANSACTION_FULL_HASH_PARAMETER].emptyToNull()
        val referencedTransactionId = request[REFERENCED_TRANSACTION_PARAMETER].emptyToNull()
        val secretPhrase = request[SECRET_PHRASE_PARAMETER].emptyToNull()
        val publicKeyValue = request[PUBLIC_KEY_PARAMETER].emptyToNull()
        val recipientPublicKeyValue = request[RECIPIENT_PUBLIC_KEY_PARAMETER].emptyToNull()
        val broadcast = !Parameters.isFalse(request[BROADCAST_PARAMETER])

        var encryptedMessage: EncryptedMessage? = null

        if (attachment.transactionType.hasRecipient()) {
            val encryptedData = dp.parameterService.getEncryptedMessage(
                request,
                dp.accountService.getAccount(recipientId!!),
                recipientPublicKeyValue?.parseHexString()
            )
            if (encryptedData != null) {
                encryptedMessage = EncryptedMessage(
                    dp,
                    encryptedData,
                    blockchainHeight
                )
            }
        }

        var encryptToSelfMessage: EncryptToSelfMessage? = null
        val encryptedToSelfData = dp.parameterService.getEncryptToSelfMessage(request)
        if (encryptedToSelfData != null) {
            encryptToSelfMessage = EncryptToSelfMessage(
                dp,
                encryptedToSelfData,
                blockchainHeight
            )
        }
        var message: Message? = null
        val messageValue = request[MESSAGE_PARAMETER].emptyToNull()
        if (messageValue != null) {
            val messageIsText = dp.fluxCapacitorService.getValue(
                FluxValues.DIGITAL_GOODS_STORE,
                blockchainHeight
            ) && !Parameters.isFalse(request[MESSAGE_IS_TEXT_PARAMETER])
            try {
                message = if (messageIsText) Message(dp, messageValue, blockchainHeight) else Message(
                    dp,
                    messageValue.parseHexString(),
                    blockchainHeight
                )
            } catch (e: Exception) {
                throw ParameterException(INCORRECT_ARBITRARY_MESSAGE)
            }

        } else if (attachment is Attachment.ColoredCoinsAssetTransfer && dp.fluxCapacitorService.getValue(
                FluxValues.DIGITAL_GOODS_STORE,
                blockchainHeight
            )
        ) {
            val commentValue = request[COMMENT_PARAMETER].emptyToNull()
            if (commentValue != null) {
                message = Message(dp, commentValue, blockchainHeight)
            }
        } else if (attachment is Attachment.ArbitraryMessage && !dp.fluxCapacitorService.getValue(
                FluxValues.DIGITAL_GOODS_STORE,
                blockchainHeight
            )
        ) {
            message = Message(dp, EMPTY_BYTE_ARRAY, blockchainHeight)
        }
        var publicKeyAnnouncement: PublicKeyAnnouncement? = null
        val recipientPublicKey = request[RECIPIENT_PUBLIC_KEY_PARAMETER].emptyToNull()
        if (recipientPublicKey != null && dp.fluxCapacitorService.getValue(
                FluxValues.DIGITAL_GOODS_STORE,
                blockchainHeight
            )
        ) {
            publicKeyAnnouncement = PublicKeyAnnouncement(dp, recipientPublicKey.parseHexString(), blockchainHeight)
        }

        if (secretPhrase == null && publicKeyValue == null) {
            return MISSING_SECRET_PHRASE
        } else if (deadlineValue == null) {
            return MISSING_DEADLINE
        }

        val deadline: Short
        try {
            deadline = deadlineValue.toShort()
            if (deadline < 1 || deadline > 1440) {
                return INCORRECT_DEADLINE
            }
        } catch (e: NumberFormatException) {
            return INCORRECT_DEADLINE
        }

        val feePlanck = ParameterParser.getFeePlanck(request)
        if (feePlanck < minimumFeePlanck) {
            return INCORRECT_FEE
        }

        try {
            if (amountPlanck.safeAdd(feePlanck) > senderAccount.unconfirmedBalancePlanck) {
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
        val publicKey =
            if (secretPhrase != null) Crypto.getPublicKey(secretPhrase) else publicKeyValue?.parseHexString()

        try {
            val builder = dp.transactionProcessorService.newTransactionBuilder(
                publicKey!!,
                amountPlanck,
                feePlanck,
                deadline,
                attachment
            )
            if (!referencedTransactionFullHash.isNullOrEmpty()) {
                builder.referencedTransactionFullHash(referencedTransactionFullHash.parseHexString())
            }
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
                response.addProperty(FULL_HASH_RESPONSE, transaction.fullHash.toHexString())
                response.addProperty(TRANSACTION_BYTES_RESPONSE, transaction.toBytes().toHexString())
                response.addProperty(
                    SIGNATURE_HASH_RESPONSE,
                    Crypto.sha256().digest(transaction.signature).toHexString()
                )
                if (broadcast) {
                    response.addProperty(
                        NUMBER_PEERS_SENT_TO_RESPONSE,
                        dp.transactionProcessorService.broadcast(transaction)
                    )
                    response.addProperty(BROADCASTED_RESPONSE, true)
                } else {
                    response.addProperty(BROADCASTED_RESPONSE, false)
                }
            } else {
                response.addProperty(BROADCASTED_RESPONSE, false)
            }
            response.addProperty(UNSIGNED_TRANSACTION_BYTES_RESPONSE, transaction.toUnsignedBytes().toHexString())
            response.add(TRANSACTION_JSON_RESPONSE, JSONData.unconfirmedTransaction(transaction))

        } catch (e: BurstException.NotYetEnabledException) {
            return FEATURE_NOT_AVAILABLE
        } catch (e: BurstException.ValidationException) {
            response.addProperty(ERROR_RESPONSE, e.message)
        }

        return response
    }
}
