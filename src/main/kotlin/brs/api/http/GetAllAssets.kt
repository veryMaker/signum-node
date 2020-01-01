package brs.api.http

import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.ASSETS_RESPONSE
import brs.services.AssetExchangeService
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetAllAssets(private val assetExchangeService: AssetExchangeService) :
    AbstractAssetsRetrieval(arrayOf(APITag.AE), assetExchangeService, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        val response = JsonObject()

        response.add(ASSETS_RESPONSE, assetsToJson(assetExchangeService.getAllAssets(firstIndex, lastIndex)))

        return response
    }
}
