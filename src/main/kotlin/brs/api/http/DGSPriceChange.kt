package brs.api.http

import brs.api.http.common.JSONResponses.UNKNOWN_GOODS
import brs.api.http.common.Parameters.GOODS_PARAMETER
import brs.api.http.common.Parameters.PRICE_PLANCK_PARAMETER
import brs.entity.DependencyProvider
import brs.transaction.appendix.Attachment
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class DGSPriceChange internal constructor(private val dp: DependencyProvider) :
    CreateTransaction(dp, arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), GOODS_PARAMETER, PRICE_PLANCK_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val account = dp.parameterService.getSenderAccount(request)
        val goods = dp.parameterService.getGoods(request)
        val pricePlanck = ParameterParser.getPricePlanck(request)
        if (goods.isDelisted || goods.sellerId != account.id) {
            return UNKNOWN_GOODS
        }
        val attachment = Attachment.DigitalGoodsPriceChange(dp, goods.id, pricePlanck, dp.blockchainService.height)
        return createTransaction(request, account, attachment)
    }
}
