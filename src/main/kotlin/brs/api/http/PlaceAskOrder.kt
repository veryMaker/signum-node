package brs.api.http

import brs.transaction.appendix.Attachment
import brs.entity.DependencyProvider
import brs.api.http.JSONResponses.NOT_ENOUGH_ASSETS
import brs.api.http.common.Parameters.ASSET_PARAMETER
import brs.api.http.common.Parameters.PRICE_PLANCK_PARAMETER
import brs.api.http.common.Parameters.QUANTITY_QNT_PARAMETER
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class PlaceAskOrder(private val dp: DependencyProvider) : CreateTransaction(
    dp,
    arrayOf(APITag.AE, APITag.CREATE_TRANSACTION),
    ASSET_PARAMETER,
    QUANTITY_QNT_PARAMETER,
    PRICE_PLANCK_PARAMETER
) {

    override fun processRequest(request: HttpServletRequest): JsonElement {

        val asset = dp.parameterService.getAsset(request)
        val pricePlanck = ParameterParser.getPricePlanck(request)
        val quantity = ParameterParser.getQuantity(request)
        val account = dp.parameterService.getSenderAccount(request)

        val assetBalance = dp.accountService.getUnconfirmedAssetBalanceQuantity(account, asset.id)
        if (assetBalance < 0 || quantity > assetBalance) {
            return NOT_ENOUGH_ASSETS
        }

        val attachment =
            Attachment.ColoredCoinsAskOrderPlacement(dp, asset.id, quantity, pricePlanck, dp.blockchainService.height)
        return createTransaction(request, account, attachment)
    }
}
