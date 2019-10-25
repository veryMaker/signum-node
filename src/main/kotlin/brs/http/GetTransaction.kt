package brs.http

import brs.Blockchain
import brs.Transaction
import brs.TransactionProcessor
import brs.http.JSONResponses.INCORRECT_TRANSACTION
import brs.http.JSONResponses.MISSING_TRANSACTION
import brs.http.JSONResponses.UNKNOWN_TRANSACTION
import brs.http.common.Parameters.FULL_HASH_PARAMETER
import brs.http.common.Parameters.TRANSACTION_PARAMETER
import brs.util.convert.emptyToNull
import brs.util.convert.parseHexString
import brs.util.convert.parseUnsignedLong
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class GetTransaction(private val transactionProcessor: TransactionProcessor, private val blockchain: Blockchain) : APIServlet.JsonRequestHandler(arrayOf(APITag.TRANSACTIONS), TRANSACTION_PARAMETER, FULL_HASH_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val transactionIdString = request.getParameter(TRANSACTION_PARAMETER).emptyToNull()
        val transactionFullHash = request.getParameter(FULL_HASH_PARAMETER).emptyToNull()
        if (transactionIdString == null && transactionFullHash == null) {
            return MISSING_TRANSACTION
        }

        var transactionId: Long = 0
        var transaction: Transaction? = null
        try {
            if (transactionIdString != null) {
                transactionId = transactionIdString.parseUnsignedLong()
                transaction = blockchain.getTransaction(transactionId)
            } else if (transactionFullHash != null) {
                transaction = blockchain.getTransactionByFullHash(transactionFullHash.parseHexString())
            }
            if (transaction == null) {
                return UNKNOWN_TRANSACTION
            }
        } catch (e: RuntimeException) {
            return INCORRECT_TRANSACTION
        }

        return JSONData.transaction(transaction, blockchain.height)
    }
}
