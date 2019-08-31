package brs.http

import brs.*
import brs.assetexchange.AssetExchange
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.UNKNOWN_ORDER
import brs.http.common.Parameters.ORDER_PARAMETER

internal class CancelBidOrder(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.AE, APITag.CREATE_TRANSACTION), ORDER_PARAMETER) {
    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val orderId = ParameterParser.getOrderId(req)
        val account = dp.parameterService.getSenderAccount(req)
        val orderData = dp.assetExchange.getBidOrder(orderId)
        if (orderData == null || orderData.accountId != account.id) {
            return UNKNOWN_ORDER
        }
        val attachment = Attachment.ColoredCoinsBidOrderCancellation(orderId, dp.blockchain.height)
        return createTransaction(req, account, attachment)
    }
}
