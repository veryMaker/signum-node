package brs.api.http

import brs.entity.Asset
import brs.services.AssetExchangeService
import com.google.gson.JsonArray

internal abstract class AbstractAssetsRetrieval(apiTags: Array<APITag>, private val assetExchangeService: AssetExchangeService, vararg parameters: String) : APIServlet.JsonRequestHandler(apiTags, *parameters) {
    fun assetsToJson(assets: Collection<Asset>): JsonArray {
        val assetsJsonArray = JsonArray()
        assets.forEach { asset ->
            val tradeCount = assetExchangeService.getTradeCount(asset.id)
            val transferCount = assetExchangeService.getTransferCount(asset.id)
            val accountsCount = assetExchangeService.getAssetAccountsCount(asset.id)
            assetsJsonArray.add(JSONData.asset(asset, tradeCount, transferCount, accountsCount))
        }
        return assetsJsonArray
    }
}
