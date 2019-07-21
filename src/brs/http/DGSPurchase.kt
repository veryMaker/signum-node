package brs.http

import brs.*
import brs.http.JSONResponses.INCORRECT_DELIVERY_DEADLINE_TIMESTAMP
import brs.http.JSONResponses.INCORRECT_PURCHASE_PRICE
import brs.http.JSONResponses.INCORRECT_PURCHASE_QUANTITY
import brs.http.JSONResponses.MISSING_DELIVERY_DEADLINE_TIMESTAMP
import brs.http.JSONResponses.UNKNOWN_GOODS
import brs.services.AccountService
import brs.services.ParameterService
import brs.services.TimeService
import brs.util.Convert
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest


import brs.http.common.Parameters.*

internal class DGSPurchase internal constructor(private val parameterService: ParameterService, private val blockchain: Blockchain, private val accountService: AccountService, private val timeService: TimeService, apiTransactionManager: APITransactionManager) : CreateTransaction(arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), apiTransactionManager, GOODS_PARAMETER, PRICE_NQT_PARAMETER, QUANTITY_PARAMETER, DELIVERY_DEADLINE_TIMESTAMP_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val goods = parameterService.getGoods(req)
        if (goods.isDelisted) {
            return UNKNOWN_GOODS
        }

        val quantity = ParameterParser.getGoodsQuantity(req)
        if (quantity > goods.quantity) {
            return INCORRECT_PURCHASE_QUANTITY
        }

        val priceNQT = ParameterParser.getPriceNQT(req)
        if (priceNQT != goods.priceNQT) {
            return INCORRECT_PURCHASE_PRICE
        }

        val deliveryDeadlineString = Convert.emptyToNull(req.getParameter(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER))
                ?: return MISSING_DELIVERY_DEADLINE_TIMESTAMP
        val deliveryDeadline: Int
        try {
            deliveryDeadline = Integer.parseInt(deliveryDeadlineString)
            if (deliveryDeadline <= timeService.epochTime) {
                return INCORRECT_DELIVERY_DEADLINE_TIMESTAMP
            }
        } catch (e: NumberFormatException) {
            return INCORRECT_DELIVERY_DEADLINE_TIMESTAMP
        }

        val buyerAccount = parameterService.getSenderAccount(req)
        val sellerAccount = accountService.getAccount(goods.sellerId)

        val attachment = Attachment.DigitalGoodsPurchase(goods.id, quantity, priceNQT,
                deliveryDeadline, blockchain.height)
        return createTransaction(req, buyerAccount, sellerAccount.getId(), 0, attachment)

    }

}
