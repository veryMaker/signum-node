package brs.api.http

import brs.api.http.common.JSONData
import brs.api.http.common.JSONResponses.INCORRECT_TRANSACTION
import brs.api.http.common.JSONResponses.MISSING_TRANSACTION
import brs.api.http.common.JSONResponses.UNKNOWN_TRANSACTION
import brs.api.http.common.Parameters.FULL_HASH_PARAMETER
import brs.api.http.common.Parameters.TRANSACTION_PARAMETER
import brs.entity.Transaction
import brs.services.BlockchainService
import brs.util.convert.emptyToNull
import brs.util.convert.parseHexString
import brs.util.convert.parseUnsignedLong
import brs.util.jetty.get
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetTransaction(private val blockchainService: BlockchainService) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.TRANSACTIONS), TRANSACTION_PARAMETER, FULL_HASH_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val transactionIdString = request[TRANSACTION_PARAMETER].emptyToNull()
        val transactionFullHash = request[FULL_HASH_PARAMETER].emptyToNull()
        if (transactionIdString == null && transactionFullHash == null) {
            return MISSING_TRANSACTION
        }

        val transaction: Transaction?
        try {
            transaction = when {
                transactionIdString != null -> blockchainService.getTransaction(transactionIdString.parseUnsignedLong())
                transactionFullHash != null -> blockchainService.getTransactionByFullHash(transactionFullHash.parseHexString())
                else -> null
            }
            if (transaction == null) {
                return UNKNOWN_TRANSACTION
            }
        } catch (e: Exception) {
            return INCORRECT_TRANSACTION
        }

        return JSONData.transaction(transaction, blockchainService.height)
    }
}
