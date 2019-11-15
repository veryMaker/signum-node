package brs.api.http

import brs.api.http.JSONResponses.UNKNOWN_ORDER
import brs.api.http.common.Parameters.ORDER_PARAMETER
import brs.services.AssetExchangeService
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetBidOrder internal constructor(private val assetExchangeService: AssetExchangeService) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ORDER_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val orderId = ParameterParser.getOrderId(request)
        val bidOrder = assetExchangeService.getBidOrder(orderId) ?: return UNKNOWN_ORDER

        return JSONData.bidOrder(bidOrder)
    }
}
