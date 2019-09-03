package brs.http

import brs.*
import brs.services.AccountService
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.NOT_ENOUGH_ASSETS

internal class TransferAsset(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.AE, APITag.CREATE_TRANSACTION), RECIPIENT_PARAMETER, ASSET_PARAMETER, QUANTITY_QNT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val recipient = ParameterParser.getRecipientId(req)

        val asset = dp.parameterService.getAsset(req)
        val quantityQNT = ParameterParser.getQuantityQNT(req)
        val account = dp.parameterService.getSenderAccount(req)

        val assetBalance = dp.accountService.getUnconfirmedAssetBalanceQNT(account, asset.id)
        if (assetBalance < 0 || quantityQNT > assetBalance) {
            return NOT_ENOUGH_ASSETS
        }

        val attachment = Attachment.ColoredCoinsAssetTransfer(asset.id, quantityQNT, dp.blockchain.height)
        return createTransaction(req, account, recipient, 0, attachment)

    }

}
