package brs.api.http

import brs.api.http.common.Parameters.TRANSACTION_BYTES_PARAMETER
import brs.api.http.common.Parameters.TRANSACTION_JSON_PARAMETER
import brs.api.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.api.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE
import brs.api.http.common.ResultFields.ERROR_RESPONSE
import brs.api.http.common.ResultFields.FULL_HASH_RESPONSE
import brs.api.http.common.ResultFields.NUMBER_PEERS_SENT_TO_RESPONSE
import brs.api.http.common.ResultFields.TRANSACTION_RESPONSE
import brs.services.ParameterService
import brs.services.TransactionProcessorService
import brs.services.TransactionService
import brs.util.BurstException
import brs.util.convert.emptyToNull
import brs.util.convert.toHexString
import brs.util.jetty.get
import brs.util.logging.safeInfo
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class BroadcastTransaction(
    private val transactionProcessorService: TransactionProcessorService,
    private val parameterService: ParameterService,
    private val transactionService: TransactionService
) : APIServlet.JsonRequestHandler(
    arrayOf(APITag.TRANSACTIONS),
    TRANSACTION_BYTES_PARAMETER,
    TRANSACTION_JSON_PARAMETER
) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val transactionBytes = request[TRANSACTION_BYTES_PARAMETER].emptyToNull()
        val transactionJSON = request[TRANSACTION_JSON_PARAMETER].emptyToNull()
        val transaction = parameterService.parseTransaction(transactionBytes, transactionJSON)
        val response = JsonObject()
        try {
            transactionService.validate(transaction)
            response.addProperty(NUMBER_PEERS_SENT_TO_RESPONSE, transactionProcessorService.broadcast(transaction))
            response.addProperty(TRANSACTION_RESPONSE, transaction.stringId)
            response.addProperty(FULL_HASH_RESPONSE, transaction.fullHash.toHexString())
        } catch (e: BurstException.ValidationException) {
            logger.safeInfo(e) { e.message }
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Incorrect transaction: $e")
            response.addProperty(ERROR_RESPONSE, e.message)
        } catch (e: Exception) {
            logger.safeInfo(e) { e.message }
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Incorrect transaction: $e")
            response.addProperty(ERROR_RESPONSE, e.message)
        }

        return response
    }

    override fun requirePost(): Boolean {
        return true
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BroadcastTransaction::class.java)
    }
}
