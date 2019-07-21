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
import brs.http.common.ResultFields.ASK_ORDER_IDS_RESPONSE

internal class GetAccountCurrentAskOrderIds internal constructor(private val parameterService: ParameterService, private val assetExchange: AssetExchange) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS, APITag.AE), ACCOUNT_PARAMETER, ASSET_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

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

        val askOrders: Collection<Order.Ask>
        if (assetId == 0L) {
            askOrders = assetExchange.getAskOrdersByAccount(accountId, firstIndex, lastIndex)
        } else {
            askOrders = assetExchange.getAskOrdersByAccountAsset(accountId, assetId, firstIndex, lastIndex)
        }
        val orderIds = JsonArray()
        for (askOrder in askOrders) {
            orderIds.add(Convert.toUnsignedLong(askOrder.id))
        }
        val response = JsonObject()
        response.add(ASK_ORDER_IDS_RESPONSE, orderIds)
        return response
    }

}
