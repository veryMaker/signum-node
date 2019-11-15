package brs.api.http

import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.ASSET_IDS_RESPONSE
import brs.services.AssetExchangeService
import brs.util.convert.toUnsignedString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetAssetIds(private val assetExchangeService: AssetExchangeService) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.AE), FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {

        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        val assetIds = JsonArray()
        for (asset in assetExchangeService.getAllAssets(firstIndex, lastIndex)) {
            assetIds.add(asset.id.toUnsignedString())
        }
        val response = JsonObject()
        response.add(ASSET_IDS_RESPONSE, assetIds)
        return response
    }

}
