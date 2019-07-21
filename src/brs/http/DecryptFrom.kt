package brs.http

import brs.Account
import brs.BurstException
import brs.crypto.EncryptedData
import brs.http.common.Parameters
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.DECRYPTION_FAILED
import brs.http.JSONResponses.INCORRECT_ACCOUNT
import brs.http.common.Parameters.*
import brs.http.common.ResultFields.DECRYPTED_MESSAGE_RESPONSE

internal class DecryptFrom internal constructor(private val parameterService: ParameterService) : APIServlet.JsonRequestHandler(arrayOf(APITag.MESSAGES), ACCOUNT_PARAMETER, DATA_PARAMETER, NONCE_PARAMETER, DECRYPTED_MESSAGE_IS_TEXT_PARAMETER, SECRET_PHRASE_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val account = parameterService.getAccount(req)
        if (account.publicKey == null) {
            return INCORRECT_ACCOUNT
        }
        val secretPhrase = ParameterParser.getSecretPhrase(req)
        val data = Convert.parseHexString(Convert.nullToEmpty(req.getParameter(DATA_PARAMETER)))
        val nonce = Convert.parseHexString(Convert.nullToEmpty(req.getParameter(NONCE_PARAMETER)))
        val encryptedData = EncryptedData(data, nonce)
        val isText = !Parameters.isFalse(req.getParameter(DECRYPTED_MESSAGE_IS_TEXT_PARAMETER))
        try {
            val decrypted = account.decryptFrom(encryptedData, secretPhrase)
            val response = JsonObject()
            response.addProperty(DECRYPTED_MESSAGE_RESPONSE, if (isText) Convert.toString(decrypted) else Convert.toHexString(decrypted))
            return response
        } catch (e: RuntimeException) {
            logger.debug(e.toString())
            return DECRYPTION_FAILED
        }

    }

    companion object {

        private val logger = LoggerFactory.getLogger(DecryptFrom::class.java)
    }

}
