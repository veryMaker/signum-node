package brs.api.http

import brs.services.AssetExchangeService
import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.ASSETS_RESPONSE
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAssetsByIssuer internal constructor(private val parameterService: ParameterService, private val assetExchangeService: AssetExchangeService) : AbstractAssetsRetrieval(arrayOf(APITag.AE, APITag.ACCOUNTS), assetExchangeService, ACCOUNT_PARAMETER, ACCOUNT_PARAMETER, ACCOUNT_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val accounts = parameterService.getAccounts(request)
        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        val response = JsonObject()
        val accountsJsonArray = JsonArray()
        response.add(ASSETS_RESPONSE, accountsJsonArray)
        for (account in accounts) {
            accountsJsonArray.add(assetsToJson(assetExchangeService.getAssetsIssuedBy(account.id, firstIndex, lastIndex)))
        }
        return response
    }
}
