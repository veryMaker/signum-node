package brs.services

import brs.at.AT
import brs.entity.*
import burst.kit.entity.BurstEncryptedMessage
import javax.servlet.http.HttpServletRequest

interface ParameterService {
    /**
     * TODO
     */
    fun getAccount(request: HttpServletRequest): Account

    /**
     * TODO
     */
    fun getAccounts(request: HttpServletRequest): List<Account>

    /**
     * TODO
     */
    fun getSenderAccount(request: HttpServletRequest): Account

    /**
     * TODO
     */
    fun getAlias(request: HttpServletRequest): Alias

    /**
     * TODO
     */
    fun getAsset(request: HttpServletRequest): Asset

    /**
     * TODO
     */
    fun getGoods(request: HttpServletRequest): Goods

    /**
     * TODO
     */
    fun getPurchase(request: HttpServletRequest): Purchase

    /**
     * TODO
     */
    fun getEncryptedMessage(
        request: HttpServletRequest,
        recipientAccount: Account?,
        publicKey: ByteArray?
    ): BurstEncryptedMessage?

    /**
     * TODO
     */
    fun getEncryptToSelfMessage(request: HttpServletRequest): BurstEncryptedMessage?

    /**
     * TODO
     */
    fun getSecretPhrase(request: HttpServletRequest): String

    /**
     * TODO
     */
    fun getNumberOfConfirmations(request: HttpServletRequest): Int

    /**
     * TODO
     */
    fun getHeight(request: HttpServletRequest): Int

    /**
     * TODO
     */
    fun parseTransaction(transactionBytes: String?, transactionJSON: String?): Transaction

    /**
     * TODO
     */
    fun getAT(request: HttpServletRequest): AT

    /**
     * TODO
     */
    fun getIncludeIndirect(request: HttpServletRequest): Boolean
}
