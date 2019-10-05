package brs.http

import brs.Account
import brs.common.QuickMocker
import brs.common.TestConstants
import brs.http.common.ResultFields.PUBLIC_KEY_RESPONSE
import brs.services.ParameterService
import brs.util.JSON
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import javax.servlet.http.HttpServletRequest

class GetAccountPublicKeyTest {

    private lateinit var t: GetAccountPublicKey

    private lateinit var mockParameterService: ParameterService

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()

        t = GetAccountPublicKey(mockParameterService!!)
    }

    @Test
    fun processRequest() = runBlocking {
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        whenever(mockAccount.publicKey).doReturn(TestConstants.TEST_PUBLIC_KEY_BYTES)

        whenever(mockParameterService!!.getAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)

        val result = t!!.processRequest(request) as JsonObject
        assertNotNull(result)

        assertEquals(TestConstants.TEST_PUBLIC_KEY, JSON.getAsString(result.get(PUBLIC_KEY_RESPONSE)))
    }

    @Test
    fun processRequest_withoutPublicKey() = runBlocking {
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        whenever(mockAccount.publicKey).doReturn(null)

        whenever(mockParameterService!!.getAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)

        assertEquals(JSON.emptyJSON, t!!.processRequest(request))
    }

}
