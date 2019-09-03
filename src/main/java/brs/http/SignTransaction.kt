package brs.http

import brs.BurstException
import brs.Transaction
import brs.crypto.Crypto
import brs.services.ParameterService
import brs.services.TransactionService
import brs.util.Convert
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.http.HttpServletRequest
import java.util.Arrays

import brs.http.JSONResponses.MISSING_SECRET_PHRASE
import brs.http.common.ResultFields.*
import brs.http.common.ResultFields.FULL_HASH_RESPONSE

internal class SignTransaction(private val parameterService: ParameterService, private val transactionService: TransactionService) : APIServlet.JsonRequestHandler(arrayOf(APITag.TRANSACTIONS), UNSIGNED_TRANSACTION_BYTES_PARAMETER, UNSIGNED_TRANSACTION_JSON_PARAMETER, SECRET_PHRASE_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val transactionBytes = Convert.emptyToNull(req.getParameter(UNSIGNED_TRANSACTION_BYTES_PARAMETER))
        val transactionJSON = Convert.emptyToNull(req.getParameter(UNSIGNED_TRANSACTION_JSON_PARAMETER))
        val transaction = parameterService.parseTransaction(transactionBytes, transactionJSON)

        val secretPhrase = Convert.emptyToNull(req.getParameter(SECRET_PHRASE_PARAMETER))
                ?: return MISSING_SECRET_PHRASE

        val response = JsonObject()
        try {
            transactionService.validate(transaction)
            if (transaction.signature != null) {
                response.addProperty(ERROR_CODE_RESPONSE, 4)
                response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Incorrect unsigned transaction - already signed")
                return response
            }
            if (!Arrays.equals(Crypto.getPublicKey(secretPhrase), transaction.senderPublicKey)) {
                response.addProperty(ERROR_CODE_RESPONSE, 4)
                response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Secret phrase doesn't match transaction sender public key")
                return response
            }
            transaction.sign(secretPhrase)
            response.addProperty(TRANSACTION_RESPONSE, transaction.stringId)
            response.addProperty(FULL_HASH_RESPONSE, transaction.fullHash)
            response.addProperty(TRANSACTION_BYTES_RESPONSE, Convert.toHexString(transaction.bytes))
            response.addProperty(SIGNATURE_HASH_RESPONSE, Convert.toHexString(Crypto.sha256().digest(transaction.signature)))
            response.addProperty(VERIFY_RESPONSE, transaction.verifySignature() && transactionService.verifyPublicKey(transaction))
        } catch (e: BurstException.ValidationException) {
            logger.debug(e.message, e)
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Incorrect unsigned transaction: $e")
            response.addProperty(ERROR_RESPONSE, e.message)
            return response
        } catch (e: RuntimeException) {
            logger.debug(e.message, e)
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Incorrect unsigned transaction: $e")
            response.addProperty(ERROR_RESPONSE, e.message)
            return response
        }

        return response
    }

    companion object {

        private val logger = LoggerFactory.getLogger(SignTransaction::class.java)
    }

}
