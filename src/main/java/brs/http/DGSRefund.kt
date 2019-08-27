package brs.http

import brs.*
import brs.http.JSONResponses.DUPLICATE_REFUND
import brs.http.JSONResponses.GOODS_NOT_DELIVERED
import brs.http.JSONResponses.INCORRECT_DGS_REFUND
import brs.http.JSONResponses.INCORRECT_PURCHASE
import brs.services.AccountService
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest


import brs.http.common.Parameters.PURCHASE_PARAMETER
import brs.http.common.Parameters.REFUND_NQT_PARAMETER

internal class DGSRefund internal constructor(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), PURCHASE_PARAMETER, REFUND_NQT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val sellerAccount = dp.parameterService.getSenderAccount(req)
        val purchase = dp.parameterService.getPurchase(req)

        if (sellerAccount.getId() != purchase.sellerId) {
            return INCORRECT_PURCHASE
        }
        if (purchase.refundNote != null) {
            return DUPLICATE_REFUND
        }
        if (purchase.encryptedGoods == null) {
            return GOODS_NOT_DELIVERED
        }

        val refundValueNQT = Convert.emptyToNull(req.getParameter(REFUND_NQT_PARAMETER))
        var refundNQT: Long = 0
        try {
            if (refundValueNQT != null) {
                refundNQT = java.lang.Long.parseLong(refundValueNQT)
            }
        } catch (e: RuntimeException) {
            return INCORRECT_DGS_REFUND
        }

        if (refundNQT < 0 || refundNQT > Constants.MAX_BALANCE_NQT) {
            return INCORRECT_DGS_REFUND
        }

        val buyerAccount = dp.accountService.getAccount(purchase.buyerId)

        val attachment = Attachment.DigitalGoodsRefund(purchase.id, refundNQT, dp.blockchain.height)
        return createTransaction(req, sellerAccount, buyerAccount.getId(), 0, attachment)
    }
}
