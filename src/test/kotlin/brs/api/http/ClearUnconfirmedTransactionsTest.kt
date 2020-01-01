package brs.api.http

import brs.api.http.common.ResultFields.DONE_RESPONSE
import brs.api.http.common.ResultFields.ERROR_RESPONSE
import brs.common.QuickMocker
import brs.services.TransactionProcessorService
import brs.util.json.getMemberAsBoolean
import brs.util.json.getMemberAsString
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ClearUnconfirmedTransactionsTest {
    private lateinit var t: ClearUnconfirmedTransactions

    private lateinit var transactionProcessorServiceMock: TransactionProcessorService

    @Before
    fun init() {
        transactionProcessorServiceMock = mockk(relaxed = true)

        this.t = ClearUnconfirmedTransactions(transactionProcessorServiceMock)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val result = t.processRequest(request) as JsonObject

        assertEquals(true, result.getMemberAsBoolean(DONE_RESPONSE))
    }

    @Test
    fun processRequest_runtimeExceptionOccurs() {
        val request = QuickMocker.httpServletRequest()

        every { transactionProcessorServiceMock.clearUnconfirmedTransactions() } throws RuntimeException("errorMessage")

        val result = t.processRequest(request) as JsonObject

        assertEquals("java.lang.RuntimeException: errorMessage", result.getMemberAsString(ERROR_RESPONSE))
    }

    @Test
    fun requirePost() {
        assertTrue(t.requirePost())
    }
}