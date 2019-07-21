package brs.http

import brs.Asset
import brs.assetexchange.AssetExchange
import com.google.gson.JsonArray

internal abstract class AbstractAssetsRetrieval(apiTags: Array<APITag>, private val assetExchange: AssetExchange, vararg parameters: String) : APIServlet.JsonRequestHandler(apiTags, *parameters) {
    fun assetsToJson(assets: Iterator<Asset>): JsonArray {
        val assetsJsonArray = JsonArray()

        while (assets.hasNext()) {
            val asset = assets.next()

            val tradeCount = assetExchange.getTradeCount(asset.id)
            val transferCount = assetExchange.getTransferCount(asset.id)
            val accountsCount = assetExchange.getAssetAccountsCount(asset.id)

            assetsJsonArray.add(JSONData.asset(asset, tradeCount, transferCount, accountsCount))
        }

        return assetsJsonArray
    }
}
