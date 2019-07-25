package brs.http

import brs.BurstException
import brs.Transaction
import brs.TransactionProcessor
import brs.services.ParameterService
import brs.services.TransactionService
import brs.util.JSON
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.TRANSACTION_BYTES_PARAMETER
import brs.http.common.Parameters.TRANSACTION_JSON_PARAMETER
import brs.http.common.ResultFields.*
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.*

class BroadcastTransactionTest {

    private var t: BroadcastTransaction? = null

    private var transactionProcessorMock: TransactionProcessor? = null
    private var parameterServiceMock: ParameterService? = null
    private var transactionServiceMock: TransactionService? = null

    @Before
    fun setUp() {
        this.transactionProcessorMock = mock<TransactionProcessor>()
        this.parameterServiceMock = mock<ParameterService>()
        this.transactionServiceMock = mock<TransactionService>()

        t = BroadcastTransaction(transactionProcessorMock!!, parameterServiceMock!!, transactionServiceMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val mockTransactionBytesParameter = "mockTransactionBytesParameter"
        val mockTransactionJson = "mockTransactionJson"

        val mockTransactionStringId = "mockTransactionStringId"
        val mockTransactionFullHash = "mockTransactionFullHash"

        val req = mock<HttpServletRequest>()
        val mockTransaction = mock<Transaction>()

        whenever(mockTransaction.stringId).thenReturn(mockTransactionStringId)
        whenever(mockTransaction.fullHash).thenReturn(mockTransactionFullHash)

        whenever(req.getParameter(TRANSACTION_BYTES_PARAMETER)).thenReturn(mockTransactionBytesParameter)
        whenever(req.getParameter(TRANSACTION_JSON_PARAMETER)).thenReturn(mockTransactionJson)

        whenever(parameterServiceMock!!.parseTransaction(eq(mockTransactionBytesParameter), eq(mockTransactionJson))).thenReturn(mockTransaction)

        val result = t!!.processRequest(req) as JsonObject

        verify(transactionProcessorMock!!).broadcast(eq(mockTransaction))

        assertEquals(mockTransactionStringId, JSON.getAsString(result.get(TRANSACTION_RESPONSE)))
        assertEquals(mockTransactionFullHash, JSON.getAsString(result.get(FULL_HASH_RESPONSE)))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_validationException() {
        val mockTransactionBytesParameter = "mockTransactionBytesParameter"
        val mockTransactionJson = "mockTransactionJson"

        val req = mock<HttpServletRequest>()
        val mockTransaction = mock<Transaction>()

        whenever(req.getParameter(TRANSACTION_BYTES_PARAMETER)).thenReturn(mockTransactionBytesParameter)
        whenever(req.getParameter(TRANSACTION_JSON_PARAMETER)).thenReturn(mockTransactionJson)

        whenever(parameterServiceMock!!.parseTransaction(eq(mockTransactionBytesParameter), eq(mockTransactionJson))).thenReturn(mockTransaction)

        doThrow(BurstException.NotCurrentlyValidException::class).whenever(transactionServiceMock!!).validate(eq(mockTransaction))

        val result = t!!.processRequest(req) as JsonObject

        assertEquals(4, JSON.getAsInt(result.get(ERROR_CODE_RESPONSE)).toLong())
        assertNotNull(result.get(ERROR_DESCRIPTION_RESPONSE))
    }

    @Test
    fun requirePost() {
        assertTrue(t!!.requirePost())
    }
}
