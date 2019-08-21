package brs.http

import brs.Account
import brs.BurstException
import brs.common.QuickMocker
import brs.services.ParameterService
import brs.util.JSON
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.common.ResultFields.*
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetBalanceTest {

    private var t: GetBalance? = null

    private var parameterServiceMock: ParameterService? = null

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        this.t = GetBalance(parameterServiceMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val req = QuickMocker.httpServletRequest()
        val mockAccount = mock<Account>()

        whenever(mockAccount.balanceNQT).doReturn(1L)
        whenever(mockAccount.unconfirmedBalanceNQT).doReturn(2L)
        whenever(mockAccount.forgedBalanceNQT).doReturn(3L)

        whenever(parameterServiceMock!!.getAccount(eq<HttpServletRequest>(req))).doReturn(mockAccount)

        val result = t!!.processRequest(req) as JsonObject

        assertEquals("1", JSON.getAsString(result.get(BALANCE_NQT_RESPONSE)))
        assertEquals("2", JSON.getAsString(result.get(UNCONFIRMED_BALANCE_NQT_RESPONSE)))
        assertEquals("1", JSON.getAsString(result.get(EFFECTIVE_BALANCE_NQT_RESPONSE)))
        assertEquals("3", JSON.getAsString(result.get(FORGED_BALANCE_NQT_RESPONSE)))
        assertEquals("1", JSON.getAsString(result.get(GUARANTEED_BALANCE_NQT_RESPONSE)))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_noAccountFound() {
        val req = QuickMocker.httpServletRequest()

        whenever(parameterServiceMock!!.getAccount(eq<HttpServletRequest>(req))).doReturn(null)

        val result = t!!.processRequest(req) as JsonObject

        assertEquals("0", JSON.getAsString(result.get(BALANCE_NQT_RESPONSE)))
        assertEquals("0", JSON.getAsString(result.get(UNCONFIRMED_BALANCE_NQT_RESPONSE)))
        assertEquals("0", JSON.getAsString(result.get(EFFECTIVE_BALANCE_NQT_RESPONSE)))
        assertEquals("0", JSON.getAsString(result.get(FORGED_BALANCE_NQT_RESPONSE)))
        assertEquals("0", JSON.getAsString(result.get(GUARANTEED_BALANCE_NQT_RESPONSE)))
    }
}
