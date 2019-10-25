package brs.http

import brs.Attachment
import brs.Constants
import brs.DependencyProvider
import brs.http.common.Parameters.AMOUNT_NQT_PARAMETER
import brs.http.common.Parameters.FREQUENCY_PARAMETER
import brs.http.common.Parameters.RECIPIENT_PARAMETER
import brs.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class SendMoneySubscription(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.TRANSACTIONS, APITag.CREATE_TRANSACTION), RECIPIENT_PARAMETER, AMOUNT_NQT_PARAMETER, FREQUENCY_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {
        val sender = dp.parameterService.getSenderAccount(request)
        val recipient = ParameterParser.getRecipientId(request)
        val amountNQT = ParameterParser.getAmountNQT(request)

        val frequency: Int
        try {
            frequency = Integer.parseInt(request.getParameter(FREQUENCY_PARAMETER))
        } catch (e: Exception) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid or missing frequency parameter")
            return response
        }

        if (frequency < Constants.BURST_SUBSCRIPTION_MIN_Frequest || frequency > Constants.BURST_SUBSCRIPTION_MAX_Frequest) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid frequency amount")
            return response
        }

        val attachment = Attachment.AdvancedPaymentSubscriptionSubscribe(dp, frequency, dp.blockchain.height)

        return createTransaction(request, sender, recipient, amountNQT, attachment)
    }
}
