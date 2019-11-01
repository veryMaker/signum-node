package brs.api.http

import brs.api.http.JSONResponses.INCORRECT_TRANSACTION
import brs.api.http.JSONResponses.MISSING_TRANSACTION
import brs.api.http.JSONResponses.UNKNOWN_TRANSACTION
import brs.api.http.common.Parameters.FULL_HASH_PARAMETER
import brs.api.http.common.Parameters.TRANSACTION_PARAMETER
import brs.entity.Transaction
import brs.services.BlockchainService
import brs.util.convert.emptyToNull
import brs.util.convert.parseHexString
import brs.util.convert.parseUnsignedLong
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetTransaction(private val blockchainService: BlockchainService) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.TRANSACTIONS), TRANSACTION_PARAMETER, FULL_HASH_PARAMETER) {
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
                transaction = blockchainService.getTransaction(transactionId)
            } else if (transactionFullHash != null) {
                transaction = blockchainService.getTransactionByFullHash(transactionFullHash.parseHexString())
            }
            if (transaction == null) {
                return UNKNOWN_TRANSACTION
            }
        } catch (e: RuntimeException) {
            return INCORRECT_TRANSACTION
        }

        return JSONData.transaction(transaction, blockchainService.height)
    }
}
