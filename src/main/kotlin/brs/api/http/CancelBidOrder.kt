package brs.api.http

import brs.entity.DependencyProvider
import brs.api.http.JSONResponses.UNKNOWN_ORDER
import brs.api.http.common.Parameters.ORDER_PARAMETER
import brs.transaction.appendix.Attachment
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class CancelBidOrder(private val dp: DependencyProvider) :
    CreateTransaction(dp, arrayOf(APITag.AE, APITag.CREATE_TRANSACTION), ORDER_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val orderId = ParameterParser.getOrderId(request)
        val account = dp.parameterService.getSenderAccount(request)
        val orderData = dp.assetExchangeService.getBidOrder(orderId)
        if (orderData == null || orderData.accountId != account.id) {
            return UNKNOWN_ORDER
        }
        val attachment = Attachment.ColoredCoinsBidOrderCancellation(dp, orderId, dp.blockchainService.height)
        return createTransaction(request, account, attachment)
    }
}
