package brs.http

import brs.*
import brs.services.AccountService
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.GOODS_NOT_DELIVERED
import brs.http.JSONResponses.INCORRECT_PURCHASE
import brs.http.common.Parameters.PURCHASE_PARAMETER

internal class DGSFeedback internal constructor(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), PURCHASE_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val purchase = dp.parameterService.getPurchase(req)
        val buyerAccount = dp.parameterService.getSenderAccount(req)

        if (buyerAccount.id != purchase.buyerId) {
            return INCORRECT_PURCHASE
        }
        if (purchase.encryptedGoods == null) {
            return GOODS_NOT_DELIVERED
        }

        val sellerAccount = dp.accountService.getAccount(purchase.sellerId)
        val attachment = Attachment.DigitalGoodsFeedback(purchase.id, dp.blockchain.height)

        return createTransaction(req, buyerAccount, sellerAccount.id, 0, attachment)
    }

}
