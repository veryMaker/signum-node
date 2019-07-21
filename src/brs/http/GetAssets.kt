package brs.http

import brs.Asset
import brs.assetexchange.AssetExchange
import brs.util.Convert
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.INCORRECT_ASSET
import brs.http.JSONResponses.UNKNOWN_ASSET
import brs.http.common.Parameters.ASSETS_PARAMETER
import brs.http.common.ResultFields.ASSETS_RESPONSE

internal class GetAssets(private val assetExchange: AssetExchange) // limit to 3 for testing
    : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ASSETS_PARAMETER, ASSETS_PARAMETER, ASSETS_PARAMETER) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val assets = req.getParameterValues(ASSETS_PARAMETER)

        val response = JsonObject()
        val assetsJsonArray = JsonArray()
        response.add(ASSETS_RESPONSE, assetsJsonArray)
        for (assetIdString in assets) {
            if (assetIdString == null || assetIdString.isEmpty()) {
                continue
            }
            try {
                val asset = assetExchange.getAsset(Convert.parseUnsignedLong(assetIdString)) ?: return UNKNOWN_ASSET

                val tradeCount = assetExchange.getTradeCount(asset.id)
                val transferCount = assetExchange.getTransferCount(asset.id)
                val accountsCount = assetExchange.getAssetAccountsCount(asset.id)

                assetsJsonArray.add(JSONData.asset(asset, tradeCount, transferCount, accountsCount))
            } catch (e: RuntimeException) {
                return INCORRECT_ASSET
            }

        }
        return response
    }

}
