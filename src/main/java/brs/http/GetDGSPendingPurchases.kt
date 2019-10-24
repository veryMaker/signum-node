package brs.http

import brs.http.JSONResponses.MISSING_SELLER
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.Parameters.SELLER_PARAMETER
import brs.http.common.ResultFields.PURCHASES_RESPONSE
import brs.services.DGSGoodsStoreService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetDGSPendingPurchases internal constructor(private val dgsGoodStoreService: DGSGoodsStoreService) : APIServlet.JsonRequestHandler(arrayOf(APITag.DGS), SELLER_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {
        val sellerId = ParameterParser.getSellerId(request)

        if (sellerId == 0L) {
            return MISSING_SELLER
        }

        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        val response = JsonObject()
        val purchasesJSON = JsonArray()

        for (purchase in dgsGoodStoreService.getPendingSellerPurchases(sellerId, firstIndex, lastIndex)) {
            purchasesJSON.add(JSONData.purchase(purchase))
        }

        response.add(PURCHASES_RESPONSE, purchasesJSON)
        return response
    }

}
