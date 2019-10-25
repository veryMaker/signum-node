package brs.api.http

import brs.api.http.common.ResultFields.CHEAP_FEE_RESPONSE
import brs.api.http.common.ResultFields.PRIORITY_FEE_RESPONSE
import brs.api.http.common.ResultFields.STANDARD_FEE_RESPONSE
import brs.services.FeeSuggestionService
import brs.services.impl.FeeSuggestionServiceImpl
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class SuggestFee(private val feeSuggestionService: FeeSuggestionService) : APIServlet.JsonRequestHandler(arrayOf(APITag.FEES)) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val feeSuggestion = feeSuggestionService.giveFeeSuggestion()

        val response = JsonObject()

        response.addProperty(CHEAP_FEE_RESPONSE, feeSuggestion.cheapFee)
        response.addProperty(STANDARD_FEE_RESPONSE, feeSuggestion.standardFee)
        response.addProperty(PRIORITY_FEE_RESPONSE, feeSuggestion.priorityFee)

        return response
    }
}
