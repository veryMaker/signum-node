package brs.http

import brs.Attachment
import brs.DependencyProvider
import brs.http.JSONResponses.NOT_ENOUGH_ASSETS
import brs.http.common.Parameters.ASSET_PARAMETER
import brs.http.common.Parameters.PRICE_NQT_PARAMETER
import brs.http.common.Parameters.QUANTITY_QNT_PARAMETER
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class PlaceAskOrder(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.AE, APITag.CREATE_TRANSACTION), ASSET_PARAMETER, QUANTITY_QNT_PARAMETER, PRICE_NQT_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {

        val asset = dp.parameterService.getAsset(request)
        val priceNQT = ParameterParser.getPriceNQT(request)
        val quantityQNT = ParameterParser.getQuantityQNT(request)
        val account = dp.parameterService.getSenderAccount(request)

        val assetBalance = dp.accountService.getUnconfirmedAssetBalanceQNT(account, asset.id)
        if (assetBalance < 0 || quantityQNT > assetBalance) {
            return NOT_ENOUGH_ASSETS
        }

        val attachment = Attachment.ColoredCoinsAskOrderPlacement(dp, asset.id, quantityQNT, priceNQT, dp.blockchain.height)
        return createTransaction(request, account, attachment)
    }
}
