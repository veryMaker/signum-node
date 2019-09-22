package brs.http

import brs.Attachment
import brs.BurstException
import brs.DependencyProvider
import brs.http.JSONResponses.NOT_ENOUGH_ASSETS
import brs.http.common.Parameters.ASSET_PARAMETER
import brs.http.common.Parameters.QUANTITY_QNT_PARAMETER
import brs.http.common.Parameters.RECIPIENT_PARAMETER
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class TransferAsset(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.AE, APITag.CREATE_TRANSACTION), RECIPIENT_PARAMETER, ASSET_PARAMETER, QUANTITY_QNT_PARAMETER) {
    internal override fun processRequest(request: HttpServletRequest): JsonElement {
        val recipient = ParameterParser.getRecipientId(request)

        val asset = dp.parameterService.getAsset(request)
        val quantityQNT = ParameterParser.getQuantityQNT(request)
        val account = dp.parameterService.getSenderAccount(request)

        val assetBalance = dp.accountService.getUnconfirmedAssetBalanceQNT(account, asset.id)
        if (assetBalance < 0 || quantityQNT > assetBalance) {
            return NOT_ENOUGH_ASSETS
        }

        val attachment = Attachment.ColoredCoinsAssetTransfer(dp, asset.id, quantityQNT, dp.blockchain.height)
        return createTransaction(request, account, recipient, 0, attachment)
    }
}
