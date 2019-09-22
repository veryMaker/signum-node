package brs.http

import brs.Attachment
import brs.BurstException
import brs.DependencyProvider
import brs.http.JSONResponses.UNKNOWN_ORDER
import brs.http.common.Parameters.ORDER_PARAMETER
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class CancelAskOrder(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.AE, APITag.CREATE_TRANSACTION), ORDER_PARAMETER) {

    internal override fun processRequest(request: HttpServletRequest): JsonElement {
        val orderId = ParameterParser.getOrderId(request)
        val account = dp.parameterService.getSenderAccount(request)
        val orderData = dp.assetExchange.getAskOrder(orderId)
        if (orderData == null || orderData.accountId != account.id) {
            return UNKNOWN_ORDER
        }
        val attachment = Attachment.ColoredCoinsAskOrderCancellation(dp, orderId, dp.blockchain.height)
        return createTransaction(request, account, attachment)
    }

}
