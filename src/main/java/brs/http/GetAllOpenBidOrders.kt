package brs.http

import brs.Order
import brs.assetexchange.AssetExchange
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER

internal class GetAllOpenBidOrders internal constructor(private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val response = JsonObject()
        val ordersData = JsonArray()

        val firstIndex = ParameterParser.getFirstIndex(req)
        val lastIndex = ParameterParser.getLastIndex(req)

        for (bidOrder in assetExchange.getAllBidOrders(firstIndex, lastIndex)) {
            ordersData.add(JSONData.bidOrder(bidOrder))
        }

        response.add("openOrders", ordersData)
        return response
    }

}
