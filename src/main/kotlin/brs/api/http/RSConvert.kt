package brs.api.http

import brs.api.http.common.JSONResponses.INCORRECT_ACCOUNT
import brs.api.http.common.JSONResponses.MISSING_ACCOUNT
import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.JSONData
import brs.util.convert.emptyToNull
import brs.util.convert.parseAccountId
import brs.util.jetty.get
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal object RSConvert : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS, APITag.UTILS), ACCOUNT_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val accountValue = request[ACCOUNT_PARAMETER].emptyToNull() ?: return MISSING_ACCOUNT
        try {
            val accountId = accountValue.parseAccountId()
            if (accountId == 0L) {
                return INCORRECT_ACCOUNT
            }
            val response = JsonObject()
            JSONData.putAccount(response, "account", accountId)
            return response
        } catch (e: Exception) {
            return INCORRECT_ACCOUNT
        }
    }
}
