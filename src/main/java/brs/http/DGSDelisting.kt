package brs.http

import brs.*
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.UNKNOWN_GOODS
import brs.http.common.Parameters.GOODS_PARAMETER

internal class DGSDelisting(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), GOODS_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val account = dp.parameterService.getSenderAccount(req)
        val goods = dp.parameterService.getGoods(req)
        if (goods.isDelisted || goods.sellerId != account.id) {
            return UNKNOWN_GOODS
        }
        val attachment = Attachment.DigitalGoodsDelisting(goods.id, dp.blockchain.height)
        return createTransaction(req, account, attachment)
    }

}
