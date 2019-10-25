package brs.api.http

import brs.transaction.appendix.Attachment
import brs.entity.DependencyProvider
import brs.api.http.JSONResponses.INCORRECT_ACCOUNT
import brs.api.http.JSONResponses.INCORRECT_DELIVERY_DEADLINE_TIMESTAMP
import brs.api.http.JSONResponses.INCORRECT_PURCHASE_PRICE
import brs.api.http.JSONResponses.INCORRECT_PURCHASE_QUANTITY
import brs.api.http.JSONResponses.MISSING_DELIVERY_DEADLINE_TIMESTAMP
import brs.api.http.JSONResponses.UNKNOWN_GOODS
import brs.api.http.common.Parameters.DELIVERY_DEADLINE_TIMESTAMP_PARAMETER
import brs.api.http.common.Parameters.GOODS_PARAMETER
import brs.api.http.common.Parameters.PRICE_PLANCK_PARAMETER
import brs.api.http.common.Parameters.QUANTITY_PARAMETER
import brs.util.convert.emptyToNull
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class DGSPurchase internal constructor(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), GOODS_PARAMETER, PRICE_PLANCK_PARAMETER, QUANTITY_PARAMETER, DELIVERY_DEADLINE_TIMESTAMP_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {

        val goods = dp.parameterService.getGoods(request)
        if (goods.isDelisted) {
            return UNKNOWN_GOODS
        }

        val quantity = ParameterParser.getGoodsQuantity(request)
        if (quantity > goods.quantity) {
            return INCORRECT_PURCHASE_QUANTITY
        }

        val pricePlanck = ParameterParser.getPricePlanck(request)
        if (pricePlanck != goods.pricePlanck) {
            return INCORRECT_PURCHASE_PRICE
        }

        val deliveryDeadlineString = request.getParameter(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER).emptyToNull()
                ?: return MISSING_DELIVERY_DEADLINE_TIMESTAMP
        val deliveryDeadline: Int
        try {
            deliveryDeadline = Integer.parseInt(deliveryDeadlineString)
            if (deliveryDeadline <= dp.timeService.epochTime) {
                return INCORRECT_DELIVERY_DEADLINE_TIMESTAMP
            }
        } catch (e: NumberFormatException) {
            return INCORRECT_DELIVERY_DEADLINE_TIMESTAMP
        }

        val buyerAccount = dp.parameterService.getSenderAccount(request)
        val sellerAccount = dp.accountService.getAccount(goods.sellerId) ?: return INCORRECT_ACCOUNT

        val attachment = Attachment.DigitalGoodsPurchase(dp, goods.id, quantity, pricePlanck,
                deliveryDeadline, dp.blockchainService.height)
        return createTransaction(request, buyerAccount, sellerAccount.id, 0, attachment)
    }
}
