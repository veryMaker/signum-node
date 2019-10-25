package brs.http

import brs.assetexchange.AssetExchange
import brs.http.JSONResponses.UNKNOWN_ORDER
import brs.http.common.Parameters.ORDER_PARAMETER
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class GetBidOrder internal constructor(private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ORDER_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val orderId = ParameterParser.getOrderId(request)
        val bidOrder = assetExchange.getBidOrder(orderId) ?: return UNKNOWN_ORDER

        return JSONData.bidOrder(bidOrder)
    }
}
