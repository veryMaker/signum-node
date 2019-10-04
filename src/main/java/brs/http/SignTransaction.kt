package brs.http

import brs.BurstException
import brs.crypto.Crypto
import brs.http.JSONResponses.MISSING_SECRET_PHRASE
import brs.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.http.common.Parameters.UNSIGNED_TRANSACTION_BYTES_PARAMETER
import brs.http.common.Parameters.UNSIGNED_TRANSACTION_JSON_PARAMETER
import brs.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE
import brs.http.common.ResultFields.ERROR_RESPONSE
import brs.http.common.ResultFields.FULL_HASH_RESPONSE
import brs.http.common.ResultFields.SIGNATURE_HASH_RESPONSE
import brs.http.common.ResultFields.TRANSACTION_BYTES_RESPONSE
import brs.http.common.ResultFields.TRANSACTION_RESPONSE
import brs.http.common.ResultFields.VERIFY_RESPONSE
import brs.services.ParameterService
import brs.services.TransactionService
import brs.util.convert.emptyToNull
import brs.util.convert.toHexString
import brs.util.logging.safeDebug
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest

internal class SignTransaction(private val parameterService: ParameterService, private val transactionService: TransactionService) : APIServlet.JsonRequestHandler(arrayOf(APITag.TRANSACTIONS), UNSIGNED_TRANSACTION_BYTES_PARAMETER, UNSIGNED_TRANSACTION_JSON_PARAMETER, SECRET_PHRASE_PARAMETER) {
    override suspend fun processRequest(request: HttpServletRequest): JsonElement {

        val transactionBytes = request.getParameter(UNSIGNED_TRANSACTION_BYTES_PARAMETER).emptyToNull()
        val transactionJSON = request.getParameter(UNSIGNED_TRANSACTION_JSON_PARAMETER).emptyToNull()
        val transaction = parameterService.parseTransaction(transactionBytes, transactionJSON)

        val secretPhrase = request.getParameter(SECRET_PHRASE_PARAMETER).emptyToNull()
                ?: return MISSING_SECRET_PHRASE

        val response = JsonObject()
        try {
            transactionService.validate(transaction)
            if (transaction.signature != null) {
                response.addProperty(ERROR_CODE_RESPONSE, 4)
                response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Incorrect unsigned transaction - already signed")
                return response
            }
            if (!Crypto.getPublicKey(secretPhrase).contentEquals(transaction.senderPublicKey)) {
                response.addProperty(ERROR_CODE_RESPONSE, 4)
                response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Secret phrase doesn't match transaction sender public key")
                return response
            }
            transaction.sign(secretPhrase)
            response.addProperty(TRANSACTION_RESPONSE, transaction.stringId)
            response.addProperty(FULL_HASH_RESPONSE, transaction.fullHash.toHexString())
            response.addProperty(TRANSACTION_BYTES_RESPONSE, transaction.bytes.toHexString())
            response.addProperty(SIGNATURE_HASH_RESPONSE, Crypto.sha256().digest(transaction.signature).toHexString())
            response.addProperty(VERIFY_RESPONSE, transaction.verifySignature() && transactionService.verifyPublicKey(transaction))
        } catch (e: BurstException.ValidationException) {
            logger.safeDebug(e) { e.message }
            response.addProperty(ERROR_CODE_RESPONSE, 4)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Incorrect unsigned transaction: $e")
            response.addProperty(ERROR_RESPONSE, e.message)
            return response
        } catch (e: RuntimeException) {
            logger.safeDebug(e) { e.message }
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
