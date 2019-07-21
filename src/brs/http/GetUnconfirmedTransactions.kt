package brs.http

import brs.Transaction
import brs.TransactionProcessor
import brs.services.IndirectIncomingService
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.INCORRECT_ACCOUNT
import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.INCLUDE_INDIRECT_PARAMETER
import brs.http.common.ResultFields.UNCONFIRMED_TRANSACTIONS_RESPONSE

internal class GetUnconfirmedTransactions(private val transactionProcessor: TransactionProcessor, private val indirectIncomingService: IndirectIncomingService, private val parameterService: ParameterService) : APIServlet.JsonRequestHandler(arrayOf(APITag.TRANSACTIONS, APITag.ACCOUNTS), ACCOUNT_PARAMETER, INCLUDE_INDIRECT_PARAMETER) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val accountIdString = Convert.emptyToNull(req.getParameter(ACCOUNT_PARAMETER))
        val includeIndirect = parameterService.getIncludeIndirect(req)

        var accountId: Long = 0

        if (accountIdString != null) {
            try {
                accountId = Convert.parseAccountId(accountIdString)
            } catch (e: RuntimeException) {
                return INCORRECT_ACCOUNT
            }

        }

        val unconfirmedTransactions = transactionProcessor.allUnconfirmedTransactions

        val transactions = JsonArray()

        for (transaction in unconfirmedTransactions) {
            if (accountId == 0L
                    || accountId == transaction.senderId || accountId == transaction.recipientId
                    || includeIndirect && indirectIncomingService.isIndirectlyReceiving(transaction, accountId)) {
                transactions.add(JSONData.unconfirmedTransaction(transaction))
            }
        }

        val response = JsonObject()

        response.add(UNCONFIRMED_TRANSACTIONS_RESPONSE, transactions)

        return response
    }

}
