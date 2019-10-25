package brs.http


import brs.Attachment
import brs.Constants
import brs.DependencyProvider
import brs.http.JSONResponses.DUPLICATE_REFUND
import brs.http.JSONResponses.GOODS_NOT_DELIVERED
import brs.http.JSONResponses.INCORRECT_ACCOUNT
import brs.http.JSONResponses.INCORRECT_DGS_REFUND
import brs.http.JSONResponses.INCORRECT_PURCHASE
import brs.http.common.Parameters.PURCHASE_PARAMETER
import brs.http.common.Parameters.REFUND_PLANCK_PARAMETER
import brs.util.convert.emptyToNull
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class DGSRefund internal constructor(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), PURCHASE_PARAMETER, REFUND_PLANCK_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {
        val sellerAccount = dp.parameterService.getSenderAccount(request)
        val purchase = dp.parameterService.getPurchase(request)

        if (sellerAccount.id != purchase.sellerId) {
            return INCORRECT_PURCHASE
        }
        if (purchase.refundNote != null) {
            return DUPLICATE_REFUND
        }
        if (purchase.encryptedGoods == null) {
            return GOODS_NOT_DELIVERED
        }

        val refundValuePlanck = request.getParameter(REFUND_PLANCK_PARAMETER).emptyToNull()
        var refundPlanck: Long = 0
        try {
            if (refundValuePlanck != null) {
                refundPlanck = refundValuePlanck.toLong()
            }
        } catch (e: RuntimeException) {
            return INCORRECT_DGS_REFUND
        }

        if (refundPlanck < 0 || refundPlanck > Constants.MAX_BALANCE_PLANCK) {
            return INCORRECT_DGS_REFUND
        }

        val buyerAccount = dp.accountService.getAccount(purchase.buyerId) ?: return INCORRECT_ACCOUNT

        val attachment = Attachment.DigitalGoodsRefund(dp, purchase.id, refundPlanck, dp.blockchain.height)
        return createTransaction(request, sellerAccount, buyerAccount.id, 0, attachment)
    }
}
