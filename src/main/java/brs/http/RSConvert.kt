package brs.http

import brs.util.Convert
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.INCORRECT_ACCOUNT
import brs.http.JSONResponses.MISSING_ACCOUNT
import brs.http.common.Parameters.ACCOUNT_PARAMETER

internal object RSConvert : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS, APITag.UTILS), ACCOUNT_PARAMETER) {
    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        val accountValue = Convert.emptyToNull(request.getParameter(ACCOUNT_PARAMETER)) ?: return MISSING_ACCOUNT
        try {
            val accountId = Convert.parseAccountId(accountValue)
            if (accountId == 0L) {
                return INCORRECT_ACCOUNT
            }
            val response = JsonObject()
            JSONData.putAccount(response, "account", accountId)
            return response
        } catch (e: RuntimeException) {
            return INCORRECT_ACCOUNT
        }

    }
}
