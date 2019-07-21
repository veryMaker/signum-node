package brs.http

import brs.Blockchain
import brs.Transaction
import brs.TransactionProcessor
import brs.util.Convert
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest


import brs.http.JSONResponses.INCORRECT_TRANSACTION
import brs.http.JSONResponses.MISSING_TRANSACTION
import brs.http.JSONResponses.UNKNOWN_TRANSACTION
import brs.http.common.Parameters.TRANSACTION_PARAMETER

internal class GetTransactionBytes(private val blockchain: Blockchain, private val transactionProcessor: TransactionProcessor) : APIServlet.JsonRequestHandler(arrayOf(APITag.TRANSACTIONS), TRANSACTION_PARAMETER) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val transactionValue = req.getParameter(TRANSACTION_PARAMETER) ?: return MISSING_TRANSACTION

        val transactionId: Long
        var transaction: Transaction?
        try {
            transactionId = Convert.parseUnsignedLong(transactionValue)
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

        response.addProperty("transactionBytes", Convert.toHexString(transaction.bytes))
        response.addProperty("unsignedTransactionBytes", Convert.toHexString(transaction.unsignedBytes))

        return response
    }

}
