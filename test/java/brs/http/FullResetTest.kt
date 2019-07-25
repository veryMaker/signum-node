package brs.http

import brs.BlockchainProcessor
import brs.common.QuickMocker
import brs.util.JSON
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.common.ResultFields.DONE_RESPONSE
import brs.http.common.ResultFields.ERROR_RESPONSE
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class FullResetTest {

    private var t: FullReset? = null

    private var blockchainProcessor: BlockchainProcessor? = null

    @Before
    fun init() {
        blockchainProcessor = mock()

        this.t = FullReset(blockchainProcessor!!)
    }

    @Test
    fun processRequest() {
        val req = QuickMocker.httpServletRequest()

        val result = t!!.processRequest(req) as JsonObject

        assertTrue(JSON.getAsBoolean(result.get(DONE_RESPONSE)))
    }

    @Test
    fun processRequest_runtimeExceptionOccurs() {
        val req = QuickMocker.httpServletRequest()

        doThrow(RuntimeException("errorMessage")).whenever(blockchainProcessor!!).fullReset()

        val result = t!!.processRequest(req) as JsonObject

        assertEquals("java.lang.RuntimeException: errorMessage", JSON.getAsString(result.get(ERROR_RESPONSE)))
    }

    @Test
    fun requirePost() {
        assertTrue(t!!.requirePost())
    }
}