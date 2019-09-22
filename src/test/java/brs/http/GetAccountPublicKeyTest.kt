package brs.http

import brs.Account
import brs.BurstException
import brs.common.QuickMocker
import brs.common.TestConstants
import brs.services.ParameterService
import brs.util.JSON
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.common.ResultFields.PUBLIC_KEY_RESPONSE
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAccountPublicKeyTest {

    private var t: GetAccountPublicKey? = null

    private var mockParameterService: ParameterService? = null

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()

        t = GetAccountPublicKey(mockParameterService!!)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        whenever(mockAccount.publicKey).doReturn(TestConstants.TEST_PUBLIC_KEY_BYTES)

        whenever(mockParameterService!!.getAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)

        val result = t!!.processRequest(request) as JsonObject
        assertNotNull(result)

        assertEquals(TestConstants.TEST_PUBLIC_KEY, JSON.getAsString(result.get(PUBLIC_KEY_RESPONSE)))
    }

    @Test
    fun processRequest_withoutPublicKey() {
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        whenever(mockAccount.publicKey).doReturn(null)

        whenever(mockParameterService!!.getAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)

        assertEquals(JSON.emptyJSON, t!!.processRequest(request))
    }

}
