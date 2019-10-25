package brs.http

import brs.assetexchange.AssetExchange
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.ResultFields.ASSETS_RESPONSE
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAllAssets(private val assetExchange: AssetExchange) : AbstractAssetsRetrieval(arrayOf(APITag.AE), assetExchange, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        val response = JsonObject()

        response.add(ASSETS_RESPONSE, assetsToJson(assetExchange.getAllAssets(firstIndex, lastIndex)))

        return response
    }
}
