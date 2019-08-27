package brs.http

import brs.*
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest
import java.util.ArrayList

import brs.http.common.Parameters.*
import brs.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE

internal class SendMoneyMultiSame(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf<APITag>(APITag.TRANSACTIONS, APITag.CREATE_TRANSACTION), true, *commonParameters) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val amountNQT = ParameterParser.getAmountNQT(req)
        val sender = dp.parameterService.getSenderAccount(req)
        val recipientString = Convert.emptyToNull(req.getParameter(RECIPIENTS_PARAMETER))


        if (recipientString == null) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 3)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Recipients not specified")
            return response
        }

        val recipientsArray = recipientString.split(";".toRegex(), Constants.MAX_MULTI_SAME_OUT_RECIPIENTS).toTypedArray()

        if (recipientsArray.size > Constants.MAX_MULTI_SAME_OUT_RECIPIENTS || recipientsArray.size < 2) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid number of recipients")
            return response
        }

        val recipients = ArrayList<Long>()

        val totalAmountNQT = amountNQT * recipientsArray.size
        try {
            for (recipientId in recipientsArray) {
                recipients.add(Convert.parseUnsignedLong(recipientId))
            }
        } catch (e: Exception) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid recipients parameter")
            return response
        }

        if (sender.balanceNQT < totalAmountNQT) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 6)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Insufficient funds")
            return response
        }

        val attachment = Attachment.PaymentMultiSameOutCreation(recipients, dp.blockchain.height)

        return createTransaction(req, sender, null, totalAmountNQT, attachment)
    }

    companion object {

        private val commonParameters = arrayOf(SECRET_PHRASE_PARAMETER, PUBLIC_KEY_PARAMETER, FEE_NQT_PARAMETER, DEADLINE_PARAMETER, REFERENCED_TRANSACTION_FULL_HASH_PARAMETER, BROADCAST_PARAMETER, RECIPIENTS_PARAMETER, AMOUNT_NQT_PARAMETER)
    }
}
