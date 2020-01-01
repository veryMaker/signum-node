package brs.api.http


import brs.api.http.common.JSONResponses.INCORRECT_DELTA_QUANTITY
import brs.api.http.common.JSONResponses.MISSING_DELTA_QUANTITY
import brs.api.http.common.JSONResponses.UNKNOWN_GOODS
import brs.api.http.common.Parameters.DELTA_QUANTITY_PARAMETER
import brs.api.http.common.Parameters.GOODS_PARAMETER
import brs.entity.DependencyProvider
import brs.objects.Constants
import brs.transaction.appendix.Attachment
import brs.util.convert.emptyToNull
import brs.util.jetty.get
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class DGSQuantityChange internal constructor(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), GOODS_PARAMETER, DELTA_QUANTITY_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val account = dp.parameterService.getSenderAccount(request)
        val goods = dp.parameterService.getGoods(request)
        if (goods.isDelisted || goods.sellerId != account.id) {
            return UNKNOWN_GOODS
        }

        val deltaQuantity: Int
        try {
            val deltaQuantityString = request[DELTA_QUANTITY_PARAMETER].emptyToNull()
                ?: return MISSING_DELTA_QUANTITY
            deltaQuantity = Integer.parseInt(deltaQuantityString)
            if (deltaQuantity > Constants.MAX_DGS_LISTING_QUANTITY || deltaQuantity < -Constants.MAX_DGS_LISTING_QUANTITY) {
                return INCORRECT_DELTA_QUANTITY
            }
        } catch (e: NumberFormatException) {
            return INCORRECT_DELTA_QUANTITY
        }

        val attachment = Attachment.DigitalGoodsQuantityChange(dp, goods.id, deltaQuantity, dp.blockchainService.height)
        return createTransaction(request, account, attachment)
    }
}
