package brs.http

import brs.http.JSONResponses.MISSING_SIGNATURE_HASH
import brs.http.JSONResponses.MISSING_UNSIGNED_BYTES
import brs.http.common.Parameters.SIGNATURE_HASH_PARAMETER
import brs.http.common.Parameters.UNSIGNED_TRANSACTION_BYTES_PARAMETER
import brs.http.common.ResultFields.FULL_HASH_RESPONSE
import brs.util.mustGetAsJsonObject
import brs.util.safeGetAsString
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import javax.servlet.http.HttpServletRequest

class CalculateFullHashTest {

    private lateinit var t: CalculateFullHash

    @Before
    fun setUp() {
        t = CalculateFullHash()
    }

    @Test
    fun processRequest() = runBlocking {
        //TODO More sensible values here...
        val mockUnsignedTransactionBytes = "123"
        val mockSignatureHash = "123"
        val expectedFullHash = "fe09cbf95619345cde91e0dee049d55498085a152e19c1009cb8973f9e1b4518"

        val request = mock<HttpServletRequest>()

        whenever(request.getParameter(eq(UNSIGNED_TRANSACTION_BYTES_PARAMETER))).doReturn(mockUnsignedTransactionBytes)
        whenever(request.getParameter(eq(SIGNATURE_HASH_PARAMETER))).doReturn(mockSignatureHash)

        val result = t.processRequest(request).mustGetAsJsonObject("result")
        assertEquals(expectedFullHash, result.get(FULL_HASH_RESPONSE).safeGetAsString())
    }

    @Test
    fun processRequest_missingUnsignedBytes() = runBlocking {
        assertEquals(MISSING_UNSIGNED_BYTES, t.processRequest(mock()))
    }

    @Test
    fun processRequest_missingSignatureHash() = runBlocking {
        val mockUnsignedTransactionBytes = "mockUnsignedTransactionBytes"
        val request = mock<HttpServletRequest>()

        whenever(request.getParameter(eq(UNSIGNED_TRANSACTION_BYTES_PARAMETER))).doReturn(mockUnsignedTransactionBytes)

        assertEquals(MISSING_SIGNATURE_HASH, t.processRequest(request))
    }
}
