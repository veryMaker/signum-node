package brs.http

import brs.assetexchange.AssetExchange
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.ResultFields.ASSET_IDS_RESPONSE
import brs.util.toUnsignedString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAssetIds(private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {

        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        val assetIds = JsonArray()
        for (asset in assetExchange.getAllAssets(firstIndex, lastIndex)) {
            assetIds.add(asset.id.toUnsignedString())
        }
        val response = JsonObject()
        response.add(ASSET_IDS_RESPONSE, assetIds)
        return response
    }

}
