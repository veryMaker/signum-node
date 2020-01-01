package brs.api.http

import brs.api.http.common.JSONResponses.UNKNOWN_GOODS
import brs.api.http.common.Parameters.GOODS_PARAMETER
import brs.entity.DependencyProvider
import brs.transaction.appendix.Attachment
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class DGSDelisting(private val dp: DependencyProvider) :
    CreateTransaction(dp, arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), GOODS_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val account = dp.parameterService.getSenderAccount(request)
        val goods = dp.parameterService.getGoods(request)
        if (goods.isDelisted || goods.sellerId != account.id) {
            return UNKNOWN_GOODS
        }
        val attachment = Attachment.DigitalGoodsDelisting(dp, goods.id, dp.blockchainService.height)
        return createTransaction(request, account, attachment)
    }
}
