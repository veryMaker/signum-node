package brs.services

import brs.*
import brs.at.AT
import brs.crypto.EncryptedData
import brs.http.ParameterException

import javax.servlet.http.HttpServletRequest

interface ParameterService {
    @Throws(BurstException::class)
    fun getAccount(request: HttpServletRequest): Account

    @Throws(ParameterException::class)
    fun getAccounts(request: HttpServletRequest): List<Account>

    @Throws(ParameterException::class)
    fun getSenderAccount(request: HttpServletRequest): Account // TODO these should be nullable.

    @Throws(ParameterException::class)
    fun getAlias(request: HttpServletRequest): Alias

    @Throws(ParameterException::class)
    fun getAsset(request: HttpServletRequest): Asset

    @Throws(ParameterException::class)
    fun getGoods(request: HttpServletRequest): DigitalGoodsStore.Goods

    @Throws(ParameterException::class)
    fun getPurchase(request: HttpServletRequest): DigitalGoodsStore.Purchase

    @Throws(ParameterException::class)
    fun getEncryptedMessage(request: HttpServletRequest, recipientAccount: Account?, publicKey: ByteArray?): EncryptedData?

    @Throws(ParameterException::class)
    fun getEncryptToSelfMessage(request: HttpServletRequest): EncryptedData?

    @Throws(ParameterException::class)
    fun getSecretPhrase(request: HttpServletRequest): String

    @Throws(ParameterException::class)
    fun getNumberOfConfirmations(request: HttpServletRequest): Int

    @Throws(ParameterException::class)
    fun getHeight(request: HttpServletRequest): Int

    @Throws(ParameterException::class)
    fun parseTransaction(transactionBytes: String?, transactionJSON: String?): Transaction

    @Throws(ParameterException::class)
    fun getAT(request: HttpServletRequest): AT

    fun getIncludeIndirect(request: HttpServletRequest): Boolean
}
