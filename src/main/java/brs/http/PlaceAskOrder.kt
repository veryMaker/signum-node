package brs.http

import brs.*
import brs.services.AccountService
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.NOT_ENOUGH_ASSETS

internal class PlaceAskOrder(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.AE, APITag.CREATE_TRANSACTION), ASSET_PARAMETER, QUANTITY_QNT_PARAMETER, PRICE_NQT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val asset = dp.parameterService.getAsset(req)
        val priceNQT = ParameterParser.getPriceNQT(req)
        val quantityQNT = ParameterParser.getQuantityQNT(req)
        val account = dp.parameterService.getSenderAccount(req)

        val assetBalance = dp.accountService.getUnconfirmedAssetBalanceQNT(account, asset.id)
        if (assetBalance < 0 || quantityQNT > assetBalance) {
            return NOT_ENOUGH_ASSETS
        }

        val attachment = Attachment.ColoredCoinsAskOrderPlacement(asset.id, quantityQNT, priceNQT, dp.blockchain.height)
        return createTransaction(req, account, attachment)
    }
}
