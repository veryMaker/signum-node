package brs.api.http

import brs.api.http.common.Parameters.SUBSCRIPTION_PARAMETER
import brs.api.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.api.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE
import brs.api.http.common.JSONData
import brs.services.SubscriptionService
import brs.util.convert.parseUnsignedLong
import brs.util.jetty.get
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetSubscription(private val subscriptionService: SubscriptionService) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), SUBSCRIPTION_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {
        val subscriptionId: Long
        try {
            subscriptionId = request[SUBSCRIPTION_PARAMETER].parseUnsignedLong()
        } catch (e: Exception) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 3)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid or not specified subscription")
            return response
        }

        val subscription = subscriptionService.getSubscription(subscriptionId)

        if (subscription == null) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 5)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Subscription not found")
            return response
        }

        return JSONData.subscription(subscription)
    }
}
