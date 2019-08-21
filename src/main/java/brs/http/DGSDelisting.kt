package brs.http

import brs.*
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.UNKNOWN_GOODS
import brs.http.common.Parameters.GOODS_PARAMETER

internal class DGSDelisting(private val parameterService: ParameterService, private val blockchain: Blockchain, apiTransactionManager: APITransactionManager) : CreateTransaction(arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), apiTransactionManager, GOODS_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val account = parameterService.getSenderAccount(req)
        val goods = parameterService.getGoods(req)
        if (goods.isDelisted || goods.sellerId != account.getId()) {
            return UNKNOWN_GOODS
        }
        val attachment = Attachment.DigitalGoodsDelisting(goods.id, blockchain.height)
        return createTransaction(req, account, attachment)
    }

}
