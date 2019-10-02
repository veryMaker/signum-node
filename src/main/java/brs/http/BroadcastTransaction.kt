package brs.http

import brs.BurstException
import brs.TransactionProcessor
import brs.http.common.Parameters.TRANSACTION_BYTES_PARAMETER
import brs.http.common.Parameters.TRANSACTION_JSON_PARAMETER
import brs.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE
import brs.http.common.ResultFields.ERROR_RESPONSE
import brs.http.common.ResultFields.FULL_HASH_RESPONSE
import brs.http.common.ResultFields.NUMBER_PEERS_SENT_TO_RESPONSE
import brs.http.common.ResultFields.TRANSACTION_RESPONSE
import brs.services.ParameterService
import brs.services.TransactionService
import brs.util.Convert
import brs.util.toHexString
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.util.logging.Level
import java.util.logging.Logger
import javax.servlet.http.HttpServletRequest

internal class BroadcastTransaction(private val transactionProcessor: TransactionProcessor, private val parameterService: ParameterService, private val transactionService: TransactionService) : APIServlet.JsonRequestHandler(arrayOf(APITag.TRANSACTIONS), TRANSACTION_BYTES_PARAMETER, TRANSACTION_JSON_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {

        val transactionBytes = Convert.emptyToNull(request.getParameter(TRANSACTION_BYTES_PARAMETER))
        val transactionJSON = Convert.emptyToNull(request.getParameter(TRANSACTION_JSON_PARAMETER))
        val transaction = parameterService.parseTransaction(transactionBytes, transactionJSON)
        val response = JsonObject()
        try {
            transactionService.validate(transaction)
            response.addProperty(NUMBER_PEERS_SENT_TO_RESPONSE, transactionProcessor.broadcast(transaction))
            response.addProperty(TRANSACTION_RESPONSE, transaction.stringId)
            response.addProperty(FULL_HASH_RESPONSE, transaction.fullHash.toHexString())
        } catch (e: BurstException.ValidationException) {
            logger.log(Level.INFO, e.message, e)
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Incorrect transaction: $e")
            response.addProperty(ERROR_RESPONSE, e.message)
        } catch (e: RuntimeException) {
            logger.log(Level.INFO, e.message, e)
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

        private val logger = Logger.getLogger(BroadcastTransaction::class.java.simpleName)
    }

}
