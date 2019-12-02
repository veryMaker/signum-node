package brs.api.http

import brs.api.http.JSONResponses.MISSING_DOMAIN
import brs.api.http.JSONResponses.PAYLOAD_WITHOUT_ACTION
import brs.api.http.JSONResponses.incorrect
import brs.api.http.common.Parameters.ACTION_PARAMETER
import brs.api.http.common.Parameters.DOMAIN_PARAMETER
import brs.api.http.common.Parameters.PAYLOAD_PARAMETER
import brs.api.http.common.ResultFields.DEEPLINK_RESPONSE
import brs.services.DeeplinkGeneratorService
import brs.util.convert.emptyToNull
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest

internal class GenerateDeeplink(private val deeplinkGeneratorService: DeeplinkGeneratorService) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.UTILS), DOMAIN_PARAMETER, ACTION_PARAMETER, PAYLOAD_PARAMETER) {
    private val logger = LoggerFactory.getLogger(GenerateDeeplink::class.java)

    override fun processRequest(request: HttpServletRequest): JsonElement {
        try {

            val domain = request.getParameter(DOMAIN_PARAMETER).emptyToNull()
            if (domain.isNullOrEmpty()) {
                return MISSING_DOMAIN
            }

            val action = request.getParameter(ACTION_PARAMETER).emptyToNull()
            val payload = request.getParameter(PAYLOAD_PARAMETER).emptyToNull()

            if (action.isNullOrEmpty() && !payload.isNullOrEmpty()) {
                return PAYLOAD_WITHOUT_ACTION
            }

            val deepLink = deeplinkGeneratorService.generateDeepLink(domain, action, payload)
            val response = JsonObject()
            response.addProperty(DEEPLINK_RESPONSE, deepLink)
            return response

        } catch (e: IllegalArgumentException) {
            logger.error("Problem with arguments", e)
            return incorrect("arguments", e.message)
        }
    }
}
