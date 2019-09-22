package brs.services

import brs.*
import brs.at.AT
import brs.crypto.EncryptedData
import brs.http.ParameterException

import javax.servlet.http.HttpServletRequest

interface ParameterService {
    fun getAccount(request: HttpServletRequest): Account

    fun getAccounts(request: HttpServletRequest): List<Account>

    fun getSenderAccount(request: HttpServletRequest): Account // TODO these should be nullable.

    fun getAlias(request: HttpServletRequest): Alias

    fun getAsset(request: HttpServletRequest): Asset

    fun getGoods(request: HttpServletRequest): DigitalGoodsStore.Goods

    fun getPurchase(request: HttpServletRequest): DigitalGoodsStore.Purchase

    fun getEncryptedMessage(request: HttpServletRequest, recipientAccount: Account?, publicKey: ByteArray?): EncryptedData?

    fun getEncryptToSelfMessage(request: HttpServletRequest): EncryptedData?

    fun getSecretPhrase(request: HttpServletRequest): String

    fun getNumberOfConfirmations(request: HttpServletRequest): Int

    fun getHeight(request: HttpServletRequest): Int

    fun parseTransaction(transactionBytes: String?, transactionJSON: String?): Transaction

    fun getAT(request: HttpServletRequest): AT

    fun getIncludeIndirect(request: HttpServletRequest): Boolean
}
