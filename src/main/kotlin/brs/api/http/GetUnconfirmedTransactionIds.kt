package brs.api.http

import brs.api.http.JSONResponses.INCORRECT_ACCOUNT
import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.INCLUDE_INDIRECT_PARAMETER
import brs.api.http.common.ResultFields.UNCONFIRMED_TRANSACTIONS_IDS_RESPONSE
import brs.services.IndirectIncomingService
import brs.services.ParameterService
import brs.services.TransactionProcessorService
import brs.util.convert.emptyToNull
import brs.util.convert.parseAccountId
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetUnconfirmedTransactionIds(
    private val transactionProcessorService: TransactionProcessorService,
    private val indirectIncomingService: IndirectIncomingService,
    private val parameterService: ParameterService
) : APIServlet.JsonRequestHandler(
    arrayOf(APITag.TRANSACTIONS, APITag.ACCOUNTS),
    ACCOUNT_PARAMETER,
    INCLUDE_INDIRECT_PARAMETER
) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val accountIdString = request.getParameter(ACCOUNT_PARAMETER).emptyToNull()
        val includeIndirect = parameterService.getIncludeIndirect(request)

        var accountId: Long = 0

        if (accountIdString != null) {
            try {
                accountId = accountIdString.parseAccountId()
            } catch (e: Exception) {
                return INCORRECT_ACCOUNT
            }

        }

        val transactionIds = JsonArray()

        val unconfirmedTransactions = transactionProcessorService.allUnconfirmedTransactions

        for (transaction in unconfirmedTransactions) {
            if (accountId == 0L
                || accountId == transaction.senderId || accountId == transaction.recipientId
                || includeIndirect && indirectIncomingService.isIndirectlyReceiving(transaction, accountId)
            ) {
                transactionIds.add(transaction.stringId)
            }
        }

        val response = JsonObject()

        response.add(UNCONFIRMED_TRANSACTIONS_IDS_RESPONSE, transactionIds)

        return response
    }

}
