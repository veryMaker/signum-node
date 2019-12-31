package brs.api.http

import brs.api.http.common.JSONResponses.MISSING_SECRET_PHRASE_OR_PUBLIC_KEY
import brs.api.http.common.Parameters.PUBLIC_KEY_PARAMETER
import brs.api.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.api.http.common.ResultFields.ACCOUNT_RESPONSE
import brs.api.http.common.ResultFields.PUBLIC_KEY_RESPONSE
import brs.api.http.common.JSONData
import brs.util.convert.emptyToNull
import brs.util.convert.parseHexString
import brs.util.convert.publicKeyToId
import brs.util.convert.toHexString
import brs.util.crypto.Crypto
import brs.util.jetty.get
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetAccountId :
    APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), SECRET_PHRASE_PARAMETER, PUBLIC_KEY_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {

        val accountId: Long
        val secretPhrase = request[SECRET_PHRASE_PARAMETER].emptyToNull()
        var publicKeyString = request[PUBLIC_KEY_PARAMETER].emptyToNull()
        when {
            secretPhrase != null -> {
                val publicKey = Crypto.getPublicKey(secretPhrase)
                accountId = publicKey.publicKeyToId()
                publicKeyString = publicKey.toHexString()
            }
            publicKeyString != null -> accountId = publicKeyString.parseHexString().publicKeyToId()
            else -> return MISSING_SECRET_PHRASE_OR_PUBLIC_KEY
        }

        val response = JsonObject()
        JSONData.putAccount(response, ACCOUNT_RESPONSE, accountId)
        response.addProperty(PUBLIC_KEY_RESPONSE, publicKeyString)

        return response
    }

    override fun requirePost(): Boolean {
        return true
    }

    companion object {

        internal val instance = GetAccountId()
    }

}
