package brs.http

import brs.Blockchain
import brs.Transaction
import brs.TransactionProcessor
import brs.util.Convert
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.INCORRECT_TRANSACTION
import brs.http.JSONResponses.MISSING_TRANSACTION
import brs.http.JSONResponses.UNKNOWN_TRANSACTION
import brs.http.common.Parameters.FULL_HASH_PARAMETER
import brs.http.common.Parameters.TRANSACTION_PARAMETER

internal class GetTransaction(private val transactionProcessor: TransactionProcessor, private val blockchain: Blockchain) : APIServlet.JsonRequestHandler(arrayOf(APITag.TRANSACTIONS), TRANSACTION_PARAMETER, FULL_HASH_PARAMETER) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val transactionIdString = Convert.emptyToNull(req.getParameter(TRANSACTION_PARAMETER))
        val transactionFullHash = Convert.emptyToNull(req.getParameter(FULL_HASH_PARAMETER))
        if (transactionIdString == null && transactionFullHash == null) {
            return MISSING_TRANSACTION
        }

        var transactionId: Long = 0
        var transaction: Transaction?
        try {
            if (transactionIdString != null) {
                transactionId = Convert.parseUnsignedLong(transactionIdString)
                transaction = blockchain.getTransaction(transactionId)
            } else {
                transaction = blockchain.getTransactionByFullHash(transactionFullHash!!)
                if (transaction == null) {
                    return UNKNOWN_TRANSACTION
                }
            }
        } catch (e: RuntimeException) {
            return INCORRECT_TRANSACTION
        }

        if (transaction == null) {
            transaction = transactionProcessor.getUnconfirmedTransaction(transactionId)
            return if (transaction == null) {
                UNKNOWN_TRANSACTION
            } else JSONData.unconfirmedTransaction(transaction)
        } else {
            return JSONData.transaction(transaction, blockchain.height)
        }

    }

}
