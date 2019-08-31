package brs.services

import brs.*
import brs.at.AT
import brs.crypto.EncryptedData
import brs.http.ParameterException

import javax.servlet.http.HttpServletRequest

interface ParameterService {

    @Throws(BurstException::class)
    fun getAccount(req: HttpServletRequest): Account

    @Throws(ParameterException::class)
    fun getAccounts(req: HttpServletRequest): List<Account>

    @Throws(ParameterException::class)
    fun getSenderAccount(req: HttpServletRequest): Account

    @Throws(ParameterException::class)
    fun getAlias(req: HttpServletRequest): Alias

    @Throws(ParameterException::class)
    fun getAsset(req: HttpServletRequest): Asset

    @Throws(ParameterException::class)
    fun getGoods(req: HttpServletRequest): DigitalGoodsStore.Goods

    @Throws(ParameterException::class)
    fun getPurchase(req: HttpServletRequest): DigitalGoodsStore.Purchase

    @Throws(ParameterException::class)
    fun getEncryptedMessage(req: HttpServletRequest, recipientAccount: Account, publicKey: ByteArray): EncryptedData

    @Throws(ParameterException::class)
    fun getEncryptToSelfMessage(req: HttpServletRequest): EncryptedData

    @Throws(ParameterException::class)
    fun getSecretPhrase(req: HttpServletRequest): String

    @Throws(ParameterException::class)
    fun getNumberOfConfirmations(req: HttpServletRequest): Int

    @Throws(ParameterException::class)
    fun getHeight(req: HttpServletRequest): Int

    @Throws(ParameterException::class)
    fun parseTransaction(transactionBytes: String, transactionJSON: String): Transaction

    @Throws(ParameterException::class)
    fun getAT(req: HttpServletRequest): AT

    fun getIncludeIndirect(req: HttpServletRequest): Boolean
}
