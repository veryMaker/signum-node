package brs.api.http

import brs.api.http.common.Parameters.ASSET_PARAMETER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.ASK_ORDERS_RESPONSE
import brs.services.AssetExchangeService
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetAskOrders internal constructor(
    private val parameterService: ParameterService,
    private val assetExchangeService: AssetExchangeService
) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ASSET_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {

        val assetId = parameterService.getAsset(request).id
        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        val orders = JsonArray()
        for (askOrder in assetExchangeService.getSortedAskOrders(assetId, firstIndex, lastIndex)) {
            orders.add(JSONData.askOrder(askOrder))
        }

        val response = JsonObject()
        response.add(ASK_ORDERS_RESPONSE, orders)
        return response
    }
}
