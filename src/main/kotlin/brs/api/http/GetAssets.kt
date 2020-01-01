package brs.api.http

import brs.api.http.common.JSONData
import brs.api.http.common.JSONResponses.INCORRECT_ASSET
import brs.api.http.common.JSONResponses.MISSING_ASSETS
import brs.api.http.common.JSONResponses.UNKNOWN_ASSET
import brs.api.http.common.Parameters.ASSETS_PARAMETER
import brs.api.http.common.ResultFields.ASSETS_RESPONSE
import brs.services.AssetExchangeService
import brs.util.convert.parseUnsignedLong
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetAssets(private val assetExchangeService: AssetExchangeService) // limit to 3 for testing
    : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ASSETS_PARAMETER, ASSETS_PARAMETER, ASSETS_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val assets = request.getParameterValues(ASSETS_PARAMETER) ?: return MISSING_ASSETS

        val response = JsonObject()
        val assetsJsonArray = JsonArray()
        response.add(ASSETS_RESPONSE, assetsJsonArray)
        for (assetIdString in assets) {
            if (assetIdString == null || assetIdString.isEmpty()) {
                continue
            }
            try {
                val asset = assetExchangeService.getAsset(assetIdString.parseUnsignedLong()) ?: return UNKNOWN_ASSET

                val tradeCount = assetExchangeService.getTradeCount(asset.id)
                val transferCount = assetExchangeService.getTransferCount(asset.id)
                val accountsCount = assetExchangeService.getAssetAccountsCount(asset.id)

                assetsJsonArray.add(JSONData.asset(asset, tradeCount, transferCount, accountsCount))
            } catch (e: Exception) {
                return INCORRECT_ASSET
            }
        }
        return response
    }
}
