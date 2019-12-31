package brs.api.http

import brs.api.http.common.JSONResponses.MISSING_DOMAIN
import brs.api.http.common.JSONResponses.PAYLOAD_WITHOUT_ACTION
import brs.api.http.common.JSONResponses.incorrect
import brs.api.http.common.Parameters.ACTION_PARAMETER
import brs.api.http.common.Parameters.DOMAIN_PARAMETER
import brs.api.http.common.Parameters.PAYLOAD_PARAMETER
import brs.api.http.common.ResultFields.DEEPLINK_RESPONSE
import brs.services.DeeplinkGeneratorService
import brs.util.convert.emptyToNull
import brs.util.jetty.get
import brs.util.logging.safeDebug
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import javax.servlet.http.HttpServletRequest

internal class GenerateDeeplink(private val deeplinkGeneratorService: DeeplinkGeneratorService) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.UTILS), DOMAIN_PARAMETER, ACTION_PARAMETER, PAYLOAD_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        try {
            val domain = request[DOMAIN_PARAMETER].emptyToNull()
            if (domain.isNullOrEmpty()) {
                return MISSING_DOMAIN
            }

            val action = request[ACTION_PARAMETER].emptyToNull()
            val payload = request[PAYLOAD_PARAMETER].emptyToNull()

            if (action.isNullOrEmpty() && !payload.isNullOrEmpty()) {
                return PAYLOAD_WITHOUT_ACTION
            }

            val deepLink = deeplinkGeneratorService.generateDeepLink(domain, action, payload)
            val response = JsonObject()
            response.addProperty(DEEPLINK_RESPONSE, deepLink)
            return response
        } catch (e: IllegalArgumentException) {
            logger.safeDebug(e) { "Problem with arguments" }
            return incorrect("arguments", e.message ?: "")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GenerateDeeplink::class.java)
    }
}
