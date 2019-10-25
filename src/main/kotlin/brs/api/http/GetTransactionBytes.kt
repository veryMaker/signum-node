package brs.api.http


import brs.services.BlockchainService
import brs.entity.Transaction
import brs.services.TransactionProcessorService
import brs.api.http.JSONResponses.INCORRECT_TRANSACTION
import brs.api.http.JSONResponses.MISSING_TRANSACTION
import brs.api.http.JSONResponses.UNKNOWN_TRANSACTION
import brs.api.http.common.Parameters.TRANSACTION_PARAMETER
import brs.util.convert.parseUnsignedLong
import brs.util.convert.toHexString
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetTransactionBytes(private val blockchainService: BlockchainService, private val transactionProcessorService: TransactionProcessorService) : APIServlet.JsonRequestHandler(arrayOf(APITag.TRANSACTIONS), TRANSACTION_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val transactionValue = request.getParameter(TRANSACTION_PARAMETER) ?: return MISSING_TRANSACTION

        val transactionId: Long
        var transaction: Transaction?
        try {
            transactionId = transactionValue.parseUnsignedLong()
        } catch (e: RuntimeException) {
            return INCORRECT_TRANSACTION
        }

        transaction = blockchainService.getTransaction(transactionId)
        val response = JsonObject()
        if (transaction == null) {
            transaction = transactionProcessorService.getUnconfirmedTransaction(transactionId)
            if (transaction == null) {
                return UNKNOWN_TRANSACTION
            }
        } else {
            response.addProperty("confirmations", blockchainService.height - transaction.height)
        }

        response.addProperty("transactionBytes", transaction.toBytes().toHexString())
        response.addProperty("unsignedTransactionBytes", transaction.toUnsignedBytes().toHexString())

        return response
    }
}
