package brs.api.http

import brs.api.http.common.Parameters
import brs.api.http.common.ResultFields.DEEPLINK_RESPONSE
import brs.api.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.api.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE
import brs.common.QuickMocker
import brs.services.DeeplinkGeneratorService
import brs.services.impl.DeeplinkGeneratorServiceImpl
import brs.util.json.getMemberAsInt
import brs.util.json.getMemberAsString
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GenerateDeeplinkTest {
    private lateinit var t: GenerateDeeplink

    private lateinit var deeplinkService: DeeplinkGeneratorService

    @Before
    fun setUp() {
        deeplinkService = DeeplinkGeneratorServiceImpl()
        this.t = GenerateDeeplink(deeplinkService)
    }

    @Test
    fun processRequest_success() {
        val request = QuickMocker.httpServletRequest(
            QuickMocker.MockParam(Parameters.DOMAIN_PARAMETER, "payment"),
            QuickMocker.MockParam(Parameters.ACTION_PARAMETER, "testAction"),
            QuickMocker.MockParam(Parameters.PAYLOAD_PARAMETER, "testPayload")
        )

        val result = t.processRequest(request) as JsonObject

        assertEquals("burst.payment://v1?action=testAction&payload=testPayload", result.getMemberAsString(DEEPLINK_RESPONSE))
    }

    @Test
    fun processRequest_noDomain() {
        val request = QuickMocker.httpServletRequest()

        val result = t.processRequest(request) as JsonObject

        assertEquals(3, result.getMemberAsInt(ERROR_CODE_RESPONSE))
        assertEquals("\"domain\" not specified", result.getMemberAsString(ERROR_DESCRIPTION_RESPONSE))
    }

    @Test
    fun processRequest_invalidDomain() {
        val request = QuickMocker.httpServletRequest(
            QuickMocker.MockParam(Parameters.DOMAIN_PARAMETER, "invalid"),
            QuickMocker.MockParam(Parameters.ACTION_PARAMETER, "testAction"),
            QuickMocker.MockParam(Parameters.PAYLOAD_PARAMETER, "testPayload")
        )

        val result = t.processRequest(request) as JsonObject

        assertEquals(4, result.getMemberAsInt(ERROR_CODE_RESPONSE))
        assertEquals("Incorrect \"arguments\": Invalid domain: \"invalid\"", result.getMemberAsString(ERROR_DESCRIPTION_RESPONSE))
    }

    @Test
    fun processRequest_payloadWithoutAction() {
        val request = QuickMocker.httpServletRequest(
            QuickMocker.MockParam(Parameters.DOMAIN_PARAMETER, "invalid"),
            QuickMocker.MockParam(Parameters.PAYLOAD_PARAMETER, "testPayload")
        )

        val result = t.processRequest(request) as JsonObject

        assertEquals(4, result.getMemberAsInt(ERROR_CODE_RESPONSE))
        assertEquals("Incorrect \"payload\": With 'payload' parameter the 'action' parameter is mandatory", result.getMemberAsString(ERROR_DESCRIPTION_RESPONSE))
    }
}
