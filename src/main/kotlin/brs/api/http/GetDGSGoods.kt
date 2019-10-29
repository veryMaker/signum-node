package brs.api.http

import brs.api.http.common.Parameters
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.IN_STOCK_ONLY_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.Parameters.SELLER_PARAMETER
import brs.api.http.common.ResultFields.GOODS_RESPONSE
import brs.services.DigitalGoodsStoreService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetDGSGoods(private val digitalGoodsStoreService: DigitalGoodsStoreService) :
    APIServlet.JsonRequestHandler(
        arrayOf(APITag.DGS),
        SELLER_PARAMETER,
        FIRST_INDEX_PARAMETER,
        LAST_INDEX_PARAMETER,
        IN_STOCK_ONLY_PARAMETER
    ) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val sellerId = ParameterParser.getSellerId(request)
        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)
        val inStockOnly = !Parameters.isFalse(request.getParameter(IN_STOCK_ONLY_PARAMETER))

        val response = JsonObject()
        val goodsJSON = JsonArray()
        response.add(GOODS_RESPONSE, goodsJSON)

        val goods = if (sellerId == 0L) {
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
