package brs.http

import brs.*
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.UNKNOWN_GOODS
import brs.http.common.Parameters.GOODS_PARAMETER
import brs.http.common.Parameters.PRICE_NQT_PARAMETER

internal class DGSPriceChange internal constructor(private val parameterService: ParameterService, private val blockchain: Blockchain, apiTransactionManager: APITransactionManager) : CreateTransaction(arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), apiTransactionManager, GOODS_PARAMETER, PRICE_NQT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val account = parameterService.getSenderAccount(req)
        val goods = parameterService.getGoods(req)
        val priceNQT = ParameterParser.getPriceNQT(req)
        if (goods.isDelisted || goods.sellerId != account.getId()) {
            return UNKNOWN_GOODS
        }
        val attachment = Attachment.DigitalGoodsPriceChange(goods.id, priceNQT, blockchain.height)
        return createTransaction(req, account, attachment)
    }

}
