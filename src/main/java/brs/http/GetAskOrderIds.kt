package brs.http

import brs.BurstException
import brs.assetexchange.AssetExchange
import brs.http.common.Parameters.ASSET_PARAMETER
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.ResultFields.ASK_ORDER_IDS_RESPONSE
import brs.services.ParameterService
import brs.util.toUnsignedString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAskOrderIds internal constructor(private val parameterService: ParameterService, private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ASSET_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {

        val assetId = parameterService.getAsset(request).id
        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        val orderIds = JsonArray()
        for (askOrder in assetExchange.getSortedAskOrders(assetId, firstIndex, lastIndex)) {
            orderIds.add(askOrder.id.toUnsignedString())
        }

        val response = JsonObject()
        response.add(ASK_ORDER_IDS_RESPONSE, orderIds)
        return response
    }
}
