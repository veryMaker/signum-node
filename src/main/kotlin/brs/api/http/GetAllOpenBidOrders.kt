package brs.api.http

import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.services.AssetExchangeService
import com.google.gson.JsonArray
import brs.util.jetty.get
import com.google.gson.JsonElement
import brs.util.jetty.get
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetAllOpenBidOrders internal constructor(private val assetExchangeService: AssetExchangeService) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.AE), FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {

        val response = JsonObject()
        val ordersData = JsonArray()

        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        for (bidOrder in assetExchangeService.getAllBidOrders(firstIndex, lastIndex)) {
            ordersData.add(JSONData.bidOrder(bidOrder))
        }

        response.add("openOrders", ordersData)
        return response
    }

}
