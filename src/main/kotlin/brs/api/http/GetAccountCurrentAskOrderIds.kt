package brs.api.http

import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.ASSET_PARAMETER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.ASK_ORDER_IDS_RESPONSE
import brs.entity.Order
import brs.services.AssetExchangeService
import brs.services.ParameterService
import brs.util.convert.parseUnsignedLong
import brs.util.convert.toUnsignedString
import com.google.gson.JsonArray
import brs.util.jetty.get
import com.google.gson.JsonElement
import brs.util.jetty.get
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetAccountCurrentAskOrderIds internal constructor(
    private val parameterService: ParameterService,
    private val assetExchangeService: AssetExchangeService
) : APIServlet.JsonRequestHandler(
    arrayOf(APITag.ACCOUNTS, APITag.AE),
    ACCOUNT_PARAMETER,
    ASSET_PARAMETER,
    FIRST_INDEX_PARAMETER,
    LAST_INDEX_PARAMETER
) {

    override fun processRequest(request: HttpServletRequest): JsonElement {
        val accountId = parameterService.getAccount(request).id
        var assetId: Long = 0
        try {
            assetId = request[ASSET_PARAMETER].parseUnsignedLong()
        } catch (e: Exception) {
            // ignore
        }

        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        val askOrders: Collection<Order.Ask>
        askOrders = if (assetId == 0L) {
            assetExchangeService.getAskOrdersByAccount(accountId, firstIndex, lastIndex)
        } else {
            assetExchangeService.getAskOrdersByAccountAsset(accountId, assetId, firstIndex, lastIndex)
        }
        val orderIds = JsonArray()
        for (askOrder in askOrders) {
            orderIds.add(askOrder.id.toUnsignedString())
        }
        val response = JsonObject()
        response.add(ASK_ORDER_IDS_RESPONSE, orderIds)
        return response
    }

}
