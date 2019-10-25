package brs.services.impl

import brs.*
import brs.at.AT
import brs.crypto.Crypto
import brs.crypto.EncryptedData
import brs.http.JSONResponses.HEIGHT_NOT_AVAILABLE
import brs.http.JSONResponses.INCORRECT_ACCOUNT
import brs.http.JSONResponses.INCORRECT_ALIAS
import brs.http.JSONResponses.INCORRECT_ASSET
import brs.http.JSONResponses.INCORRECT_AT
import brs.http.JSONResponses.INCORRECT_ENCRYPTED_MESSAGE
import brs.http.JSONResponses.INCORRECT_GOODS
import brs.http.JSONResponses.INCORRECT_HEIGHT
import brs.http.JSONResponses.INCORRECT_NUMBER_OF_CONFIRMATIONS
import brs.http.JSONResponses.INCORRECT_PLAIN_MESSAGE
import brs.http.JSONResponses.INCORRECT_PUBLIC_KEY
import brs.http.JSONResponses.INCORRECT_PURCHASE
import brs.http.JSONResponses.INCORRECT_RECIPIENT
import brs.http.JSONResponses.MISSING_ACCOUNT
import brs.http.JSONResponses.MISSING_ALIAS_OR_ALIAS_NAME
import brs.http.JSONResponses.MISSING_ASSET
import brs.http.JSONResponses.MISSING_AT
import brs.http.JSONResponses.MISSING_GOODS
import brs.http.JSONResponses.MISSING_PURCHASE
import brs.http.JSONResponses.MISSING_SECRET_PHRASE
import brs.http.JSONResponses.MISSING_SECRET_PHRASE_OR_PUBLIC_KEY
import brs.http.JSONResponses.MISSING_TRANSACTION_BYTES_OR_JSON
import brs.http.JSONResponses.UNKNOWN_ACCOUNT
import brs.http.JSONResponses.UNKNOWN_ALIAS
import brs.http.JSONResponses.UNKNOWN_ASSET
import brs.http.JSONResponses.UNKNOWN_AT
import brs.http.JSONResponses.UNKNOWN_GOODS
import brs.http.ParameterException
import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.ALIAS_NAME_PARAMETER
import brs.http.common.Parameters.ALIAS_PARAMETER
import brs.http.common.Parameters.ASSET_PARAMETER
import brs.http.common.Parameters.AT_PARAMETER
import brs.http.common.Parameters.ENCRYPTED_MESSAGE_DATA_PARAMETER
import brs.http.common.Parameters.ENCRYPTED_MESSAGE_NONCE_PARAMETER
import brs.http.common.Parameters.ENCRYPT_TO_SELF_MESSAGE_DATA
import brs.http.common.Parameters.ENCRYPT_TO_SELF_MESSAGE_NONCE
import brs.http.common.Parameters.GOODS_PARAMETER
import brs.http.common.Parameters.HEIGHT_PARAMETER
import brs.http.common.Parameters.INCLUDE_INDIRECT_PARAMETER
import brs.http.common.Parameters.MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER
import brs.http.common.Parameters.MESSAGE_TO_ENCRYPT_PARAMETER
import brs.http.common.Parameters.MESSAGE_TO_ENCRYPT_TO_SELF_IS_TEXT_PARAMETER
import brs.http.common.Parameters.MESSAGE_TO_ENCRYPT_TO_SELF_PARAMETER
import brs.http.common.Parameters.NUMBER_OF_CONFIRMATIONS_PARAMETER
import brs.http.common.Parameters.PUBLIC_KEY_PARAMETER
import brs.http.common.Parameters.PURCHASE_PARAMETER
import brs.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.http.common.Parameters.isFalse
import brs.http.common.Parameters.isTrue
import brs.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE
import brs.services.ParameterService
import brs.util.convert.*
import brs.util.logging.safeDebug
import brs.util.mustGetAsJsonObject
import brs.util.parseJson
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest

class ParameterServiceImpl(private val dp: DependencyProvider) : ParameterService {

    override fun getAccount(request: HttpServletRequest): Account {
        val accountId = request.getParameter(ACCOUNT_PARAMETER).emptyToNull()
                ?: throw ParameterException(MISSING_ACCOUNT)
        try {
            return dp.accountService.getAccount(accountId.parseAccountId())
                    ?: throw ParameterException(UNKNOWN_ACCOUNT)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_ACCOUNT)
        }

    }

    override fun getAccounts(request: HttpServletRequest): List<Account> {
        val accountIDs = request.getParameterValues(ACCOUNT_PARAMETER)
        if (accountIDs == null || accountIDs.isEmpty()) {
            throw ParameterException(MISSING_ACCOUNT)
        }
        val result = mutableListOf<Account>()
        for (accountValue in accountIDs) {
            if (accountValue == null || accountValue.isEmpty()) {
                continue
            }
            try {
                val account = dp.accountService.getAccount(accountValue.parseAccountId())
                        ?: throw ParameterException(UNKNOWN_ACCOUNT)
                result.add(account)
            } catch (e: RuntimeException) {
                throw ParameterException(INCORRECT_ACCOUNT)
            }

        }
        return result
    }

    override fun getSenderAccount(request: HttpServletRequest): Account {
        val secretPhrase = request.getParameter(SECRET_PHRASE_PARAMETER).emptyToNull()
        val publicKeyString = request.getParameter(PUBLIC_KEY_PARAMETER).emptyToNull()
        return when {
            secretPhrase != null -> dp.accountService.getAccount(Crypto.getPublicKey(secretPhrase))
            publicKeyString != null -> try {
                dp.accountService.getAccount(publicKeyString.parseHexString())
            } catch (e: RuntimeException) {
                throw ParameterException(INCORRECT_PUBLIC_KEY)
            }
            else -> throw ParameterException(MISSING_SECRET_PHRASE_OR_PUBLIC_KEY)
        } ?: throw ParameterException(UNKNOWN_ACCOUNT)
    }

    override fun getAlias(request: HttpServletRequest): Alias {
        val aliasId: Long
        try {
            aliasId = request.getParameter(ALIAS_PARAMETER).emptyToNull().parseUnsignedLong()
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_ALIAS)
        }

        val aliasName = request.getParameter(ALIAS_NAME_PARAMETER).emptyToNull()
        return when {
            aliasId != 0L -> dp.aliasService.getAlias(aliasId)
            aliasName != null -> dp.aliasService.getAlias(aliasName)
            else -> throw ParameterException(MISSING_ALIAS_OR_ALIAS_NAME)
        } ?: throw ParameterException(UNKNOWN_ALIAS)
    }

    override fun getAsset(request: HttpServletRequest): Asset {
        val assetValue = request.getParameter(ASSET_PARAMETER).emptyToNull()
                ?: throw ParameterException(MISSING_ASSET)
        val asset: Asset?
        try {
            val assetId = assetValue.parseUnsignedLong()
            asset = dp.assetExchange.getAsset(assetId)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_ASSET)
        }

        if (asset == null) {
            throw ParameterException(UNKNOWN_ASSET)
        }
        return asset
    }

    override fun getGoods(request: HttpServletRequest): DigitalGoodsStore.Goods {
        val goodsValue = request.getParameter(GOODS_PARAMETER).emptyToNull()
                ?: throw ParameterException(MISSING_GOODS)

        try {
            val goodsId = goodsValue.parseUnsignedLong()
            return dp.digitalGoodsStoreService.getGoods(goodsId) ?: throw ParameterException(UNKNOWN_GOODS)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_GOODS)
        }

    }

    override fun getPurchase(request: HttpServletRequest): DigitalGoodsStore.Purchase {
        val purchaseIdString = request.getParameter(PURCHASE_PARAMETER).emptyToNull()
                ?: throw ParameterException(MISSING_PURCHASE)
        try {
            return dp.digitalGoodsStoreService.getPurchase(purchaseIdString.parseUnsignedLong())
                    ?: throw ParameterException(INCORRECT_PURCHASE)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_PURCHASE)
        }

    }

    override fun getEncryptedMessage(request: HttpServletRequest, recipientAccount: Account?, publicKey: ByteArray?): EncryptedData? {
        val data = request.getParameter(ENCRYPTED_MESSAGE_DATA_PARAMETER).emptyToNull()
        val nonce = request.getParameter(ENCRYPTED_MESSAGE_NONCE_PARAMETER).emptyToNull()
        if (data != null && nonce != null) {
            try {
                return EncryptedData(data.parseHexString(), nonce.parseHexString())
            } catch (e: RuntimeException) {
                throw ParameterException(INCORRECT_ENCRYPTED_MESSAGE)
            }

        }
        val plainMessage = request.getParameter(MESSAGE_TO_ENCRYPT_PARAMETER).emptyToNull() ?: return null

        val secretPhrase = getSecretPhrase(request)
        val isText = isTrue(request.getParameter(MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER))
        try {
            val plainMessageBytes = if (isText) plainMessage.toBytes() else plainMessage.parseHexString()
            return when {
                recipientAccount?.publicKey != null -> recipientAccount.encryptTo(plainMessageBytes, secretPhrase)
                publicKey != null -> Account.encryptTo(plainMessageBytes, secretPhrase, publicKey)
                else -> throw ParameterException(INCORRECT_RECIPIENT)
            }
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_PLAIN_MESSAGE)
        }

    }

    override fun getEncryptToSelfMessage(request: HttpServletRequest): EncryptedData? {
        val data = request.getParameter(ENCRYPT_TO_SELF_MESSAGE_DATA).emptyToNull()
        val nonce = request.getParameter(ENCRYPT_TO_SELF_MESSAGE_NONCE).emptyToNull()
        if (data != null && nonce != null) {
            try {
                return EncryptedData(data.parseHexString(), nonce.parseHexString())
            } catch (e: RuntimeException) {
                throw ParameterException(INCORRECT_ENCRYPTED_MESSAGE)
            }

        }
        val plainMessage = request.getParameter(MESSAGE_TO_ENCRYPT_TO_SELF_PARAMETER).emptyToNull() ?: return null
        val secretPhrase = getSecretPhrase(request)
        val senderAccount = dp.accountService.getAccount(Crypto.getPublicKey(secretPhrase))
        val isText = !isFalse(request.getParameter(MESSAGE_TO_ENCRYPT_TO_SELF_IS_TEXT_PARAMETER))
        try {
            val plainMessageBytes = if (isText) plainMessage.toBytes() else plainMessage.parseHexString()
            return senderAccount?.encryptTo(plainMessageBytes, secretPhrase)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_PLAIN_MESSAGE)
        }

    }

    override fun getSecretPhrase(request: HttpServletRequest): String {
        return request.getParameter(SECRET_PHRASE_PARAMETER).emptyToNull()
                ?: throw ParameterException(MISSING_SECRET_PHRASE)
    }

    override fun getNumberOfConfirmations(request: HttpServletRequest): Int {
        val numberOfConfirmationsValue = request.getParameter(NUMBER_OF_CONFIRMATIONS_PARAMETER).emptyToNull()
        if (numberOfConfirmationsValue != null) {
            try {
                val numberOfConfirmations = Integer.parseInt(numberOfConfirmationsValue)
                if (numberOfConfirmations <= dp.blockchain.height) {
                    return numberOfConfirmations
                }
                throw ParameterException(INCORRECT_NUMBER_OF_CONFIRMATIONS)
            } catch (e: NumberFormatException) {
                throw ParameterException(INCORRECT_NUMBER_OF_CONFIRMATIONS)
            }

        }
        return 0
    }

    override fun getHeight(request: HttpServletRequest): Int {
        val heightValue = request.getParameter(HEIGHT_PARAMETER).emptyToNull()
        if (heightValue != null) {
            try {
                val height = Integer.parseInt(heightValue)
                if (height < 0 || height > dp.blockchain.height) {
                    throw ParameterException(INCORRECT_HEIGHT)
                }
                if (height < dp.blockchainProcessor.minRollbackHeight) {
                    throw ParameterException(HEIGHT_NOT_AVAILABLE)
                }
                return height
            } catch (e: NumberFormatException) {
                throw ParameterException(INCORRECT_HEIGHT)
            }

        }
        return -1
    }

    override fun parseTransaction(transactionBytes: String?, transactionJSON: String?): Transaction {
        return when {
            transactionBytes != null -> try {
                val bytes = transactionBytes.parseHexString()
                dp.transactionProcessor.parseTransaction(bytes)
            } catch (e: BurstException.ValidationException) {
                logger.safeDebug(e) { e.message } // TODO remove?
                val response = JsonObject()
                response.addProperty(ERROR_CODE_RESPONSE, 4)
                response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Incorrect transactionBytes: $e")
                throw ParameterException(response)
            } catch (e: RuntimeException) {
                logger.safeDebug(e) { e.message }
                val response = JsonObject()
                response.addProperty(ERROR_CODE_RESPONSE, 4)
                response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Incorrect transactionBytes: $e")
                throw ParameterException(response)
            }
            transactionJSON != null -> try {
                val json = transactionJSON.parseJson().mustGetAsJsonObject("transaction")
                dp.transactionProcessor.parseTransaction(json)
            } catch (e: BurstException.ValidationException) {
                logger.safeDebug(e) { e.message }
                val response = JsonObject()
                response.addProperty(ERROR_CODE_RESPONSE, 4)
                response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Incorrect transactionJSON: $e")
                throw ParameterException(response)
            } catch (e: RuntimeException) {
                logger.safeDebug(e) { e.message }
                val response = JsonObject()
                response.addProperty(ERROR_CODE_RESPONSE, 4)
                response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Incorrect transactionJSON: $e")
                throw ParameterException(response)
            }
            else -> throw ParameterException(MISSING_TRANSACTION_BYTES_OR_JSON)
        }
    }

    override fun getAT(request: HttpServletRequest): AT {
        val atValue = request.getParameter(AT_PARAMETER).emptyToNull() ?: throw ParameterException(MISSING_AT)
        val at: AT?
        try {
            val atId = atValue.parseUnsignedLong()
            at = dp.atService.getAT(atId)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_AT)
        }

        if (at == null) {
            throw ParameterException(UNKNOWN_AT)
        }
        return at
    }

    override fun getIncludeIndirect(request: HttpServletRequest): Boolean {
        return request.getParameter(INCLUDE_INDIRECT_PARAMETER)?.toBoolean() ?: false
    }

    companion object {

        private val logger = LoggerFactory.getLogger(ParameterServiceImpl::class.java)
    }
}
