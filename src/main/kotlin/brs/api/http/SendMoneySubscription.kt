package brs.api.http

import brs.api.http.common.Parameters.AMOUNT_PLANCK_PARAMETER
import brs.api.http.common.Parameters.FREQUENCY_PARAMETER
import brs.api.http.common.Parameters.RECIPIENT_PARAMETER
import brs.api.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.api.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE
import brs.entity.DependencyProvider
import brs.objects.Constants
import brs.transaction.appendix.Attachment
import brs.util.jetty.get
import com.google.gson.JsonElement
import brs.util.jetty.get
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class SendMoneySubscription(private val dp: DependencyProvider) : CreateTransaction(
    dp,
    arrayOf(APITag.TRANSACTIONS, APITag.CREATE_TRANSACTION),
    RECIPIENT_PARAMETER,
    AMOUNT_PLANCK_PARAMETER,
    FREQUENCY_PARAMETER
) {

    override fun processRequest(request: HttpServletRequest): JsonElement {
        val sender = dp.parameterService.getSenderAccount(request)
        val recipient = ParameterParser.getRecipientId(request)
        val amountPlanck = ParameterParser.getAmountPlanck(request)

        val frequency: Int
        try {
            frequency = Integer.parseInt(request[FREQUENCY_PARAMETER])
        } catch (e: Exception) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid or missing frequency parameter")
            return response
        }

        if (frequency < Constants.BURST_SUBSCRIPTION_MIN_FREQUENCY || frequency > Constants.BURST_SUBSCRIPTION_MAX_FREQUENCY) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid frequency amount")
            return response
        }

        val attachment = Attachment.AdvancedPaymentSubscriptionSubscribe(dp, frequency, dp.blockchainService.height)

        return createTransaction(request, sender, recipient, amountPlanck, attachment)
    }
}
