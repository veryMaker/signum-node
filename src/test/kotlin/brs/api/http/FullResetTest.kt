package brs.api.http

import brs.services.BlockchainProcessorService
import brs.common.QuickMocker
import brs.api.http.common.ResultFields.DONE_RESPONSE
import brs.api.http.common.ResultFields.ERROR_RESPONSE
import brs.util.mustGetAsBoolean
import brs.util.safeGetAsString
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FullResetTest {

    private lateinit var t: FullReset

    private lateinit var blockchainProcessorService: BlockchainProcessorService

    @Before
    fun init() {
        blockchainProcessorService = mock()

        this.t = FullReset(blockchainProcessorService)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val result = t.processRequest(request) as JsonObject

        assertTrue(result.get(DONE_RESPONSE).mustGetAsBoolean(DONE_RESPONSE))
    }

    @Test
    fun processRequest_runtimeExceptionOccurs() {
        val request = QuickMocker.httpServletRequest()

        doThrow(RuntimeException("errorMessage")).whenever(blockchainProcessorService).fullReset()

        val result = t.processRequest(request) as JsonObject

        assertEquals("java.lang.RuntimeException: errorMessage", result.get(ERROR_RESPONSE).safeGetAsString())
    }

    @Test
    fun requirePost() {
        assertTrue(t.requirePost())
    }
}