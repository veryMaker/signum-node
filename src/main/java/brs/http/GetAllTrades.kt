package brs.http

import brs.assetexchange.AssetExchange
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.INCLUDE_ASSET_INFO_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.http.common.Parameters.isFalse
import brs.http.common.ResultFields.TRADES_RESPONSE
import brs.util.FilteringIterator
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAllTrades internal constructor(private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), TIMESTAMP_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, INCLUDE_ASSET_INFO_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        val timestamp = ParameterParser.getTimestamp(request)
        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)
        val includeAssetInfo = !isFalse(request.getParameter(INCLUDE_ASSET_INFO_PARAMETER))

        val response = JsonObject()
        val trades = JsonArray()

        val tradeIterator = FilteringIterator(
                assetExchange.getAllTrades(0, -1),
                { trade -> trade != null && trade.timestamp >= timestamp }, firstIndex, lastIndex)
        while (tradeIterator.hasNext()) {
            val trade = tradeIterator.next()
            val asset = if (includeAssetInfo) assetExchange.getAsset(trade.assetId) else null

            trades.add(JSONData.trade(trade, asset))
        }

        response.add(TRADES_RESPONSE, trades)
        return response
    }
}
