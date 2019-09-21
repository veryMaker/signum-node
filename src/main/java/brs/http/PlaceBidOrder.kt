package brs.http

import brs.Attachment
import brs.BurstException
import brs.DependencyProvider
import brs.http.JSONResponses.NOT_ENOUGH_FUNDS
import brs.http.common.Parameters.ASSET_PARAMETER
import brs.http.common.Parameters.PRICE_NQT_PARAMETER
import brs.http.common.Parameters.QUANTITY_QNT_PARAMETER
import brs.util.Convert
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class PlaceBidOrder(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.AE, APITag.CREATE_TRANSACTION), ASSET_PARAMETER, QUANTITY_QNT_PARAMETER, PRICE_NQT_PARAMETER) {
    @Throws(BurstException::class)
    internal override fun processRequest(request: HttpServletRequest): JsonElement {

        val asset = dp.parameterService.getAsset(request)
        val priceNQT = ParameterParser.getPriceNQT(request)
        val quantityQNT = ParameterParser.getQuantityQNT(request)
        val feeNQT = ParameterParser.getFeeNQT(request)
        val account = dp.parameterService.getSenderAccount(request)

        try {
            if (Convert.safeAdd(feeNQT, Convert.safeMultiply(priceNQT, quantityQNT)) > account.unconfirmedBalanceNQT) {
                return NOT_ENOUGH_FUNDS
            }
        } catch (e: ArithmeticException) {
            return NOT_ENOUGH_FUNDS
        }

        val attachment = Attachment.ColoredCoinsBidOrderPlacement(asset.id, quantityQNT, priceNQT, dp.blockchain.height)
        return createTransaction(request, account, attachment)
    }
}
