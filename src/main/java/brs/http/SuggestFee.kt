package brs.http

import brs.feesuggestions.FeeSuggestionCalculator
import brs.http.common.ResultFields.CHEAP_FEE_RESPONSE
import brs.http.common.ResultFields.PRIORITY_FEE_RESPONSE
import brs.http.common.ResultFields.STANDARD_FEE_RESPONSE
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class SuggestFee(private val feeSuggestionCalculator: FeeSuggestionCalculator) : APIServlet.JsonRequestHandler(arrayOf(APITag.FEES)) {
    internal override fun processRequest(request: HttpServletRequest): JsonElement {
        val feeSuggestion = feeSuggestionCalculator.giveFeeSuggestion()

        val response = JsonObject()

        response.addProperty(CHEAP_FEE_RESPONSE, feeSuggestion.cheapFee)
        response.addProperty(STANDARD_FEE_RESPONSE, feeSuggestion.standardFee)
        response.addProperty(PRIORITY_FEE_RESPONSE, feeSuggestion.priorityFee)

        return response
    }
}
