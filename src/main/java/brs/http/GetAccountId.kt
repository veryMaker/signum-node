package brs.http

import brs.Account
import brs.crypto.Crypto
import brs.http.JSONResponses.MISSING_SECRET_PHRASE_OR_PUBLIC_KEY
import brs.http.common.Parameters.PUBLIC_KEY_PARAMETER
import brs.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.http.common.ResultFields.ACCOUNT_RESPONSE
import brs.http.common.ResultFields.PUBLIC_KEY_RESPONSE
import brs.util.Convert
import brs.util.parseHexString
import brs.util.toHexString
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAccountId : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), SECRET_PHRASE_PARAMETER, PUBLIC_KEY_PARAMETER) {
    override suspend fun processRequest(request: HttpServletRequest): JsonElement {

        val accountId: Long
        val secretPhrase = Convert.emptyToNull(request.getParameter(SECRET_PHRASE_PARAMETER))
        var publicKeyString = Convert.emptyToNull(request.getParameter(PUBLIC_KEY_PARAMETER))
        when {
            secretPhrase != null -> {
                val publicKey = Crypto.getPublicKey(secretPhrase)
                accountId = Account.getId(publicKey)
                publicKeyString = publicKey.toHexString()
            }
            publicKeyString != null -> accountId = Account.getId(publicKeyString.parseHexString())
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
