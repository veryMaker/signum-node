package brs.http

import brs.BurstException
import brs.assetexchange.AssetExchange
import brs.http.common.Parameters.ASSET_PARAMETER
import brs.services.ParameterService
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class GetAsset internal constructor(private val parameterService: ParameterService, private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ASSET_PARAMETER) {
    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        val asset = parameterService.getAsset(request)

        val tradeCount = assetExchange.getTradeCount(asset.id)
        val transferCount = assetExchange.getTransferCount(asset.id)
        val accountsCount = assetExchange.getAssetAccountsCount(asset.id)

        return JSONData.asset(asset, tradeCount, transferCount, accountsCount)
    }
}
