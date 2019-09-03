package brs.http

import brs.Account
import brs.Asset
import brs.AssetTransfer
import brs.BurstException
import brs.assetexchange.AssetExchange
import brs.http.common.Parameters
import brs.services.AccountService
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest
import brs.http.common.ResultFields.TRANSFERS_RESPONSE

internal class GetAssetTransfers internal constructor(private val parameterService: ParameterService, private val accountService: AccountService, private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ASSET_PARAMETER, ACCOUNT_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, INCLUDE_ASSET_INFO_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val assetId = Convert.emptyToNull(req.getParameter(ASSET_PARAMETER))
        val accountId = Convert.emptyToNull(req.getParameter(ACCOUNT_PARAMETER))

        val firstIndex = ParameterParser.getFirstIndex(req)
        val lastIndex = ParameterParser.getLastIndex(req)
        val includeAssetInfo = !Parameters.isFalse(req.getParameter(INCLUDE_ASSET_INFO_PARAMETER))

        val response = JsonObject()
        val transfersData = JsonArray()
        val transfers: Collection<AssetTransfer>
        if (accountId == null) {
            val asset = parameterService.getAsset(req)
            transfers = assetExchange.getAssetTransfers(asset.id, firstIndex, lastIndex)
        } else if (assetId == null) {
            val account = parameterService.getAccount(req)
            transfers = accountService.getAssetTransfers(account.id, firstIndex, lastIndex)
        } else {
            val asset = parameterService.getAsset(req)
            val account = parameterService.getAccount(req)
            transfers = assetExchange.getAccountAssetTransfers(account.id, asset.id, firstIndex, lastIndex)
        }
        for (transfer in transfers) {
            val asset = if (includeAssetInfo) assetExchange.getAsset(transfer.assetId) else null
            transfersData.add(JSONData.assetTransfer(transfer, asset))
        }

        response.add(TRANSFERS_RESPONSE, transfersData)

        return response
    }
}
