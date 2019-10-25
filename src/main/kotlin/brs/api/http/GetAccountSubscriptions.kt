package brs.api.http

import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.SUBSCRIPTIONS_RESPONSE
import brs.services.ParameterService
import brs.services.SubscriptionService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAccountSubscriptions internal constructor(private val parameterService: ParameterService, private val subscriptionService: SubscriptionService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), ACCOUNT_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {

        val account = parameterService.getAccount(request) ?: return JSONResponses.INCORRECT_ACCOUNT

        val response = JsonObject()

        val subscriptions = JsonArray()

        val accountSubscriptions = subscriptionService.getSubscriptionsByParticipant(account.id)

        for (accountSubscription in accountSubscriptions) {
            subscriptions.add(JSONData.subscription(accountSubscription))
        }

        response.add(SUBSCRIPTIONS_RESPONSE, subscriptions)
        return response
    }
}
