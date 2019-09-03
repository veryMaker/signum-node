package brs.http

import brs.BurstException
import brs.Order
import brs.assetexchange.AssetExchange
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest
import brs.http.common.ResultFields.BID_ORDERS_RESPONSE

internal class GetAccountCurrentBidOrders internal constructor(private val parameterService: ParameterService, private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS, APITag.AE), ACCOUNT_PARAMETER, ASSET_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val accountId = parameterService.getAccount(req).id
        var assetId: Long = 0
        try {
            assetId = Convert.parseUnsignedLong(req.getParameter(ASSET_PARAMETER))
        } catch (e: RuntimeException) {
            // ignore
        }

        val firstIndex = ParameterParser.getFirstIndex(req)
        val lastIndex = ParameterParser.getLastIndex(req)

        val bidOrders: Collection<Order.Bid>
        if (assetId == 0L) {
            bidOrders = assetExchange.getBidOrdersByAccount(accountId, firstIndex, lastIndex)
        } else {
            bidOrders = assetExchange.getBidOrdersByAccountAsset(accountId, assetId, firstIndex, lastIndex)
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
