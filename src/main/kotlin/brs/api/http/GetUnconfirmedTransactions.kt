package brs.api.http

import brs.services.TransactionProcessorService
import brs.api.http.JSONResponses.INCORRECT_ACCOUNT
import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.INCLUDE_INDIRECT_PARAMETER
import brs.api.http.common.ResultFields.UNCONFIRMED_TRANSACTIONS_RESPONSE
import brs.services.IndirectIncomingService
import brs.services.ParameterService
import brs.util.convert.emptyToNull
import brs.util.convert.parseAccountId
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class  GetUnconfirmedTransactions(private val transactionProcessorService: TransactionProcessorService, private val indirectIncomingService: IndirectIncomingService, private val parameterService: ParameterService) : APIServlet.JsonRequestHandler(arrayOf(APITag.TRANSACTIONS, APITag.ACCOUNTS), ACCOUNT_PARAMETER, INCLUDE_INDIRECT_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {
        val accountIdString = request.getParameter(ACCOUNT_PARAMETER).emptyToNull()
        val includeIndirect = parameterService.getIncludeIndirect(request)

        var accountId: Long = 0

        if (accountIdString != null) {
            try {
                accountId = accountIdString.parseAccountId()
            } catch (e: RuntimeException) {
                return INCORRECT_ACCOUNT
            }

        }

        val unconfirmedTransactions = transactionProcessorService.allUnconfirmedTransactions

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
