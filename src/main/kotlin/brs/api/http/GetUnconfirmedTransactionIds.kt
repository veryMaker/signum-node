package brs.api.http

import brs.api.http.JSONResponses.INCORRECT_ACCOUNT
import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.INCLUDE_INDIRECT_PARAMETER
import brs.api.http.common.ResultFields.UNCONFIRMED_TRANSACTIONS_IDS_RESPONSE
import brs.entity.DependencyProvider
import brs.util.convert.emptyToNull
import brs.util.convert.parseAccountId
import com.google.gson.JsonArray
import brs.util.jetty.get
import com.google.gson.JsonElement
import brs.util.jetty.get
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetUnconfirmedTransactionIds(private val dp: DependencyProvider) : APIServlet.JsonRequestHandler(
    arrayOf(APITag.TRANSACTIONS, APITag.ACCOUNTS),
    ACCOUNT_PARAMETER,
    INCLUDE_INDIRECT_PARAMETER
) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val accountIdString = request[ACCOUNT_PARAMETER].emptyToNull()
        val includeIndirect = dp.parameterService.getIncludeIndirect(request)

        var accountId: Long = 0

        if (accountIdString != null) {
            try {
                accountId = accountIdString.parseAccountId()
            } catch (e: Exception) {
                return INCORRECT_ACCOUNT
            }

        }

        val transactionIds = JsonArray()

        val unconfirmedTransactions = dp.unconfirmedTransactionService.all

        for (transaction in unconfirmedTransactions) {
            if (accountId == 0L
                || accountId == transaction.senderId || accountId == transaction.recipientId
                || includeIndirect && dp.indirectIncomingService.isIndirectlyReceiving(transaction, accountId)
            ) {
                transactionIds.add(transaction.stringId)
            }
        }

        val response = JsonObject()

        response.add(UNCONFIRMED_TRANSACTIONS_IDS_RESPONSE, transactionIds)

        return response
    }

}
