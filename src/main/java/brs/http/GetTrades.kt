package brs.http

import brs.Trade
import brs.assetexchange.AssetExchange
import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.ASSET_PARAMETER
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.INCLUDE_ASSET_INFO_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.Parameters.isFalse
import brs.http.common.ResultFields.TRADES_RESPONSE
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetTrades internal constructor(private val parameterService: ParameterService, private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ASSET_PARAMETER, ACCOUNT_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, INCLUDE_ASSET_INFO_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {

        val assetId = Convert.emptyToNull(request.getParameter(ASSET_PARAMETER))
        val accountId = Convert.emptyToNull(request.getParameter(ACCOUNT_PARAMETER))

        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)
        val includeAssetInfo = !isFalse(request.getParameter(INCLUDE_ASSET_INFO_PARAMETER))

        val response = JsonObject()
        val tradesData = JsonArray()
        val trades: Collection<Trade>
        trades = if (accountId == null) {
            val asset = parameterService.getAsset(request)
            assetExchange.getTrades(asset.id, firstIndex, lastIndex)
        } else if (assetId == null) {
            val account = parameterService.getAccount(request) ?: return JSONResponses.INCORRECT_ACCOUNT
            assetExchange.getAccountTrades(account.id, firstIndex, lastIndex)
        } else {
            val asset = parameterService.getAsset(request)
            val account = parameterService.getAccount(request) ?: return JSONResponses.INCORRECT_ACCOUNT
            assetExchange.getAccountAssetTrades(account.id, asset.id, firstIndex, lastIndex)
        }
        for (trade in trades) {
            val asset = if (includeAssetInfo) assetExchange.getAsset(trade.assetId) else null
            tradesData.add(JSONData.trade(trade, asset))
        }
        response.add(TRADES_RESPONSE, tradesData)

        return response
    }
}
