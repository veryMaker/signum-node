package brs.http

import brs.BurstException
import brs.assetexchange.AssetExchange
import brs.http.common.Parameters.ASSET_PARAMETER
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.HEIGHT_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAssetAccounts(private val parameterService: ParameterService, private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ASSET_PARAMETER, HEIGHT_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {

        val asset = parameterService.getAsset(request)
        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)
        val height = parameterService.getHeight(request)

        val accountAssets = JsonArray()
        for (accountAsset in assetExchange.getAccountAssetsOverview(asset.id, height, firstIndex, lastIndex)) {
            accountAssets.add(JSONData.accountAsset(accountAsset))
        }

        val response = JsonObject()
        response.add("accountAssets", accountAssets)
        return response
    }
}
