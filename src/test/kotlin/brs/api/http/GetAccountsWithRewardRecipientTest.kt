package brs.api.http

import brs.api.http.common.Parameters.ACCOUNTS_RESPONSE
import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.entity.Account
import brs.entity.Account.RewardRecipientAssignment
import brs.services.AccountService
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAccountsWithRewardRecipientTest : AbstractUnitTest() {

    private lateinit var parameterService: ParameterService
    private lateinit var accountService: AccountService

    private lateinit var t: GetAccountsWithRewardRecipient

    @Before
    fun setUp() {
        parameterService = mockk()
        accountService = mockk()

        t = GetAccountsWithRewardRecipient(parameterService, accountService)
    }

    @Test
    fun processRequest() {
        val targetAccountId = 4L

        val request = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, targetAccountId)
        )

        val targetAccount = mockk<Account>()
        every { targetAccount.id } returns targetAccountId

        every { parameterService.getAccount(eq(request)) } returns targetAccount

        val assignment = mockk<RewardRecipientAssignment>()
        every { assignment.accountId } returns targetAccountId

        val assignmentIterator = mockCollection(assignment)

        every { accountService.getRewardRecipientAssignment(eq(targetAccount)) } returns null
        every { accountService.getAccountsWithRewardRecipient(eq(targetAccountId)) } returns assignmentIterator

        val resultOverview = t.processRequest(request) as JsonObject
        assertNotNull(resultOverview)

        val resultList = resultOverview.get(ACCOUNTS_RESPONSE) as JsonArray
        assertEquals(2, resultList.size().toLong())
    }

    @Test
    fun processRequest_withRewardRecipientAssignmentKnown() {
        val targetAccountId = 4L

        val request = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, targetAccountId)
        )

        val targetAccount = mockk<Account>()
        every { targetAccount.id } returns targetAccountId

        every { parameterService.getAccount(eq(request)) } returns targetAccount

        val assignment = mockk<RewardRecipientAssignment>()
        every { assignment.accountId } returns targetAccountId

        val assignmentIterator = mockCollection(assignment)

        every { accountService.getAccountsWithRewardRecipient(eq(targetAccountId)) } returns assignmentIterator
        every { accountService.getRewardRecipientAssignment(eq(targetAccount)) } returns assignment

        val resultOverview = t.processRequest(request) as JsonObject
        assertNotNull(resultOverview)

        val resultList = resultOverview.get(ACCOUNTS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size().toLong())
    }
}
