package brs.api.http

import brs.api.http.common.ResultFields.DONE_RESPONSE
import brs.api.http.common.ResultFields.ERROR_RESPONSE
import brs.common.QuickMocker
import brs.services.BlockchainProcessorService
import brs.util.json.mustGetMemberAsBoolean
import brs.util.json.getMemberAsString
import com.google.gson.JsonObject
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FullResetTest {

    private lateinit var t: FullReset

    private lateinit var blockchainProcessorService: BlockchainProcessorService

    @Before
    fun init() {
        blockchainProcessorService = mockk(relaxed = true)

        this.t = FullReset(blockchainProcessorService)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val result = t.processRequest(request) as JsonObject

        assertTrue(result.mustGetMemberAsBoolean(DONE_RESPONSE))
    }

    @Test
    fun processRequest_runtimeExceptionOccurs() {
        val request = QuickMocker.httpServletRequest()

        every { blockchainProcessorService.fullReset() } throws RuntimeException("errorMessage")

        val result = t.processRequest(request) as JsonObject

        assertEquals("java.lang.RuntimeException: errorMessage", result.getMemberAsString(ERROR_RESPONSE))
    }

    @Test
    fun requirePost() {
        assertTrue(t.requirePost())
    }
}