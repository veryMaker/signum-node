package brs.api.http

import brs.api.http.common.ResultFields.PUBLIC_KEY_RESPONSE
import brs.common.QuickMocker
import brs.common.TestConstants
import brs.entity.Account
import brs.services.ParameterService
import brs.util.json.safeGetAsString
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAccountPublicKeyTest {

    private lateinit var t: GetAccountPublicKey

    private lateinit var mockParameterService: ParameterService

    @Before
    fun setUp() {
        mockParameterService = mock()

        t = GetAccountPublicKey(mockParameterService)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        whenever(mockAccount.publicKey).doReturn(TestConstants.TEST_PUBLIC_KEY_BYTES)

        whenever(mockParameterService.getAccount(eq(request))).doReturn(mockAccount)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        assertEquals(TestConstants.TEST_PUBLIC_KEY, result.get(PUBLIC_KEY_RESPONSE).safeGetAsString())
    }

    @Test
    fun processRequest_withoutPublicKey() {
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        whenever(mockAccount.publicKey).doReturn(null)

        whenever(mockParameterService.getAccount(eq(request))).doReturn(mockAccount)

        assertEquals(
            "Account does not have public key set in Blockchain",
            t.processRequest(request).asJsonObject["errorDescription"].asString
        )
    }

}
