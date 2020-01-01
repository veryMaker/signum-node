package brs.api.http

import brs.api.http.common.ResultFields.ATS_RESPONSE
import brs.at.AT
import brs.at.AtMachineState
import brs.common.QuickMocker
import brs.common.TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED
import brs.entity.Account
import brs.services.ATService
import brs.services.AccountService
import brs.services.ParameterService
import com.google.gson.JsonArray
import brs.util.jetty.get
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAccountATsTest {
    private lateinit var t: GetAccountATs

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockATService: ATService
    private lateinit var mockAccountService: AccountService

    @Before
    fun setUp() {
        mockParameterService = mockk(relaxed = true)
        mockATService = mockk(relaxed = true)
        mockAccountService = mockk(relaxed = true)

        t = GetAccountATs(mockParameterService, mockATService, mockAccountService)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val mockAccountId = 123L
        val mockAccount = mockk<Account>(relaxed = true)
        every { mockAccount.id } returns mockAccountId

        val mockATId = 1L
        val mockATIDBytes = TEST_ACCOUNT_NUMERIC_ID_PARSED
        val creatorBytes = TEST_ACCOUNT_NUMERIC_ID_PARSED + 1
        val mockMachineState = mockk<AtMachineState.MachineState>(relaxed = true)
        val mockAT = mockk<AT>(relaxed = true)
        every { mockAT.creator } returns creatorBytes
        every { mockAT.id } returns mockATIDBytes
        every { mockAT.machineState } returns mockMachineState

        every { mockParameterService.getAccount(eq(request)) } returns mockAccount

        every { mockAccountService.getAccount(any<Long>()) } returns mockAccount

        every { mockATService.getATsIssuedBy(eq(mockAccountId)) } returns listOf(mockATId)
        every { mockATService.getAT(eq(mockATId)) } returns mockAT

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val atsResultList = result.get(ATS_RESPONSE) as JsonArray
        assertNotNull(atsResultList)
        assertEquals(1, atsResultList.size().toLong())

        val atsResult = atsResultList.get(0) as JsonObject
        assertNotNull(atsResult)
    }
}
