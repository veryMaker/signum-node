package brs.http

import brs.feesuggestions.FeeSuggestion
import brs.feesuggestions.FeeSuggestionCalculator
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.ResultFields.*

internal class SuggestFee(private val feeSuggestionCalculator: FeeSuggestionCalculator) : APIServlet.JsonRequestHandler(arrayOf(APITag.FEES)) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val feeSuggestion = feeSuggestionCalculator.giveFeeSuggestion()

        val response = JsonObject()

        response.addProperty(CHEAP_FEE_RESPONSE, feeSuggestion.cheapFee)
        response.addProperty(STANDARD_FEE_RESPONSE, feeSuggestion.standardFee)
        response.addProperty(PRIORITY_FEE_RESPONSE, feeSuggestion.priorityFee)

        return response
    }

}
