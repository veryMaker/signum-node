package brs.http

import brs.*
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest
import java.util.AbstractMap.SimpleEntry
import java.util.ArrayList
import kotlin.collections.Map.Entry
import brs.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE

internal class SendMoneyMulti(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf<APITag>(APITag.TRANSACTIONS, APITag.CREATE_TRANSACTION), true, *commonParameters) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val sender = dp.parameterService.getSenderAccount(req)
        val recipientString = Convert.emptyToNull(req.getParameter(RECIPIENTS_PARAMETER))

        if (recipientString == null) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 3)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Recipients not specified")
            return response
        }

        val transactionArray = recipientString.split(";".toRegex(), Constants.MAX_MULTI_OUT_RECIPIENTS).toTypedArray()

        if (transactionArray.size > Constants.MAX_MULTI_OUT_RECIPIENTS || transactionArray.size < 2) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid number of recipients")
            return response
        }

        val recipients = mutableListOf<Entry<String, Long>>()

        var totalAmountNQT: Long = 0
        try {
            for (transactionString in transactionArray) {
                val recipientArray = transactionString.split(":".toRegex(), 2).toTypedArray()
                val recipientId = Convert.parseUnsignedLong(recipientArray[0])
                val amountNQT = Convert.parseUnsignedLong(recipientArray[1])
                recipients.add(SimpleEntry("" + recipientId, amountNQT))
                totalAmountNQT += amountNQT
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

        val attachment = Attachment.PaymentMultiOutCreation(recipients, dp.blockchain.height)

        return createTransaction(req, sender, null, attachment.amountNQT!!, attachment)
    }

    companion object {

        private val commonParameters = arrayOf(SECRET_PHRASE_PARAMETER, PUBLIC_KEY_PARAMETER, FEE_NQT_PARAMETER, DEADLINE_PARAMETER, REFERENCED_TRANSACTION_FULL_HASH_PARAMETER, BROADCAST_PARAMETER, RECIPIENTS_PARAMETER)
    }
}
