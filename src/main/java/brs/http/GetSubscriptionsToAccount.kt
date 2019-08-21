package brs.http

import brs.Account
import brs.BurstException
import brs.Subscription
import brs.services.ParameterService
import brs.services.SubscriptionService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.ACCOUNT_PARAMETER

internal class GetSubscriptionsToAccount(private val parameterService: ParameterService, private val subscriptionService: SubscriptionService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), ACCOUNT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val account = parameterService.getAccount(req)

        val response = JsonObject()

        val subscriptions = JsonArray()

        for (subscription in subscriptionService.getSubscriptionsToId(account.getId())) {
            subscriptions.add(JSONData.subscription(subscription))
        }

        response.add("subscriptions", subscriptions)
        return response
    }
}
