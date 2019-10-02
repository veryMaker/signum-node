package brs.http

import brs.Attachment
import brs.BurstException
import brs.DependencyProvider
import brs.http.JSONResponses.INCORRECT_ACCOUNT
import brs.http.JSONResponses.INCORRECT_DELIVERY_DEADLINE_TIMESTAMP
import brs.http.JSONResponses.INCORRECT_PURCHASE_PRICE
import brs.http.JSONResponses.INCORRECT_PURCHASE_QUANTITY
import brs.http.JSONResponses.MISSING_DELIVERY_DEADLINE_TIMESTAMP
import brs.http.JSONResponses.UNKNOWN_GOODS
import brs.http.common.Parameters.DELIVERY_DEADLINE_TIMESTAMP_PARAMETER
import brs.http.common.Parameters.GOODS_PARAMETER
import brs.http.common.Parameters.PRICE_NQT_PARAMETER
import brs.http.common.Parameters.QUANTITY_PARAMETER
import brs.util.Convert
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class DGSPurchase internal constructor(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), GOODS_PARAMETER, PRICE_NQT_PARAMETER, QUANTITY_PARAMETER, DELIVERY_DEADLINE_TIMESTAMP_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {

        val goods = dp.parameterService.getGoods(request)
        if (goods.isDelisted) {
            return UNKNOWN_GOODS
        }

        val quantity = ParameterParser.getGoodsQuantity(request)
        if (quantity > goods.quantity) {
            return INCORRECT_PURCHASE_QUANTITY
        }

        val priceNQT = ParameterParser.getPriceNQT(request)
        if (priceNQT != goods.priceNQT) {
            return INCORRECT_PURCHASE_PRICE
        }

        val deliveryDeadlineString = Convert.emptyToNull(request.getParameter(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER))
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

        val attachment = Attachment.DigitalGoodsPurchase(dp, goods.id, quantity, priceNQT,
                deliveryDeadline, dp.blockchain.height)
        return createTransaction(request, buyerAccount, sellerAccount.id, 0, attachment)

    }

}
