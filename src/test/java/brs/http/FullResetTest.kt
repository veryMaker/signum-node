package brs.http

import brs.BlockchainProcessor
import brs.common.QuickMocker
import brs.http.common.ResultFields.DONE_RESPONSE
import brs.http.common.ResultFields.ERROR_RESPONSE
import brs.util.JSON
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FullResetTest {

    private lateinit var t: FullReset

    private lateinit var blockchainProcessor: BlockchainProcessor

    @Before
    fun init() {
        blockchainProcessor = mock()

        this.t = FullReset(blockchainProcessor!!)
    }

    @Test
    fun processRequest() = runBlocking {
        val request = QuickMocker.httpServletRequest()

        val result = t!!.processRequest(request) as JsonObject

        assertTrue(JSON.getAsBoolean(result.get(DONE_RESPONSE)))
    }

    @Test
    fun processRequest_runtimeExceptionOccurs() = runBlocking {
        val request = QuickMocker.httpServletRequest()

        doThrow(RuntimeException("errorMessage")).whenever(blockchainProcessor!!).fullReset()

        val result = t!!.processRequest(request) as JsonObject

        assertEquals("java.lang.RuntimeException: errorMessage", JSON.getAsString(result.get(ERROR_RESPONSE)))
    }

    @Test
    fun requirePost() {
        assertTrue(t!!.requirePost())
    }
}