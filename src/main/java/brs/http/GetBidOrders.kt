package brs.http

import brs.BurstException
import brs.Order
import brs.assetexchange.AssetExchange
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.*
import brs.http.common.ResultFields.BID_ORDERS_RESPONSE

internal class GetBidOrders internal constructor(private val parameterService: ParameterService, private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ASSET_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val assetId = parameterService.getAsset(req).id
        val firstIndex = ParameterParser.getFirstIndex(req)
        val lastIndex = ParameterParser.getLastIndex(req)

        val orders = JsonArray()
        for (bidOrder in assetExchange.getSortedBidOrders(assetId, firstIndex, lastIndex)) {
            orders.add(JSONData.bidOrder(bidOrder))
        }

        val response = JsonObject()
        response.add(BID_ORDERS_RESPONSE, orders)
        return response
    }

}
