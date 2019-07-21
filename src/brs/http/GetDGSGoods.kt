package brs.http

import brs.BurstException
import brs.DigitalGoodsStore
import brs.http.common.Parameters
import brs.services.DGSGoodsStoreService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.*
import brs.http.common.ResultFields.GOODS_RESPONSE

internal class GetDGSGoods(private val digitalGoodsStoreService: DGSGoodsStoreService) : APIServlet.JsonRequestHandler(arrayOf(APITag.DGS), SELLER_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, IN_STOCK_ONLY_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val sellerId = ParameterParser.getSellerId(req)
        val firstIndex = ParameterParser.getFirstIndex(req)
        val lastIndex = ParameterParser.getLastIndex(req)
        val inStockOnly = !Parameters.isFalse(req.getParameter(IN_STOCK_ONLY_PARAMETER))

        val response = JsonObject()
        val goodsJSON = JsonArray()
        response.add(GOODS_RESPONSE, goodsJSON)

        var goods: Collection<DigitalGoodsStore.Goods>? = null
        if (sellerId == 0L) {
            if (inStockOnly) {
                goods = digitalGoodsStoreService.getGoodsInStock(firstIndex, lastIndex)
            } else {
                goods = digitalGoodsStoreService.getAllGoods(firstIndex, lastIndex)
            }
        } else {
            goods = digitalGoodsStoreService.getSellerGoods(sellerId, inStockOnly, firstIndex, lastIndex)
        }
        for (good in goods!!) {
            goodsJSON.add(JSONData.goods(good))
        }

        return response
    }

}
