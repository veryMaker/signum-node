package brs.http

import brs.BurstException
import brs.DigitalGoodsStore
import brs.services.DGSGoodsStoreService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.MISSING_SELLER
import brs.http.common.ResultFields.PURCHASES_RESPONSE

internal class GetDGSPendingPurchases internal constructor(private val dgsGoodStoreService: DGSGoodsStoreService) : APIServlet.JsonRequestHandler(arrayOf(APITag.DGS), SELLER_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val sellerId = ParameterParser.getSellerId(req)

        if (sellerId == 0L) {
            return MISSING_SELLER
        }

        val firstIndex = ParameterParser.getFirstIndex(req)
        val lastIndex = ParameterParser.getLastIndex(req)

        val response = JsonObject()
        val purchasesJSON = JsonArray()

        for (purchase in dgsGoodStoreService.getPendingSellerPurchases(sellerId, firstIndex, lastIndex)) {
            purchasesJSON.add(JSONData.purchase(purchase))
        }

        response.add(PURCHASES_RESPONSE, purchasesJSON)
        return response
    }

}
