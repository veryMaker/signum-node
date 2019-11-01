package brs.api.http

import brs.api.http.common.Parameters.BUYER_PARAMETER
import brs.api.http.common.Parameters.COMPLETED_PARAMETER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.Parameters.SELLER_PARAMETER
import brs.api.http.common.Parameters.isTrue
import brs.api.http.common.ResultFields.PURCHASES_RESPONSE
import brs.services.DigitalGoodsStoreService
import brs.util.FilteringIterator
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetDGSPurchases(private val digitalGoodsStoreService: DigitalGoodsStoreService) :
    APIServlet.JsonRequestHandler(
        arrayOf(APITag.DGS),
        SELLER_PARAMETER,
        BUYER_PARAMETER,
        FIRST_INDEX_PARAMETER,
        LAST_INDEX_PARAMETER,
        COMPLETED_PARAMETER
    ) {

    override fun processRequest(request: HttpServletRequest): JsonElement {
        val sellerId = ParameterParser.getSellerId(request)
        val buyerId = ParameterParser.getBuyerId(request)
        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)
        val completed = isTrue(request.getParameter(COMPLETED_PARAMETER))


        val response = JsonObject()
        val purchasesJSON = JsonArray()
        response.add(PURCHASES_RESPONSE, purchasesJSON)

        if (sellerId == 0L && buyerId == 0L) {
            val purchaseIterator = FilteringIterator(
                digitalGoodsStoreService.getAllPurchases(0, -1),
                { purchase -> purchase != null && !(completed && purchase.isPending) },
                firstIndex,
                lastIndex
            )
            while (purchaseIterator.hasNext()) {
                purchasesJSON.add(JSONData.purchase(purchaseIterator.next()))
            }
            return response
        }

        val purchases = if (sellerId != 0L && buyerId == 0L) {
            digitalGoodsStoreService.getSellerPurchases(sellerId, 0, -1)
        } else if (sellerId == 0L) {
            digitalGoodsStoreService.getBuyerPurchases(buyerId, 0, -1)
        } else {
            digitalGoodsStoreService.getSellerBuyerPurchases(sellerId, buyerId, 0, -1)
        }
        val purchaseIterator =
            FilteringIterator(purchases, { purchase -> !(completed && purchase.isPending) }, firstIndex, lastIndex)
        while (purchaseIterator.hasNext()) {
            purchasesJSON.add(JSONData.purchase(purchaseIterator.next()))
        }
        return response
    }
}
