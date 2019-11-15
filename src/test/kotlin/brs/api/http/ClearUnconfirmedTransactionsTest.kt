package brs.api.http

import brs.services.TransactionProcessorService
import brs.common.QuickMocker
import brs.api.http.common.ResultFields.DONE_RESPONSE
import brs.api.http.common.ResultFields.ERROR_RESPONSE
import brs.util.json.safeGetAsBoolean
import brs.util.json.safeGetAsString
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ClearUnconfirmedTransactionsTest {

    private lateinit var t: ClearUnconfirmedTransactions

    private lateinit var transactionProcessorServiceMock: TransactionProcessorService

    @Before
    fun init() {
        transactionProcessorServiceMock = mock()

        this.t = ClearUnconfirmedTransactions(transactionProcessorServiceMock)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val result = t.processRequest(request) as JsonObject

        assertEquals(true, result.get(DONE_RESPONSE).safeGetAsBoolean())
    }

    @Test
    fun processRequest_runtimeExceptionOccurs() {
        val request = QuickMocker.httpServletRequest()

        doThrow(RuntimeException("errorMessage")).whenever(transactionProcessorServiceMock).clearUnconfirmedTransactions()

        val result = t.processRequest(request) as JsonObject

        assertEquals("java.lang.RuntimeException: errorMessage", result.get(ERROR_RESPONSE).safeGetAsString())
    }

    @Test
    fun requirePost() {
        assertTrue(t.requirePost())
    }
}