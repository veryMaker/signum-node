package brs.http

import brs.*
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.NOT_ENOUGH_FUNDS
import brs.http.common.Parameters.*

internal class PlaceBidOrder(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.AE, APITag.CREATE_TRANSACTION), ASSET_PARAMETER, QUANTITY_QNT_PARAMETER, PRICE_NQT_PARAMETER) {
    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val asset = dp.parameterService.getAsset(req)
        val priceNQT = ParameterParser.getPriceNQT(req)
        val quantityQNT = ParameterParser.getQuantityQNT(req)
        val feeNQT = ParameterParser.getFeeNQT(req)
        val account = dp.parameterService.getSenderAccount(req)

        try {
            if (Convert.safeAdd(feeNQT, Convert.safeMultiply(priceNQT, quantityQNT)) > account.unconfirmedBalanceNQT) {
                return NOT_ENOUGH_FUNDS
            }
        } catch (e: ArithmeticException) {
            return NOT_ENOUGH_FUNDS
        }

        val attachment = Attachment.ColoredCoinsBidOrderPlacement(asset.id, quantityQNT, priceNQT, dp.blockchain.height)
        return createTransaction(req, account, attachment)
    }
}
