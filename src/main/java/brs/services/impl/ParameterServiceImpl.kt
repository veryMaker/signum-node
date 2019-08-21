package brs.services.impl

import brs.*
import brs.assetexchange.AssetExchange
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
import brs.services.*
import brs.util.Convert
import brs.util.JSON
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory

import javax.servlet.http.HttpServletRequest
import java.util.ArrayList
import brs.http.common.Parameters.*
import brs.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE

class ParameterServiceImpl(private val accountService: AccountService, private val aliasService: AliasService, private val assetExchange: AssetExchange, private val dgsGoodsStoreService: DGSGoodsStoreService, private val blockchain: Blockchain,
                           private val blockchainProcessor: BlockchainProcessor,
                           private val transactionProcessor: TransactionProcessor, private val atService: ATService) : ParameterService {

    @Throws(BurstException::class)
    override fun getAccount(req: HttpServletRequest): Account {
        val accountId = Convert.emptyToNull(req.getParameter(ACCOUNT_PARAMETER))
                ?: throw ParameterException(MISSING_ACCOUNT)
        try {
            return accountService.getAccount(Convert.parseAccountId(accountId))
                    ?: throw ParameterException(UNKNOWN_ACCOUNT)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_ACCOUNT)
        }

    }

    @Throws(ParameterException::class)
    override fun getAccounts(req: HttpServletRequest): List<Account> {
        val accountIDs = req.getParameterValues(ACCOUNT_PARAMETER)
        if (accountIDs == null || accountIDs.isEmpty()) {
            throw ParameterException(MISSING_ACCOUNT)
        }
        val result = ArrayList<Account>()
        for (accountValue in accountIDs) {
            if (accountValue == null || accountValue.isEmpty()) {
                continue
            }
            try {
                val account = accountService.getAccount(Convert.parseAccountId(accountValue))
                        ?: throw ParameterException(UNKNOWN_ACCOUNT)
                result.add(account)
            } catch (e: RuntimeException) {
                throw ParameterException(INCORRECT_ACCOUNT)
            }

        }
        return result
    }

    @Throws(ParameterException::class)
    override fun getSenderAccount(req: HttpServletRequest): Account {
        val secretPhrase = Convert.emptyToNull(req.getParameter(SECRET_PHRASE_PARAMETER))
        val publicKeyString = Convert.emptyToNull(req.getParameter(PUBLIC_KEY_PARAMETER))
        return when {
            secretPhrase != null -> accountService.getAccount(Crypto.getPublicKey(secretPhrase))
            publicKeyString != null -> try {
                accountService.getAccount(Convert.parseHexString(publicKeyString))
            } catch (e: RuntimeException) {
                throw ParameterException(INCORRECT_PUBLIC_KEY)
            }
            else -> throw ParameterException(MISSING_SECRET_PHRASE_OR_PUBLIC_KEY)
        } ?: throw ParameterException(UNKNOWN_ACCOUNT)
    }

    @Throws(ParameterException::class)
    override fun getAlias(req: HttpServletRequest): Alias {
        val aliasId: Long
        try {
            aliasId = Convert.parseUnsignedLong(Convert.emptyToNull(req.getParameter(ALIAS_PARAMETER)))
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_ALIAS)
        }

        val aliasName = Convert.emptyToNull(req.getParameter(ALIAS_NAME_PARAMETER))
        return when {
            aliasId != 0L -> aliasService.getAlias(aliasId)
            aliasName != null -> aliasService.getAlias(aliasName)
            else -> throw ParameterException(MISSING_ALIAS_OR_ALIAS_NAME)
        } ?: throw ParameterException(UNKNOWN_ALIAS)
    }

    @Throws(ParameterException::class)
    override fun getAsset(req: HttpServletRequest): Asset {
        val assetValue = Convert.emptyToNull(req.getParameter(ASSET_PARAMETER))
                ?: throw ParameterException(MISSING_ASSET)
        val asset: Asset?
        try {
            val assetId = Convert.parseUnsignedLong(assetValue)
            asset = assetExchange.getAsset(assetId)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_ASSET)
        }

        if (asset == null) {
            throw ParameterException(UNKNOWN_ASSET)
        }
        return asset
    }

    @Throws(ParameterException::class)
    override fun getGoods(req: HttpServletRequest): DigitalGoodsStore.Goods {
        val goodsValue = Convert.emptyToNull(req.getParameter(GOODS_PARAMETER))
                ?: throw ParameterException(MISSING_GOODS)

        try {
            val goodsId = Convert.parseUnsignedLong(goodsValue)
            return dgsGoodsStoreService.getGoods(goodsId) ?: throw ParameterException(UNKNOWN_GOODS)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_GOODS)
        }

    }

    @Throws(ParameterException::class)
    override fun getPurchase(req: HttpServletRequest): DigitalGoodsStore.Purchase {
        val purchaseIdString = Convert.emptyToNull(req.getParameter(PURCHASE_PARAMETER))
                ?: throw ParameterException(MISSING_PURCHASE)
        try {
            return dgsGoodsStoreService.getPurchase(Convert.parseUnsignedLong(purchaseIdString))
                    ?: throw ParameterException(INCORRECT_PURCHASE)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_PURCHASE)
        }

    }

    @Throws(ParameterException::class)
    override fun getEncryptedMessage(req: HttpServletRequest, recipientAccount: Account?, publicKey: ByteArray?): EncryptedData? {
        val data = Convert.emptyToNull(req.getParameter(ENCRYPTED_MESSAGE_DATA_PARAMETER))
        val nonce = Convert.emptyToNull(req.getParameter(ENCRYPTED_MESSAGE_NONCE_PARAMETER))
        if (data != null && nonce != null) {
            try {
                return EncryptedData(Convert.parseHexString(data), Convert.parseHexString(nonce))
            } catch (e: RuntimeException) {
                throw ParameterException(INCORRECT_ENCRYPTED_MESSAGE)
            }

        }
        val plainMessage = Convert.emptyToNull(req.getParameter(MESSAGE_TO_ENCRYPT_PARAMETER)) ?: return null

        val secretPhrase = getSecretPhrase(req)
        val isText = isTrue(req.getParameter(MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER))
        try {
            val plainMessageBytes = if (isText) Convert.toBytes(plainMessage) else Convert.parseHexString(plainMessage)
            return if (recipientAccount != null && recipientAccount.publicKey != null) {
                recipientAccount.encryptTo(plainMessageBytes, secretPhrase)
            } else if (publicKey != null) {
                Account.encryptTo(plainMessageBytes, secretPhrase, publicKey)
            } else {
                throw ParameterException(INCORRECT_RECIPIENT)
            }
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_PLAIN_MESSAGE)
        }

    }

    @Throws(ParameterException::class)
    override fun getEncryptToSelfMessage(req: HttpServletRequest): EncryptedData? {
        val data = Convert.emptyToNull(req.getParameter(ENCRYPT_TO_SELF_MESSAGE_DATA))
        val nonce = Convert.emptyToNull(req.getParameter(ENCRYPT_TO_SELF_MESSAGE_NONCE))
        if (data != null && nonce != null) {
            try {
                return EncryptedData(Convert.parseHexString(data), Convert.parseHexString(nonce))
            } catch (e: RuntimeException) {
                throw ParameterException(INCORRECT_ENCRYPTED_MESSAGE)
            }

        }
        val plainMessage = Convert.emptyToNull(req.getParameter(MESSAGE_TO_ENCRYPT_TO_SELF_PARAMETER)) ?: return null
        val secretPhrase = getSecretPhrase(req)
        val senderAccount = accountService.getAccount(Crypto.getPublicKey(secretPhrase))
        val isText = !isFalse(req.getParameter(MESSAGE_TO_ENCRYPT_TO_SELF_IS_TEXT_PARAMETER))
        try {
            val plainMessageBytes = if (isText) Convert.toBytes(plainMessage) else Convert.parseHexString(plainMessage)
            return senderAccount.encryptTo(plainMessageBytes, secretPhrase)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_PLAIN_MESSAGE)
        }

    }

    @Throws(ParameterException::class)
    override fun getSecretPhrase(req: HttpServletRequest): String {
        return Convert.emptyToNull(req.getParameter(SECRET_PHRASE_PARAMETER))
                ?: throw ParameterException(MISSING_SECRET_PHRASE)
    }

    @Throws(ParameterException::class)
    override fun getNumberOfConfirmations(req: HttpServletRequest): Int {
        val numberOfConfirmationsValue = Convert.emptyToNull(req.getParameter(NUMBER_OF_CONFIRMATIONS_PARAMETER))
        if (numberOfConfirmationsValue != null) {
            try {
                val numberOfConfirmations = Integer.parseInt(numberOfConfirmationsValue)
                if (numberOfConfirmations <= blockchain.height) {
                    return numberOfConfirmations
                }
                throw ParameterException(INCORRECT_NUMBER_OF_CONFIRMATIONS)
            } catch (e: NumberFormatException) {
                throw ParameterException(INCORRECT_NUMBER_OF_CONFIRMATIONS)
            }

        }
        return 0
    }

    @Throws(ParameterException::class)
    override fun getHeight(req: HttpServletRequest): Int {
        val heightValue = Convert.emptyToNull(req.getParameter(HEIGHT_PARAMETER))
        if (heightValue != null) {
            try {
                val height = Integer.parseInt(heightValue)
                if (height < 0 || height > blockchain.height) {
                    throw ParameterException(INCORRECT_HEIGHT)
                }
                if (height < blockchainProcessor.minRollbackHeight) {
                    throw ParameterException(HEIGHT_NOT_AVAILABLE)
                }
                return height
            } catch (e: NumberFormatException) {
                throw ParameterException(INCORRECT_HEIGHT)
            }

        }
        return -1
    }

    @Throws(ParameterException::class)
    override fun parseTransaction(transactionBytes: String?, transactionJSON: String?): Transaction {
        if (transactionBytes == null && transactionJSON == null) {
            throw ParameterException(MISSING_TRANSACTION_BYTES_OR_JSON)
        }
        return if (transactionBytes != null) {
            try {
                val bytes = Convert.parseHexString(transactionBytes)
                transactionProcessor.parseTransaction(bytes!!)
            } catch (e: BurstException.ValidationException) {
                logger.debug(e.message, e) // TODO remove?
                val response = JsonObject()
                response.addProperty(ERROR_CODE_RESPONSE, 4)
                response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Incorrect transactionBytes: $e")
                throw ParameterException(response)
            } catch (e: RuntimeException) {
                logger.debug(e.message, e)
                val response = JsonObject()
                response.addProperty(ERROR_CODE_RESPONSE, 4)
                response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Incorrect transactionBytes: $e")
                throw ParameterException(response)
            }

        } else {
            try {
                val json = JSON.getAsJsonObject(JSON.parse(transactionJSON!!))
                transactionProcessor.parseTransaction(json)
            } catch (e: BurstException.ValidationException) {
                logger.debug(e.message, e)
                val response = JsonObject()
                response.addProperty(ERROR_CODE_RESPONSE, 4)
                response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Incorrect transactionJSON: $e")
                throw ParameterException(response)
            } catch (e: RuntimeException) {
                logger.debug(e.message, e)
                val response = JsonObject()
                response.addProperty(ERROR_CODE_RESPONSE, 4)
                response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Incorrect transactionJSON: $e")
                throw ParameterException(response)
            }

        }
    }

    @Throws(ParameterException::class)
    override fun getAT(req: HttpServletRequest): AT {
        val atValue = Convert.emptyToNull(req.getParameter(AT_PARAMETER)) ?: throw ParameterException(MISSING_AT)
        val at: AT?
        try {
            val atId = Convert.parseUnsignedLong(atValue)
            at = atService.getAT(atId)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_AT)
        }

        if (at == null) {
            throw ParameterException(UNKNOWN_AT)
        }
        return at
    }

    override fun getIncludeIndirect(req: HttpServletRequest): Boolean {
        return java.lang.Boolean.parseBoolean(req.getParameter(INCLUDE_INDIRECT_PARAMETER))
    }

    companion object {

        private val logger = LoggerFactory.getLogger(ParameterServiceImpl::class.java)
    }
}
