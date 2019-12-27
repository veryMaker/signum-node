package brs.api.http

import brs.api.http.common.Parameters
import brs.common.QuickMocker
import brs.api.http.common.ResultFields.DEEPLINK_RESPONSE
import brs.api.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.api.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE
import brs.services.DeeplinkGeneratorService
import brs.services.impl.DeeplinkGeneratorServiceImpl
import brs.util.json.safeGetAsInt
import brs.util.json.safeGetAsString
import com.google.gson.JsonObject
import io.mockk.mockk
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

        assertEquals("burst.payment://v1?action=testAction&payload=testPayload", result.get(DEEPLINK_RESPONSE).safeGetAsString())
    }

    @Test
    fun processRequest_noDomain() {
        val request = QuickMocker.httpServletRequest()

        val result = t.processRequest(request) as JsonObject

        assertEquals(3, result.get(ERROR_CODE_RESPONSE).safeGetAsInt())
        assertEquals("\"domain\" not specified", result.get(ERROR_DESCRIPTION_RESPONSE).safeGetAsString())
    }

    @Test
    fun processRequest_invalidDomain() {
        val request = QuickMocker.httpServletRequest(
            QuickMocker.MockParam(Parameters.DOMAIN_PARAMETER, "invalid"),
            QuickMocker.MockParam(Parameters.ACTION_PARAMETER, "testAction"),
            QuickMocker.MockParam(Parameters.PAYLOAD_PARAMETER, "testPayload")
        )

        val result = t.processRequest(request) as JsonObject

        assertEquals(4, result.get(ERROR_CODE_RESPONSE).safeGetAsInt())
        assertEquals("Incorrect \"arguments\"Invalid domain:invalid", result.get(ERROR_DESCRIPTION_RESPONSE).safeGetAsString())
    }

    @Test
    fun processRequest_payloadWithoutAction() {
        val request = QuickMocker.httpServletRequest(
            QuickMocker.MockParam(Parameters.DOMAIN_PARAMETER, "invalid"),
            QuickMocker.MockParam(Parameters.PAYLOAD_PARAMETER, "testPayload")
        )

        val result = t.processRequest(request) as JsonObject

        assertEquals(4, result.get(ERROR_CODE_RESPONSE).safeGetAsInt())
        assertEquals("Incorrect \"payload\"With 'payload' parameter the 'action' parameter is mandatory", result.get(ERROR_DESCRIPTION_RESPONSE).safeGetAsString())
    }

}
