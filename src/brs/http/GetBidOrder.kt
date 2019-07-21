package brs.http

import brs.BurstException
import brs.Order
import brs.assetexchange.AssetExchange
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.UNKNOWN_ORDER
import brs.http.common.Parameters.ORDER_PARAMETER

internal class GetBidOrder internal constructor(private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ORDER_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val orderId = ParameterParser.getOrderId(req)
        val bidOrder = assetExchange.getBidOrder(orderId) ?: return UNKNOWN_ORDER

        return JSONData.bidOrder(bidOrder)
    }

}
