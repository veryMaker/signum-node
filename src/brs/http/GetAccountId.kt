package brs.http

import brs.Account
import brs.crypto.Crypto
import brs.util.Convert
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.MISSING_SECRET_PHRASE_OR_PUBLIC_KEY
import brs.http.common.Parameters.PUBLIC_KEY_PARAMETER
import brs.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.http.common.ResultFields.ACCOUNT_RESPONSE
import brs.http.common.ResultFields.PUBLIC_KEY_RESPONSE

internal class GetAccountId : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), SECRET_PHRASE_PARAMETER, PUBLIC_KEY_PARAMETER) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val accountId: Long
        val secretPhrase = Convert.emptyToNull(req.getParameter(SECRET_PHRASE_PARAMETER))
        var publicKeyString = Convert.emptyToNull(req.getParameter(PUBLIC_KEY_PARAMETER))
        if (secretPhrase != null) {
            val publicKey = Crypto.getPublicKey(secretPhrase)
            accountId = Account.getId(publicKey)
            publicKeyString = Convert.toHexString(publicKey)
        } else if (publicKeyString != null) {
            accountId = Account.getId(Convert.parseHexString(publicKeyString))
        } else {
            return MISSING_SECRET_PHRASE_OR_PUBLIC_KEY
        }

        val response = JsonObject()
        JSONData.putAccount(response, ACCOUNT_RESPONSE, accountId)
        response.addProperty(PUBLIC_KEY_RESPONSE, publicKeyString)

        return response
    }

    internal override fun requirePost(): Boolean {
        return true
    }

    companion object {

        internal val instance = GetAccountId()
    }

}
