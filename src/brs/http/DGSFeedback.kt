package brs.http

import brs.*
import brs.services.AccountService
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.GOODS_NOT_DELIVERED
import brs.http.JSONResponses.INCORRECT_PURCHASE
import brs.http.common.Parameters.PURCHASE_PARAMETER

internal class DGSFeedback internal constructor(private val parameterService: ParameterService, private val blockchain: Blockchain, private val accountService: AccountService, apiTransactionManager: APITransactionManager) : CreateTransaction(arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), apiTransactionManager, PURCHASE_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val purchase = parameterService.getPurchase(req)
        val buyerAccount = parameterService.getSenderAccount(req)

        if (buyerAccount.getId() != purchase.buyerId) {
            return INCORRECT_PURCHASE
        }
        if (purchase.encryptedGoods == null) {
            return GOODS_NOT_DELIVERED
        }

        val sellerAccount = accountService.getAccount(purchase.sellerId)
        val attachment = Attachment.DigitalGoodsFeedback(purchase.id, blockchain.height)

        return createTransaction(req, buyerAccount, sellerAccount.getId(), 0, attachment)
    }

}
