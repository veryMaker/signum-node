package brs.http

import brs.TransactionProcessor
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

class ClearUnconfirmedTransactionsTest {

    private lateinit var t: ClearUnconfirmedTransactions

    private lateinit var transactionProcessorMock: TransactionProcessor

    @Before
    fun init() {
        transactionProcessorMock = mock<TransactionProcessor>()

        this.t = ClearUnconfirmedTransactions(transactionProcessorMock!!)
    }

    @Test
    fun processRequest() = runBlocking {
        val request = QuickMocker.httpServletRequest()

        val result = t!!.processRequest(request) as JsonObject

        assertEquals(true, JSON.getAsBoolean(result.get(DONE_RESPONSE)))
    }

    @Test
    fun processRequest_runtimeExceptionOccurs() = runBlocking {
        val request = QuickMocker.httpServletRequest()

        doThrow(RuntimeException("errorMessage")).whenever(transactionProcessorMock!!).clearUnconfirmedTransactions()

        val result = t!!.processRequest(request) as JsonObject

        assertEquals("java.lang.RuntimeException: errorMessage", JSON.getAsString(result.get(ERROR_RESPONSE)))
    }

    @Test
    fun requirePost() {
        assertTrue(t!!.requirePost())
    }
}