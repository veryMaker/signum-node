package brs.http

import brs.Asset
import brs.BurstException
import brs.Trade
import brs.assetexchange.AssetExchange
import brs.http.common.Parameters
import brs.util.FilteringIterator
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.*
import brs.http.common.ResultFields.TRADES_RESPONSE

internal class GetAllTrades internal constructor(private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), TIMESTAMP_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, INCLUDE_ASSET_INFO_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val timestamp = ParameterParser.getTimestamp(req)
        val firstIndex = ParameterParser.getFirstIndex(req)
        val lastIndex = ParameterParser.getLastIndex(req)
        val includeAssetInfo = !isFalse(req.getParameter(INCLUDE_ASSET_INFO_PARAMETER))

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
