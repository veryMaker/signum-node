package brs.http

import brs.util.Convert
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.INCORRECT_ACCOUNT
import brs.http.JSONResponses.MISSING_ACCOUNT
import brs.http.common.Parameters.ACCOUNT_PARAMETER

internal class RSConvert private constructor() : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS, APITag.UTILS), ACCOUNT_PARAMETER) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val accountValue = Convert.emptyToNull(req.getParameter(ACCOUNT_PARAMETER)) ?: return MISSING_ACCOUNT
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

    companion object {

        val instance = RSConvert()
    }

}
