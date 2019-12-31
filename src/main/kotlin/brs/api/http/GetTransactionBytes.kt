package brs.api.http

import brs.api.http.common.JSONResponses.INCORRECT_TRANSACTION
import brs.api.http.common.JSONResponses.MISSING_TRANSACTION
import brs.api.http.common.JSONResponses.UNKNOWN_TRANSACTION
import brs.api.http.common.Parameters.TRANSACTION_PARAMETER
import brs.entity.DependencyProvider
import brs.entity.Transaction
import brs.util.convert.parseUnsignedLong
import brs.util.convert.toHexString
import brs.util.jetty.get
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetTransactionBytes(private val dp: DependencyProvider) : APIServlet.JsonRequestHandler(arrayOf(APITag.TRANSACTIONS), TRANSACTION_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val transactionValue = request[TRANSACTION_PARAMETER] ?: return MISSING_TRANSACTION

        val transactionId: Long
        var transaction: Transaction?
        try {
            transactionId = transactionValue.parseUnsignedLong()
        } catch (e: Exception) {
            return INCORRECT_TRANSACTION
        }

        transaction = dp.blockchainService.getTransaction(transactionId)
        val response = JsonObject()
        if (transaction == null) {
            transaction = dp.unconfirmedTransactionService.get(transactionId)
            if (transaction == null) {
                return UNKNOWN_TRANSACTION
            }
        } else {
            response.addProperty("confirmations", dp.blockchainService.height - transaction.height)
        }

        response.addProperty("transactionBytes", transaction.toBytes().toHexString())
        response.addProperty("unsignedTransactionBytes", transaction.toUnsignedBytes().toHexString())

        return response
    }
}
