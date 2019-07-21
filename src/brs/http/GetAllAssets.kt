package brs.http

import brs.assetexchange.AssetExchange
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.ResultFields.ASSETS_RESPONSE

internal class GetAllAssets(private val assetExchange: AssetExchange) : AbstractAssetsRetrieval(arrayOf(APITag.AE), assetExchange, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val firstIndex = ParameterParser.getFirstIndex(req)
        val lastIndex = ParameterParser.getLastIndex(req)

        val response = JsonObject()

        response.add(ASSETS_RESPONSE, assetsToJson(assetExchange.getAllAssets(firstIndex, lastIndex).iterator()))

        return response
    }
}
