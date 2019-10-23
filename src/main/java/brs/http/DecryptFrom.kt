package brs.http

import brs.crypto.EncryptedData
import brs.http.JSONResponses.DECRYPTION_FAILED
import brs.http.JSONResponses.INCORRECT_ACCOUNT
import brs.http.common.Parameters
import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.DATA_PARAMETER
import brs.http.common.Parameters.DECRYPTED_MESSAGE_IS_TEXT_PARAMETER
import brs.http.common.Parameters.NONCE_PARAMETER
import brs.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.http.common.ResultFields.DECRYPTED_MESSAGE_RESPONSE
import brs.services.ParameterService
import brs.util.convert.parseHexString
import brs.util.convert.toHexString
import brs.util.convert.toUtf8String
import brs.util.logging.safeDebug
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest

internal class DecryptFrom internal constructor(private val parameterService: ParameterService) : APIServlet.JsonRequestHandler(arrayOf(APITag.MESSAGES), ACCOUNT_PARAMETER, DATA_PARAMETER, NONCE_PARAMETER, DECRYPTED_MESSAGE_IS_TEXT_PARAMETER, SECRET_PHRASE_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        val account = parameterService.getAccount(request) ?: return INCORRECT_ACCOUNT
        if (account.publicKey == null) {
            return INCORRECT_ACCOUNT
        }
        val secretPhrase = ParameterParser.getSecretPhrase(request)
        val data = request.getParameter(DATA_PARAMETER).orEmpty().parseHexString()
        val nonce = request.getParameter(NONCE_PARAMETER).orEmpty().parseHexString()
        val encryptedData = EncryptedData(data, nonce)
        val isText = !Parameters.isFalse(request.getParameter(DECRYPTED_MESSAGE_IS_TEXT_PARAMETER))
        return try {
            val decrypted = account.decryptFrom(encryptedData, secretPhrase)
            val response = JsonObject()
            response.addProperty(DECRYPTED_MESSAGE_RESPONSE, if (isText) decrypted.toUtf8String() else decrypted.toHexString())
            response
        } catch (e: RuntimeException) {
            logger.safeDebug { e.toString() }
            DECRYPTION_FAILED
        }

    }

    companion object {
        private val logger = LoggerFactory.getLogger(DecryptFrom::class.java)
    }

}
