package brs.api.http

import brs.entity.Order
import brs.services.AssetExchangeService
import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.ASSET_PARAMETER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.BID_ORDERS_RESPONSE
import brs.services.ParameterService
import brs.util.convert.parseUnsignedLong
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAccountCurrentBidOrders internal constructor(private val parameterService: ParameterService, private val assetExchangeService: AssetExchangeService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS, APITag.AE), ACCOUNT_PARAMETER, ASSET_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {

        val accountId = parameterService.getAccount(request)?.id ?: return JSONResponses.INCORRECT_ACCOUNT
        var assetId: Long = 0
        try {
            assetId = request.getParameter(ASSET_PARAMETER).parseUnsignedLong()
        } catch (e: RuntimeException) {
            // ignore
        }

        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        val bidOrders: Collection<Order.Bid>
        bidOrders = if (assetId == 0L) {
            assetExchangeService.getBidOrdersByAccount(accountId, firstIndex, lastIndex)
        } else {
            assetExchangeService.getBidOrdersByAccountAsset(accountId, assetId, firstIndex, lastIndex)
        }
        val orders = JsonArray()
        for (bidOrder in bidOrders) {
            orders.add(JSONData.bidOrder(bidOrder))
        }
        val response = JsonObject()
        response.add(BID_ORDERS_RESPONSE, orders)
        return response
    }

}
