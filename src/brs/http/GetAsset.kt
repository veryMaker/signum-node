package brs.http

import brs.Asset
import brs.BurstException
import brs.assetexchange.AssetExchange
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.ASSET_PARAMETER

internal class GetAsset internal constructor(private val parameterService: ParameterService, private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.AE), ASSET_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val asset = parameterService.getAsset(req)

        val tradeCount = assetExchange.getTradeCount(asset.id)
        val transferCount = assetExchange.getTransferCount(asset.id)
        val accountsCount = assetExchange.getAssetAccountsCount(asset.id)

        return JSONData.asset(asset, tradeCount, transferCount, accountsCount)
    }

}
