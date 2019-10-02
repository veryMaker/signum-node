package brs.http

import brs.BurstException
import brs.assetexchange.AssetExchange
import brs.http.common.Parameters
import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.ASSET_PARAMETER
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.INCLUDE_ASSET_INFO_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.ResultFields.TRANSFERS_RESPONSE
import brs.services.AccountService
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAssetTransfers internal constructor(private val parameterService: ParameterService, private val accountService: AccountService, private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ASSET_PARAMETER, ACCOUNT_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, INCLUDE_ASSET_INFO_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        val assetId = Convert.emptyToNull(request.getParameter(ASSET_PARAMETER))
        val accountId = Convert.emptyToNull(request.getParameter(ACCOUNT_PARAMETER))

        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)
        val includeAssetInfo = !Parameters.isFalse(request.getParameter(INCLUDE_ASSET_INFO_PARAMETER))

        val response = JsonObject()
        val transfersData = JsonArray()
        val transfers = when {
            accountId == null -> {
                val asset = parameterService.getAsset(request)
                assetExchange.getAssetTransfers(asset.id, firstIndex, lastIndex)
            }
            assetId == null -> {
                val account = parameterService.getAccount(request) ?: return JSONResponses.INCORRECT_ACCOUNT
                accountService.getAssetTransfers(account.id, firstIndex, lastIndex)
            }
            else -> {
                val asset = parameterService.getAsset(request)
                val account = parameterService.getAccount(request) ?: return JSONResponses.INCORRECT_ACCOUNT
                assetExchange.getAccountAssetTransfers(account.id, asset.id, firstIndex, lastIndex)
            }
        }
        for (transfer in transfers) {
            val asset = if (includeAssetInfo) assetExchange.getAsset(transfer.assetId) else null
            transfersData.add(JSONData.assetTransfer(transfer, asset))
        }

        response.add(TRANSFERS_RESPONSE, transfersData)

        return response
    }
}
