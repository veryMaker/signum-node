package brs.http

import brs.BurstException
import brs.DigitalGoodsStore
import brs.http.common.Parameters
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.IN_STOCK_ONLY_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.Parameters.SELLER_PARAMETER
import brs.http.common.ResultFields.GOODS_RESPONSE
import brs.services.DGSGoodsStoreService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetDGSGoods(private val digitalGoodsStoreService: DGSGoodsStoreService) : APIServlet.JsonRequestHandler(arrayOf(APITag.DGS), SELLER_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, IN_STOCK_ONLY_PARAMETER) {

    internal override fun processRequest(request: HttpServletRequest): JsonElement {
        val sellerId = ParameterParser.getSellerId(request)
        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)
        val inStockOnly = !Parameters.isFalse(request.getParameter(IN_STOCK_ONLY_PARAMETER))

        val response = JsonObject()
        val goodsJSON = JsonArray()
        response.add(GOODS_RESPONSE, goodsJSON)

        var goods: Collection<DigitalGoodsStore.Goods>? = null
        goods = if (sellerId == 0L) {
            if (inStockOnly) {
                digitalGoodsStoreService.getGoodsInStock(firstIndex, lastIndex)
            } else {
                digitalGoodsStoreService.getAllGoods(firstIndex, lastIndex)
            }
        } else {
            digitalGoodsStoreService.getSellerGoods(sellerId, inStockOnly, firstIndex, lastIndex)
        }
        for (good in goods) {
            goodsJSON.add(JSONData.goods(good))
        }

        return response
    }

}
