package brs.http

import brs.BurstException
import brs.Trade
import brs.assetexchange.AssetExchange
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest
import brs.http.common.ResultFields.TRADES_RESPONSE

internal class GetTrades internal constructor(private val parameterService: ParameterService, private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ASSET_PARAMETER, ACCOUNT_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, INCLUDE_ASSET_INFO_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val assetId = Convert.emptyToNull(req.getParameter(ASSET_PARAMETER))
        val accountId = Convert.emptyToNull(req.getParameter(ACCOUNT_PARAMETER))

        val firstIndex = ParameterParser.getFirstIndex(req)
        val lastIndex = ParameterParser.getLastIndex(req)
        val includeAssetInfo = !isFalse(req.getParameter(INCLUDE_ASSET_INFO_PARAMETER))

        val response = JsonObject()
        val tradesData = JsonArray()
        val trades: Collection<Trade>
        if (accountId == null) {
            val asset = parameterService.getAsset(req)
            trades = assetExchange.getTrades(asset.id, firstIndex, lastIndex)
        } else if (assetId == null) {
            val account = parameterService.getAccount(req)
            trades = assetExchange.getAccountTrades(account.id, firstIndex, lastIndex)
        } else {
            val asset = parameterService.getAsset(req)
            val account = parameterService.getAccount(req)
            trades = assetExchange.getAccountAssetTrades(account.id, asset.id, firstIndex, lastIndex)
        }
        for (trade in trades) {
            val asset = if (includeAssetInfo) assetExchange.getAsset(trade.assetId) else null
            tradesData.add(JSONData.trade(trade, asset))
        }
        response.add(TRADES_RESPONSE, tradesData)

        return response
    }
}
