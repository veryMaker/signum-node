package brs.api.http

import brs.transaction.appendix.Attachment
import brs.DependencyProvider
import brs.api.http.JSONResponses.NOT_ENOUGH_FUNDS
import brs.api.http.common.Parameters.ASSET_PARAMETER
import brs.api.http.common.Parameters.PRICE_PLANCK_PARAMETER
import brs.api.http.common.Parameters.QUANTITY_QNT_PARAMETER
import brs.util.convert.safeAdd
import brs.util.convert.safeMultiply
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class PlaceBidOrder(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.AE, APITag.CREATE_TRANSACTION), ASSET_PARAMETER, QUANTITY_QNT_PARAMETER, PRICE_PLANCK_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {

        val asset = dp.parameterService.getAsset(request)
        val pricePlanck = ParameterParser.getPricePlanck(request)
        val quantity = ParameterParser.getQuantity(request)
        val feePlanck = ParameterParser.getFeePlanck(request)
        val account = dp.parameterService.getSenderAccount(request)

        try {
            if (feePlanck.safeAdd(pricePlanck.safeMultiply(quantity)) > account.unconfirmedBalancePlanck) {
                return NOT_ENOUGH_FUNDS
            }
        } catch (e: ArithmeticException) {
            return NOT_ENOUGH_FUNDS
        }

        val attachment = Attachment.ColoredCoinsBidOrderPlacement(dp, asset.id, quantity, pricePlanck, dp.blockchainService.height)
        return createTransaction(request, account, attachment)
    }
}
