package brs.http

import brs.assetexchange.AssetExchange
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAllOpenBidOrders internal constructor(private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

    internal override fun processRequest(request: HttpServletRequest): JsonElement {

        val response = JsonObject()
        val ordersData = JsonArray()

        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        for (bidOrder in assetExchange.getAllBidOrders(firstIndex, lastIndex)) {
            ordersData.add(JSONData.bidOrder(bidOrder))
        }

        response.add("openOrders", ordersData)
        return response
    }

}
