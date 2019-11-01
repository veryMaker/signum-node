package brs.api.http

import brs.api.http.common.Parameters.ASSET_PARAMETER
import brs.services.AssetExchangeService
import brs.services.ParameterService
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetAsset internal constructor(
    private val parameterService: ParameterService,
    private val assetExchangeService: AssetExchangeService
) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ASSET_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val asset = parameterService.getAsset(request)

        val tradeCount = assetExchangeService.getTradeCount(asset.id)
        val transferCount = assetExchangeService.getTransferCount(asset.id)
        val accountsCount = assetExchangeService.getAssetAccountsCount(asset.id)

        return JSONData.asset(asset, tradeCount, transferCount, accountsCount)
    }
}
