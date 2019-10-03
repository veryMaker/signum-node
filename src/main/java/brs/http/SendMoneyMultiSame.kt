package brs.http

import brs.Attachment
import brs.Constants
import brs.DependencyProvider
import brs.http.common.Parameters.AMOUNT_NQT_PARAMETER
import brs.http.common.Parameters.BROADCAST_PARAMETER
import brs.http.common.Parameters.DEADLINE_PARAMETER
import brs.http.common.Parameters.FEE_NQT_PARAMETER
import brs.http.common.Parameters.PUBLIC_KEY_PARAMETER
import brs.http.common.Parameters.RECIPIENTS_PARAMETER
import brs.http.common.Parameters.REFERENCED_TRANSACTION_FULL_HASH_PARAMETER
import brs.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE
import brs.util.convert.emptyToNull
import brs.util.convert.parseUnsignedLong
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class SendMoneyMultiSame(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf<APITag>(APITag.TRANSACTIONS, APITag.CREATE_TRANSACTION), true, *commonParameters) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        val amountNQT = ParameterParser.getAmountNQT(request)
        val sender = dp.parameterService.getSenderAccount(request)
        val recipientString = request.getParameter(RECIPIENTS_PARAMETER).emptyToNull()


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

        val recipients = mutableListOf<Long>()

        val totalAmountNQT = amountNQT * recipientsArray.size
        try {
            for (recipientId in recipientsArray) {
                recipients.add(recipientId.parseUnsignedLong())
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

        val attachment = Attachment.PaymentMultiSameOutCreation(dp, recipients, dp.blockchain.height)

        return createTransaction(request, sender, null, totalAmountNQT, attachment)
    }

    companion object {

        private val commonParameters = arrayOf(SECRET_PHRASE_PARAMETER, PUBLIC_KEY_PARAMETER, FEE_NQT_PARAMETER, DEADLINE_PARAMETER, REFERENCED_TRANSACTION_FULL_HASH_PARAMETER, BROADCAST_PARAMETER, RECIPIENTS_PARAMETER, AMOUNT_NQT_PARAMETER)
    }
}
