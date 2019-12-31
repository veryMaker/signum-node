package brs.api.http

import brs.api.http.common.Parameters.TRANSACTION_BYTES_PARAMETER
import brs.api.http.common.Parameters.TRANSACTION_JSON_PARAMETER
import brs.api.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.api.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE
import brs.api.http.common.ResultFields.ERROR_RESPONSE
import brs.api.http.common.ResultFields.VALIDATE_RESPONSE
import brs.api.http.common.ResultFields.VERIFY_RESPONSE
import brs.services.ParameterService
import brs.services.TransactionService
import brs.util.BurstException
import brs.util.convert.emptyToNull
import brs.util.logging.safeDebug
import brs.util.jetty.get
import com.google.gson.JsonElement
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class ParseTransaction(
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
        val response = JSONData.unconfirmedTransaction(transaction)
        try {
            transactionService.validate(transaction)
        } catch (e: BurstException.ValidationException) {
            logger.safeDebug(e) { e.message }
            response.addProperty(VALIDATE_RESPONSE, false)
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid transaction: $e")
            response.addProperty(ERROR_RESPONSE, e.message)
        } catch (e: Exception) {
            logger.safeDebug(e) { e.message }
            response.addProperty(VALIDATE_RESPONSE, false)
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid transaction: $e")
            response.addProperty(ERROR_RESPONSE, e.message)
        }

        response.addProperty(
            VERIFY_RESPONSE,
            transaction.verifySignature() && transactionService.verifyPublicKey(transaction)
        )
        return response
    }

    companion object {

        private val logger = LoggerFactory.getLogger(ParseTransaction::class.java)
    }

}
