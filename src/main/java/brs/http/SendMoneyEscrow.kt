package brs.http

import brs.*
import brs.http.common.Parameters.AMOUNT_NQT_PARAMETER
import brs.http.common.Parameters.DEADLINE_ACTION_PARAMETER
import brs.http.common.Parameters.ESCROW_DEADLINE_PARAMETER
import brs.http.common.Parameters.RECIPIENT_PARAMETER
import brs.http.common.Parameters.REQUIRED_SIGNERS_PARAMETER
import brs.http.common.Parameters.SIGNERS_PARAMETER
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest
import java.util.ArrayList
import brs.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE

internal class SendMoneyEscrow(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.TRANSACTIONS, APITag.CREATE_TRANSACTION), RECIPIENT_PARAMETER, AMOUNT_NQT_PARAMETER, ESCROW_DEADLINE_PARAMETER, SIGNERS_PARAMETER, REQUIRED_SIGNERS_PARAMETER, DEADLINE_ACTION_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        val sender = dp.parameterService.getSenderAccount(request)
        val recipient = ParameterParser.getRecipientId(request)
        val amountNQT = ParameterParser.getAmountNQT(request)
        val signerString = Convert.emptyToNull(request.getParameter(SIGNERS_PARAMETER))

        val requiredSigners: Long
        try {
            requiredSigners = java.lang.Long.parseLong(request.getParameter(REQUIRED_SIGNERS_PARAMETER))
            if (requiredSigners < 1 || requiredSigners > 10) {
                val response = JsonObject()
                response.addProperty(ERROR_CODE_RESPONSE, 4)
                response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid number of requiredSigners")
                return response
            }
        } catch (e: Exception) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid requiredSigners parameter")
            return response
        }

        if (signerString == null) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 3)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Signers not specified")
            return response
        }

        val signersArray = signerString.split(";".toRegex(), 10).toTypedArray()

        if (signersArray.isEmpty() || signersArray.size > 10 || signersArray.size < requiredSigners) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid number of signers")
            return response
        }

        val signers = mutableListOf<Long>()

        try {
            for (signer in signersArray) {
                val id = Convert.parseAccountId(signer)
                signers.add(id)
            }
        } catch (e: Exception) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid signers parameter")
            return response
        }

        val totalAmountNQT = Convert.safeAdd(amountNQT, signers.size * Constants.ONE_BURST)
        if (sender.balanceNQT < totalAmountNQT) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 6)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Insufficient funds")
            return response
        }

        val deadline: Long
        try {
            deadline = java.lang.Long.parseLong(request.getParameter(ESCROW_DEADLINE_PARAMETER))
            if (deadline < 1 || deadline > 7776000) {
                val response = JsonObject()
                response.addProperty(ERROR_CODE_RESPONSE, 4)
                response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Escrow deadline must be 1 - 7776000")
                return response
            }
        } catch (e: Exception) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid $ESCROW_DEADLINE_PARAMETER parameter")
            return response
        }

        val deadlineAction = Escrow.stringToDecision(request.getParameter(DEADLINE_ACTION_PARAMETER))
        if (deadlineAction == null || deadlineAction == Escrow.DecisionType.UNDECIDED) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid $DEADLINE_ACTION_PARAMETER parameter")
            return response
        }

        val attachment = Attachment.AdvancedPaymentEscrowCreation(dp, amountNQT, deadline.toInt(), deadlineAction, requiredSigners.toInt(), signers, dp.blockchain.height)

        return createTransaction(request, sender, recipient, 0, attachment)
    }
}
