package brs.http

import brs.BurstException
import brs.assetexchange.AssetExchange
import brs.http.JSONResponses.UNKNOWN_ORDER
import brs.http.common.Parameters.ORDER_PARAMETER
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class GetAskOrder internal constructor(private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ORDER_PARAMETER) {
    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        val orderId = ParameterParser.getOrderId(request)
        val askOrder = assetExchange.getAskOrder(orderId) ?: return UNKNOWN_ORDER
        return JSONData.askOrder(askOrder)
    }
}
