package brs.http

import brs.*
import brs.services.AccountService
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.NOT_ENOUGH_ASSETS
import brs.http.common.Parameters.*

internal class PlaceAskOrder(private val parameterService: ParameterService, private val blockchain: Blockchain, apiTransactionManager: APITransactionManager, private val accountService: AccountService) : CreateTransaction(arrayOf(APITag.AE, APITag.CREATE_TRANSACTION), apiTransactionManager, ASSET_PARAMETER, QUANTITY_QNT_PARAMETER, PRICE_NQT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val asset = parameterService.getAsset(req)
        val priceNQT = ParameterParser.getPriceNQT(req)
        val quantityQNT = ParameterParser.getQuantityQNT(req)
        val account = parameterService.getSenderAccount(req)

        val assetBalance = accountService.getUnconfirmedAssetBalanceQNT(account, asset.id)
        if (assetBalance < 0 || quantityQNT > assetBalance) {
            return NOT_ENOUGH_ASSETS
        }

        val attachment = Attachment.ColoredCoinsAskOrderPlacement(asset.id, quantityQNT, priceNQT, blockchain.height)
        return createTransaction(req, account, attachment)

    }

}
