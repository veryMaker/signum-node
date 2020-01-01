package brs.api.http

import brs.api.http.common.Parameters.TRANSACTION_BYTES_PARAMETER
import brs.api.http.common.Parameters.TRANSACTION_JSON_PARAMETER
import brs.api.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.api.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE
import brs.api.http.common.ResultFields.FULL_HASH_RESPONSE
import brs.api.http.common.ResultFields.TRANSACTION_RESPONSE
import brs.entity.Transaction
import brs.services.ParameterService
import brs.services.TransactionProcessorService
import brs.services.TransactionService
import brs.util.BurstException
import brs.util.convert.parseHexString
import brs.util.convert.toHexString
import brs.util.json.getMemberAsLong
import brs.util.json.getMemberAsString
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import javax.servlet.http.HttpServletRequest

class BroadcastTransactionTest {

    private lateinit var t: BroadcastTransaction

    private lateinit var transactionProcessorServiceMock: TransactionProcessorService
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var transactionServiceMock: TransactionService

    @Before
    fun setUp() {
        this.transactionProcessorServiceMock = mockk(relaxed = true)
        this.parameterServiceMock = mockk(relaxed = true)
        this.transactionServiceMock = mockk(relaxed = true)

        t = BroadcastTransaction(transactionProcessorServiceMock, parameterServiceMock, transactionServiceMock)
    }

    @Test
    fun processRequest() {
        val mockTransactionBytesParameter = "mockTransactionBytesParameter"
        val mockTransactionJson = "mockTransactionJson"

        val mockTransactionStringId = "mockTransactionStringId"
        val mockTransactionFullHash = "deadbeef".parseHexString()

        val request = mockk<HttpServletRequest>(relaxed = true)
        val mockTransaction = mockk<Transaction>(relaxed = true)

        every { mockTransaction.stringId } returns mockTransactionStringId
        every { mockTransaction.fullHash } returns mockTransactionFullHash

        every { request.getParameter(TRANSACTION_BYTES_PARAMETER) } returns mockTransactionBytesParameter
        every { request.getParameter(TRANSACTION_JSON_PARAMETER) } returns mockTransactionJson

        every { parameterServiceMock.parseTransaction(eq(mockTransactionBytesParameter), eq(mockTransactionJson)) } returns mockTransaction

        val result = t.processRequest(request) as JsonObject

        verify { transactionProcessorServiceMock.broadcast(eq(mockTransaction)) }

        assertEquals(mockTransactionStringId, result.getMemberAsString(TRANSACTION_RESPONSE))
        assertEquals(mockTransactionFullHash.toHexString(), result.getMemberAsString(FULL_HASH_RESPONSE))
    }

    @Test
    fun processRequest_validationException() {
        val mockTransactionBytesParameter = "mockTransactionBytesParameter"
        val mockTransactionJson = "mockTransactionJson"

        val request = mockk<HttpServletRequest>(relaxed = true)
        val mockTransaction = mockk<Transaction>(relaxed = true)

        every { request.getParameter(TRANSACTION_BYTES_PARAMETER) } returns mockTransactionBytesParameter
        every { request.getParameter(TRANSACTION_JSON_PARAMETER) } returns mockTransactionJson

        every { parameterServiceMock.parseTransaction(eq(mockTransactionBytesParameter), eq(mockTransactionJson)) } returns mockTransaction

        every { transactionServiceMock.validate(eq(mockTransaction), any()) } throws BurstException.NotCurrentlyValidException("")

        val result = t.processRequest(request) as JsonObject

        assertEquals(4L, result.getMemberAsLong(ERROR_CODE_RESPONSE))
        assertNotNull(result.get(ERROR_DESCRIPTION_RESPONSE))
    }

    @Test
    fun requirePost() {
        assertTrue(t.requirePost())
    }
}
