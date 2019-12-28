package brs.api.http

import brs.api.http.common.ResultFields.BALANCE_PLANCK_RESPONSE
import brs.api.http.common.ResultFields.EFFECTIVE_BALANCE_PLANCK_RESPONSE
import brs.api.http.common.ResultFields.FORGED_BALANCE_PLANCK_RESPONSE
import brs.api.http.common.ResultFields.GUARANTEED_BALANCE_PLANCK_RESPONSE
import brs.api.http.common.ResultFields.UNCONFIRMED_BALANCE_PLANCK_RESPONSE
import brs.common.QuickMocker
import brs.entity.Account
import brs.services.ParameterService
import brs.util.json.safeGetAsString
import com.google.gson.JsonObject
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetBalanceTest {

    private lateinit var t: GetBalance

    private lateinit var parameterServiceMock: ParameterService

    @Before
    fun setUp() {
        parameterServiceMock = mockk(relaxed = true)
        this.t = GetBalance(parameterServiceMock)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()
        val mockAccount = mockk<Account>(relaxed = true)

        every { mockAccount.balancePlanck } returns 1L
        every { mockAccount.unconfirmedBalancePlanck } returns 2L
        every { mockAccount.forgedBalancePlanck } returns 3L

        every { parameterServiceMock.getAccount(eq(request)) } returns mockAccount

        val result = t.processRequest(request) as JsonObject

        assertEquals("1", result.get(BALANCE_PLANCK_RESPONSE).safeGetAsString())
        assertEquals("2", result.get(UNCONFIRMED_BALANCE_PLANCK_RESPONSE).safeGetAsString())
        assertEquals("1", result.get(EFFECTIVE_BALANCE_PLANCK_RESPONSE).safeGetAsString())
        assertEquals("3", result.get(FORGED_BALANCE_PLANCK_RESPONSE).safeGetAsString())
        assertEquals("1", result.get(GUARANTEED_BALANCE_PLANCK_RESPONSE).safeGetAsString())
    }

    @Test
    fun processRequest_noAccountFound() {
        val request = QuickMocker.httpServletRequest()

        every { parameterServiceMock.getAccount(eq(request)) } returns mockk(relaxed = true)

        val result = t.processRequest(request) as JsonObject

        assertEquals("0", result.get(BALANCE_PLANCK_RESPONSE).safeGetAsString())
        assertEquals("0", result.get(UNCONFIRMED_BALANCE_PLANCK_RESPONSE).safeGetAsString())
        assertEquals("0", result.get(EFFECTIVE_BALANCE_PLANCK_RESPONSE).safeGetAsString())
        assertEquals("0", result.get(FORGED_BALANCE_PLANCK_RESPONSE).safeGetAsString())
        assertEquals("0", result.get(GUARANTEED_BALANCE_PLANCK_RESPONSE).safeGetAsString())
    }
}
