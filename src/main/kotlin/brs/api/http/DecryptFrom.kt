package brs.api.http

import brs.api.http.JSONResponses.DECRYPTION_FAILED
import brs.api.http.JSONResponses.INCORRECT_ACCOUNT
import brs.api.http.common.Parameters
import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.DATA_PARAMETER
import brs.api.http.common.Parameters.DECRYPTED_MESSAGE_IS_TEXT_PARAMETER
import brs.api.http.common.Parameters.NONCE_PARAMETER
import brs.api.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.api.http.common.ResultFields.DECRYPTED_MESSAGE_RESPONSE
import brs.services.ParameterService
import brs.util.convert.parseHexString
import brs.util.convert.toHexString
import brs.util.convert.toUtf8String
import brs.util.logging.safeDebug
import burst.kit.entity.BurstEncryptedMessage
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class DecryptFrom internal constructor(private val parameterService: ParameterService) :
    APIServlet.JsonRequestHandler(
        arrayOf(APITag.MESSAGES),
        ACCOUNT_PARAMETER,
        DATA_PARAMETER,
        NONCE_PARAMETER,
        DECRYPTED_MESSAGE_IS_TEXT_PARAMETER,
        SECRET_PHRASE_PARAMETER
    ) {

    override fun processRequest(request: HttpServletRequest): JsonElement {
        val account = parameterService.getAccount(request) ?: return INCORRECT_ACCOUNT
        if (account.publicKey == null) {
            return INCORRECT_ACCOUNT
        }
        val secretPhrase = ParameterParser.getSecretPhrase(request)
        val data = request.getParameter(DATA_PARAMETER).orEmpty().parseHexString()
        val nonce = request.getParameter(NONCE_PARAMETER).orEmpty().parseHexString()
        val isText = !Parameters.isFalse(request.getParameter(DECRYPTED_MESSAGE_IS_TEXT_PARAMETER))
        val encryptedData = BurstEncryptedMessage(data, nonce, isText)
        return try {
            val decrypted = account.decryptFrom(encryptedData, secretPhrase)
            val response = JsonObject()
            response.addProperty(
                DECRYPTED_MESSAGE_RESPONSE,
                if (isText) decrypted.toUtf8String() else decrypted.toHexString()
            )
            response
        } catch (e: Exception) {
            logger.safeDebug { e.toString() }
            DECRYPTION_FAILED
        }

    }

    companion object {
        private val logger = LoggerFactory.getLogger(DecryptFrom::class.java)
    }
}
