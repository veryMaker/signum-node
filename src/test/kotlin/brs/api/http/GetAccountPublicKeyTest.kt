package brs.api.http

import brs.api.http.common.ResultFields.PUBLIC_KEY_RESPONSE
import brs.common.QuickMocker
import brs.common.TestConstants
import brs.entity.Account
import brs.services.ParameterService
import brs.util.json.getMemberAsString
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAccountPublicKeyTest {
    private lateinit var t: GetAccountPublicKey

    private lateinit var mockParameterService: ParameterService

    @Before
    fun setUp() {
        mockParameterService = mockk(relaxed = true)

        t = GetAccountPublicKey(mockParameterService)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mockk<Account>(relaxed = true)
        every { mockAccount.publicKey } returns TestConstants.TEST_PUBLIC_KEY_BYTES

        every { mockParameterService.getAccount(eq(request)) } returns mockAccount

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        assertEquals(TestConstants.TEST_PUBLIC_KEY, result.getMemberAsString(PUBLIC_KEY_RESPONSE))
    }

    @Test
    fun processRequest_withoutPublicKey() {
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mockk<Account>(relaxed = true)
        every { mockAccount.publicKey } returns null

        every { mockParameterService.getAccount(eq(request)) } returns mockAccount

        assertEquals(
            "Account does not have public key set in Blockchain",
            t.processRequest(request).asJsonObject["errorDescription"].asString
        )
    }
}
