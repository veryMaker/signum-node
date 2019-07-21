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

import brs.http.common.Parameters.*
import brs.http.common.ResultFields.ASK_ORDERS_RESPONSE

internal class GetAccountCurrentAskOrders internal constructor(private val parameterService: ParameterService, private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS, APITag.AE), ACCOUNT_PARAMETER, ASSET_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val accountId = parameterService.getAccount(req).getId()

        var assetId: Long = 0
        try {
            assetId = Convert.parseUnsignedLong(req.getParameter(ASSET_PARAMETER))
        } catch (e: RuntimeException) {
            // ignore
        }

        val firstIndex = ParameterParser.getFirstIndex(req)
        val lastIndex = ParameterParser.getLastIndex(req)

        val askOrders: Iterator<Order.Ask>
        if (assetId == 0L) {
            askOrders = assetExchange.getAskOrdersByAccount(accountId, firstIndex, lastIndex).iterator()
        } else {
            askOrders = assetExchange.getAskOrdersByAccountAsset(accountId, assetId, firstIndex, lastIndex).iterator()
        }
        val orders = JsonArray()
        while (askOrders.hasNext()) {
            orders.add(JSONData.askOrder(askOrders.next()))
        }
        val response = JsonObject()
        response.add(ASK_ORDERS_RESPONSE, orders)
        return response
    }

}
