package brs.http

import brs.Token
import brs.services.TimeService
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.Constants.TOKEN
import brs.Constants.WEBSITE
import brs.http.JSONResponses.INCORRECT_WEBSITE
import brs.http.JSONResponses.MISSING_SECRET_PHRASE
import brs.http.JSONResponses.MISSING_WEBSITE

import brs.http.common.Parameters.SECRET_PHRASE_PARAMETER

internal class GenerateToken(private val timeService: TimeService) : APIServlet.JsonRequestHandler(arrayOf(APITag.TOKENS), WEBSITE, SECRET_PHRASE_PARAMETER) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val secretPhrase = req.getParameter(SECRET_PHRASE_PARAMETER)
        val website = req.getParameter(WEBSITE)
        if (secretPhrase == null) {
            return MISSING_SECRET_PHRASE
        } else if (website == null) {
            return MISSING_WEBSITE
        }

        try {

            val tokenString = Token.generateToken(secretPhrase, website.trim { it <= ' ' }, timeService.epochTime)

            val response = JsonObject()
            response.addProperty(TOKEN, tokenString)

            return response

        } catch (e: RuntimeException) {
            return INCORRECT_WEBSITE
        }

    }

    internal override fun requirePost(): Boolean {
        return true
    }

}
