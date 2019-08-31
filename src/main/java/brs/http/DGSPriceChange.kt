package brs.http

import brs.*
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.UNKNOWN_GOODS
import brs.http.common.Parameters.GOODS_PARAMETER
import brs.http.common.Parameters.PRICE_NQT_PARAMETER

internal class DGSPriceChange internal constructor(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), GOODS_PARAMETER, PRICE_NQT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val account = dp.parameterService.getSenderAccount(req)
        val goods = dp.parameterService.getGoods(req)
        val priceNQT = ParameterParser.getPriceNQT(req)
        if (goods.isDelisted || goods.sellerId != account.id) {
            return UNKNOWN_GOODS
        }
        val attachment = Attachment.DigitalGoodsPriceChange(goods.id, priceNQT, dp.blockchain.height)
        return createTransaction(req, account, attachment)
    }

}
