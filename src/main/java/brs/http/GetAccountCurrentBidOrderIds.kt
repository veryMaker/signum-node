package brs.http

import brs.BurstException
import brs.Order
import brs.assetexchange.AssetExchange
import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.ASSET_PARAMETER
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.ResultFields.BID_ORDER_IDS_RESPONSE
import brs.services.ParameterService
import brs.util.parseUnsignedLong
import brs.util.toUnsignedString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAccountCurrentBidOrderIds internal constructor(private val parameterService: ParameterService, private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS, APITag.AE), ACCOUNT_PARAMETER, ASSET_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

    internal override fun processRequest(request: HttpServletRequest): JsonElement {

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
        if (assetId == 0L) {
            bidOrders = assetExchange.getBidOrdersByAccount(accountId, firstIndex, lastIndex)
        } else {
            bidOrders = assetExchange.getBidOrdersByAccountAsset(accountId, assetId, firstIndex, lastIndex)
        }
        val orderIds = JsonArray()
        for (bidOrder in bidOrders) {
            orderIds.add(bidOrder.id.toUnsignedString())
        }
        val response = JsonObject()
        response.add(BID_ORDER_IDS_RESPONSE, orderIds)
        return response
    }

}
