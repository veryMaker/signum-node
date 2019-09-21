package brs.http


import brs.Blockchain
import brs.Transaction
import brs.TransactionProcessor
import brs.http.JSONResponses.INCORRECT_TRANSACTION
import brs.http.JSONResponses.MISSING_TRANSACTION
import brs.http.JSONResponses.UNKNOWN_TRANSACTION
import brs.http.common.Parameters.TRANSACTION_PARAMETER
import brs.util.parseUnsignedLong
import brs.util.toHexString
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetTransactionBytes(private val blockchain: Blockchain, private val transactionProcessor: TransactionProcessor) : APIServlet.JsonRequestHandler(arrayOf(APITag.TRANSACTIONS), TRANSACTION_PARAMETER) {
    internal override fun processRequest(request: HttpServletRequest): JsonElement {
        val transactionValue = request.getParameter(TRANSACTION_PARAMETER) ?: return MISSING_TRANSACTION

        val transactionId: Long
        var transaction: Transaction?
        try {
            transactionId = transactionValue.parseUnsignedLong()
        } catch (e: RuntimeException) {
            return INCORRECT_TRANSACTION
        }

        transaction = blockchain.getTransaction(transactionId)
        val response = JsonObject()
        if (transaction == null) {
            transaction = transactionProcessor.getUnconfirmedTransaction(transactionId)
            if (transaction == null) {
                return UNKNOWN_TRANSACTION
            }
        } else {
            response.addProperty("confirmations", blockchain.height - transaction.height)
        }

        response.addProperty("transactionBytes", transaction.bytes.toHexString())
        response.addProperty("unsignedTransactionBytes", transaction.unsignedBytes.toHexString())

        return response
    }
}
