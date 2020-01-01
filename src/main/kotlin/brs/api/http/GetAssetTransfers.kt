package brs.api.http

import brs.api.http.common.JSONData
import brs.api.http.common.Parameters
import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.ASSET_PARAMETER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.INCLUDE_ASSET_INFO_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.TRANSFERS_RESPONSE
import brs.services.AccountService
import brs.services.AssetExchangeService
import brs.services.ParameterService
import brs.util.convert.emptyToNull
import brs.util.jetty.get
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetAssetTransfers internal constructor(
    private val parameterService: ParameterService,
    private val accountService: AccountService,
    private val assetExchangeService: AssetExchangeService
) : APIServlet.JsonRequestHandler(
    arrayOf(APITag.AE),
    ASSET_PARAMETER,
    ACCOUNT_PARAMETER,
    FIRST_INDEX_PARAMETER,
    LAST_INDEX_PARAMETER,
    INCLUDE_ASSET_INFO_PARAMETER
) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val assetId = request[ASSET_PARAMETER].emptyToNull()
        val accountId = request[ACCOUNT_PARAMETER].emptyToNull()

        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)
        val includeAssetInfo = !Parameters.isFalse(request[INCLUDE_ASSET_INFO_PARAMETER])

        val response = JsonObject()
        val transfersData = JsonArray()
        val transfers = when {
            accountId == null -> {
                val asset = parameterService.getAsset(request)
                assetExchangeService.getAssetTransfers(asset.id, firstIndex, lastIndex)
            }
            assetId == null -> {
                val account = parameterService.getAccount(request)
                accountService.getAssetTransfers(account.id, firstIndex, lastIndex)
            }
            else -> {
                val asset = parameterService.getAsset(request)
                val account = parameterService.getAccount(request)
                assetExchangeService.getAccountAssetTransfers(account.id, asset.id, firstIndex, lastIndex)
            }
        }
        for (transfer in transfers) {
            val asset = if (includeAssetInfo) assetExchangeService.getAsset(transfer.assetId) else null
            transfersData.add(JSONData.assetTransfer(transfer, asset))
        }

        response.add(TRANSFERS_RESPONSE, transfersData)

        return response
    }
}
