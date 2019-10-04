package brs.http

import brs.Attachment
import brs.DependencyProvider
import brs.http.JSONResponses.UNKNOWN_GOODS
import brs.http.common.Parameters.GOODS_PARAMETER
import brs.http.common.Parameters.PRICE_NQT_PARAMETER
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class DGSPriceChange internal constructor(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), GOODS_PARAMETER, PRICE_NQT_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        val account = dp.parameterService.getSenderAccount(request)
        val goods = dp.parameterService.getGoods(request)
        val priceNQT = ParameterParser.getPriceNQT(request)
        if (goods.isDelisted || goods.sellerId != account.id) {
            return UNKNOWN_GOODS
        }
        val attachment = Attachment.DigitalGoodsPriceChange(dp, goods.id, priceNQT, dp.blockchain.height)
        return createTransaction(request, account, attachment)
    }

}
