package brs.http

import brs.Account
import brs.assetexchange.AssetExchange
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.*
import brs.http.common.ResultFields.ASSETS_RESPONSE

internal class GetAssetsByIssuer internal constructor(private val parameterService: ParameterService, private val assetExchange: AssetExchange) : AbstractAssetsRetrieval(arrayOf(APITag.AE, APITag.ACCOUNTS), assetExchange, ACCOUNT_PARAMETER, ACCOUNT_PARAMETER, ACCOUNT_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

    @Throws(ParameterException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val accounts = parameterService.getAccounts(req)
        val firstIndex = ParameterParser.getFirstIndex(req)
        val lastIndex = ParameterParser.getLastIndex(req)

        val response = JsonObject()
        val accountsJsonArray = JsonArray()
        response.add(ASSETS_RESPONSE, accountsJsonArray)
        for (account in accounts) {
            accountsJsonArray.add(assetsToJson(assetExchange.getAssetsIssuedBy(account.getId(), firstIndex, lastIndex).iterator()))
        }
        return response
    }

}
