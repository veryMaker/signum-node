package brs.http

import brs.TransactionProcessor
import brs.common.QuickMocker
import brs.util.JSON
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.common.ResultFields.DONE_RESPONSE
import brs.http.common.ResultFields.ERROR_RESPONSE
import com.nhaarman.mockitokotlin2.doThrow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class ClearUnconfirmedTransactionsTest {

    private var t: ClearUnconfirmedTransactions? = null

    private var transactionProcessorMock: TransactionProcessor? = null

    @Before
    fun init() {
        transactionProcessorMock = mock<TransactionProcessor>()

        this.t = ClearUnconfirmedTransactions(transactionProcessorMock!!)
    }

    @Test
    fun processRequest() {
        val req = QuickMocker.httpServletRequest()

        val result = t!!.processRequest(req) as JsonObject

        assertEquals(true, JSON.getAsBoolean(result.get(DONE_RESPONSE)))
    }

    @Test
    fun processRequest_runtimeExceptionOccurs() {
        val req = QuickMocker.httpServletRequest()

        doThrow(RuntimeException("errorMessage")).whenever(transactionProcessorMock!!).clearUnconfirmedTransactions()

        val result = t!!.processRequest(req) as JsonObject

        assertEquals("java.lang.RuntimeException: errorMessage", JSON.getAsString(result.get(ERROR_RESPONSE)))
    }

    @Test
    fun requirePost() {
        assertTrue(t!!.requirePost())
    }
}