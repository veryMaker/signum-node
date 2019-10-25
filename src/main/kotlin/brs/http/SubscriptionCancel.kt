package brs.http

import brs.Attachment
import brs.DependencyProvider
import brs.http.common.Parameters.SUBSCRIPTION_PARAMETER
import brs.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE
import brs.util.convert.emptyToNull
import brs.util.convert.parseUnsignedLong
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class SubscriptionCancel(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.TRANSACTIONS, APITag.CREATE_TRANSACTION), SUBSCRIPTION_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {
        val sender = dp.parameterService.getSenderAccount(request)

        val subscriptionString = request.getParameter(SUBSCRIPTION_PARAMETER).emptyToNull()
        if (subscriptionString == null) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 3)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Subscription Id not specified")
            return response
        }

        val subscriptionId: Long
        try {
            subscriptionId = subscriptionString.parseUnsignedLong()
        } catch (e: Exception) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Failed to parse subscription id")
            return response
        }

        val subscription = dp.subscriptionService.getSubscription(subscriptionId)
        if (subscription == null) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 5)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Subscription not found")
            return response
        }

        if (sender.id != subscription.senderId && sender.id != subscription.recipientId) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 7)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Must be sender or recipient to cancel subscription")
            return response
        }

        val attachment = Attachment.AdvancedPaymentSubscriptionCancel(dp, subscription.id, dp.blockchain.height)

        return createTransaction(request, sender, null, 0, attachment)
    }
}
