package brs.http

import brs.BurstException
import brs.DigitalGoodsStore
import brs.http.common.Parameters
import brs.services.DGSGoodsStoreService
import brs.util.FilteringIterator
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.*
import brs.http.common.ResultFields.PURCHASES_RESPONSE

internal class GetDGSPurchases(private val dgsGoodsStoreService: DGSGoodsStoreService) : APIServlet.JsonRequestHandler(arrayOf(APITag.DGS), SELLER_PARAMETER, BUYER_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, COMPLETED_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val sellerId = ParameterParser.getSellerId(req)
        val buyerId = ParameterParser.getBuyerId(req)
        val firstIndex = ParameterParser.getFirstIndex(req)
        val lastIndex = ParameterParser.getLastIndex(req)
        val completed = isTrue(req.getParameter(COMPLETED_PARAMETER))


        val response = JsonObject()
        val purchasesJSON = JsonArray()
        response.add(PURCHASES_RESPONSE, purchasesJSON)

        if (sellerId == 0L && buyerId == 0L) {
            val purchaseIterator = FilteringIterator(dgsGoodsStoreService.getAllPurchases(0, -1), { purchase -> purchase != null && !(completed && purchase.isPending) }, firstIndex, lastIndex)
            while (purchaseIterator.hasNext()) {
                purchasesJSON.add(JSONData.purchase(purchaseIterator.next()))
            }
            return response
        }

        val purchases = if (sellerId != 0L && buyerId == 0L) {
            dgsGoodsStoreService.getSellerPurchases(sellerId, 0, -1)
        } else if (sellerId == 0L) {
            dgsGoodsStoreService.getBuyerPurchases(buyerId, 0, -1)
        } else {
            dgsGoodsStoreService.getSellerBuyerPurchases(sellerId, buyerId, 0, -1)
        }
        val purchaseIterator = FilteringIterator(purchases, { purchase -> purchase != null && !(completed && purchase.isPending) }, firstIndex, lastIndex)
        while (purchaseIterator.hasNext()) {
            purchasesJSON.add(JSONData.purchase(purchaseIterator.next()))
        }
        return response
    }
}
