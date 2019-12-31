package brs.api.http

import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.OPEN_ORDERS_RESPONSE
import brs.api.http.common.JSONData
import brs.services.AssetExchangeService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetAllOpenAskOrders internal constructor(private val assetExchangeService: AssetExchangeService) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.AE), FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {

        val response = JsonObject()
        val ordersData = JsonArray()

        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        for (askOrder in assetExchangeService.getAllAskOrders(firstIndex, lastIndex)) {
            ordersData.add(JSONData.askOrder(askOrder))
        }

        response.add(OPEN_ORDERS_RESPONSE, ordersData)
        return response
    }
}
