package brs.api.http

import brs.api.http.common.JSONResponses.MISSING_SIGNATURE_HASH
import brs.api.http.common.JSONResponses.MISSING_UNSIGNED_BYTES
import brs.api.http.common.Parameters.SIGNATURE_HASH_PARAMETER
import brs.api.http.common.Parameters.UNSIGNED_TRANSACTION_BYTES_PARAMETER
import brs.api.http.common.ResultFields.FULL_HASH_RESPONSE
import brs.util.json.mustGetAsJsonObject
import brs.util.json.safeGetAsString
import io.mockk.mockk
import io.mockk.every
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
    fun processRequest() {
        //TODO More sensible values here...
        val mockUnsignedTransactionBytes = "123"
        val mockSignatureHash = "123"
        val expectedFullHash = "fe09cbf95619345cde91e0dee049d55498085a152e19c1009cb8973f9e1b4518"

        val request = mockk<HttpServletRequest>(relaxed = true)

        every { request.getParameter(eq(UNSIGNED_TRANSACTION_BYTES_PARAMETER)) } returns mockUnsignedTransactionBytes
        every { request.getParameter(eq(SIGNATURE_HASH_PARAMETER)) } returns mockSignatureHash

        val result = t.processRequest(request).mustGetAsJsonObject("result")
        assertEquals(expectedFullHash, result.get(FULL_HASH_RESPONSE).safeGetAsString())
    }

    @Test
    fun processRequest_missingUnsignedBytes() {
        assertEquals(MISSING_UNSIGNED_BYTES, t.processRequest(mockk(relaxed = true)))
    }

    @Test
    fun processRequest_missingSignatureHash() {
        val mockUnsignedTransactionBytes = "mockUnsignedTransactionBytes"
        val request = mockk<HttpServletRequest>(relaxed = true)

        every { request.getParameter(eq(UNSIGNED_TRANSACTION_BYTES_PARAMETER)) } returns mockUnsignedTransactionBytes

        assertEquals(MISSING_SIGNATURE_HASH, t.processRequest(request))
    }
}
