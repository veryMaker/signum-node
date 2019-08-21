package brs.http

import brs.BurstException
import brs.Transaction
import brs.services.ParameterService
import brs.services.TransactionService
import brs.util.Convert
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.TRANSACTION_BYTES_PARAMETER
import brs.http.common.Parameters.TRANSACTION_JSON_PARAMETER
import brs.http.common.ResultFields.*

internal class ParseTransaction(private val parameterService: ParameterService, private val transactionService: TransactionService) : APIServlet.JsonRequestHandler(arrayOf(APITag.TRANSACTIONS), TRANSACTION_BYTES_PARAMETER, TRANSACTION_JSON_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val transactionBytes = Convert.emptyToNull(req.getParameter(TRANSACTION_BYTES_PARAMETER))
        val transactionJSON = Convert.emptyToNull(req.getParameter(TRANSACTION_JSON_PARAMETER))
        val transaction = parameterService.parseTransaction(transactionBytes, transactionJSON)
        val response = JSONData.unconfirmedTransaction(transaction)
        try {
            transactionService.validate(transaction)
        } catch (e: BurstException.ValidationException) {
            logger.debug(e.message, e)
            response.addProperty(VALIDATE_RESPONSE, false)
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid transaction: $e")
            response.addProperty(ERROR_RESPONSE, e.message)
        } catch (e: RuntimeException) {
            logger.debug(e.message, e)
            response.addProperty(VALIDATE_RESPONSE, false)
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid transaction: $e")
            response.addProperty(ERROR_RESPONSE, e.message)
        }

        response.addProperty(VERIFY_RESPONSE, transaction.verifySignature() && transactionService.verifyPublicKey(transaction))
        return response
    }

    companion object {

        private val logger = LoggerFactory.getLogger(ParseTransaction::class.java)
    }

}
