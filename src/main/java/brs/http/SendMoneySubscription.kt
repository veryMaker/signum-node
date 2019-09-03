package brs.http

import brs.*
import brs.services.ParameterService
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest
import brs.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE

internal class SendMoneySubscription(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.TRANSACTIONS, APITag.CREATE_TRANSACTION), RECIPIENT_PARAMETER, AMOUNT_NQT_PARAMETER, FREQUENCY_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val sender = dp.parameterService.getSenderAccount(req)
        val recipient = ParameterParser.getRecipientId(req)
        val amountNQT = ParameterParser.getAmountNQT(req)

        val frequency: Int
        try {
            frequency = Integer.parseInt(req.getParameter(FREQUENCY_PARAMETER))
        } catch (e: Exception) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid or missing frequency parameter")
            return response
        }

        if (frequency < Constants.BURST_SUBSCRIPTION_MIN_FREQ || frequency > Constants.BURST_SUBSCRIPTION_MAX_FREQ) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid frequency amount")
            return response
        }

        val attachment = Attachment.AdvancedPaymentSubscriptionSubscribe(frequency, dp.blockchain.height)

        return createTransaction(req, sender, recipient, amountNQT, attachment)
    }
}
