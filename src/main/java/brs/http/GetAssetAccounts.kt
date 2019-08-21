package brs.http

import brs.Account
import brs.Asset
import brs.BurstException
import brs.assetexchange.AssetExchange
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.*

internal class GetAssetAccounts(private val parameterService: ParameterService, private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ASSET_PARAMETER, HEIGHT_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val asset = parameterService.getAsset(req)
        val firstIndex = ParameterParser.getFirstIndex(req)
        val lastIndex = ParameterParser.getLastIndex(req)
        val height = parameterService.getHeight(req)

        val accountAssets = JsonArray()
        for (accountAsset in assetExchange.getAccountAssetsOverview(asset.id, height, firstIndex, lastIndex)) {
            accountAssets.add(JSONData.accountAsset(accountAsset))
        }

        val response = JsonObject()
        response.add("accountAssets", accountAssets)
        return response
    }
}
