package brs.http

import brs.Account
import brs.common.QuickMocker
import brs.http.common.ResultFields.BALANCE_NQT_RESPONSE
import brs.http.common.ResultFields.EFFECTIVE_BALANCE_NQT_RESPONSE
import brs.http.common.ResultFields.FORGED_BALANCE_NQT_RESPONSE
import brs.http.common.ResultFields.GUARANTEED_BALANCE_NQT_RESPONSE
import brs.http.common.ResultFields.UNCONFIRMED_BALANCE_NQT_RESPONSE
import brs.services.ParameterService
import brs.util.safeGetAsString
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetBalanceTest {

    private lateinit var t: GetBalance

    private lateinit var parameterServiceMock: ParameterService

    @Before
    fun setUp() {
        parameterServiceMock = mock()
        this.t = GetBalance(parameterServiceMock)
    }

    @Test
    fun processRequest() = runBlocking {
        val request = QuickMocker.httpServletRequest()
        val mockAccount = mock<Account>()

        whenever(mockAccount.balanceNQT).doReturn(1L)
        whenever(mockAccount.unconfirmedBalanceNQT).doReturn(2L)
        whenever(mockAccount.forgedBalanceNQT).doReturn(3L)

        whenever(parameterServiceMock.getAccount(eq(request))).doReturn(mockAccount)

        val result = t.processRequest(request) as JsonObject

        assertEquals("1", result.get(BALANCE_NQT_RESPONSE).safeGetAsString())
        assertEquals("2", result.get(UNCONFIRMED_BALANCE_NQT_RESPONSE).safeGetAsString())
        assertEquals("1", result.get(EFFECTIVE_BALANCE_NQT_RESPONSE).safeGetAsString())
        assertEquals("3", result.get(FORGED_BALANCE_NQT_RESPONSE).safeGetAsString())
        assertEquals("1", result.get(GUARANTEED_BALANCE_NQT_RESPONSE).safeGetAsString())
    }

    @Test
    fun processRequest_noAccountFound() = runBlocking {
        val request = QuickMocker.httpServletRequest()

        whenever(parameterServiceMock.getAccount(eq(request))).doReturn(null)

        val result = t.processRequest(request) as JsonObject

        assertEquals("0", result.get(BALANCE_NQT_RESPONSE).safeGetAsString())
        assertEquals("0", result.get(UNCONFIRMED_BALANCE_NQT_RESPONSE).safeGetAsString())
        assertEquals("0", result.get(EFFECTIVE_BALANCE_NQT_RESPONSE).safeGetAsString())
        assertEquals("0", result.get(FORGED_BALANCE_NQT_RESPONSE).safeGetAsString())
        assertEquals("0", result.get(GUARANTEED_BALANCE_NQT_RESPONSE).safeGetAsString())
    }
}
