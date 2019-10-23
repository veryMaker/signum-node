package brs.services

import brs.*
import brs.at.AT
import brs.crypto.EncryptedData
import javax.servlet.http.HttpServletRequest

interface ParameterService {
    suspend fun getAccount(request: HttpServletRequest): Account?

    suspend fun getAccounts(request: HttpServletRequest): List<Account>

    suspend fun getSenderAccount(request: HttpServletRequest): Account // TODO these should be nullable.

    suspend fun getAlias(request: HttpServletRequest): Alias

    suspend fun getAsset(request: HttpServletRequest): Asset

    suspend fun getGoods(request: HttpServletRequest): DigitalGoodsStore.Goods

    suspend fun getPurchase(request: HttpServletRequest): DigitalGoodsStore.Purchase

    fun getEncryptedMessage(request: HttpServletRequest, recipientAccount: Account?, publicKey: ByteArray?): EncryptedData?

    suspend fun getEncryptToSelfMessage(request: HttpServletRequest): EncryptedData?

    fun getSecretPhrase(request: HttpServletRequest): String

    fun getNumberOfConfirmations(request: HttpServletRequest): Int

    fun getHeight(request: HttpServletRequest): Int

    fun parseTransaction(transactionBytes: String?, transactionJSON: String?): Transaction

    suspend fun getAT(request: HttpServletRequest): AT

    fun getIncludeIndirect(request: HttpServletRequest): Boolean
}
