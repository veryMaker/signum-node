package brs.http

import brs.Account
import brs.Account.RewardRecipientAssignment
import brs.BurstException
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.services.AccountService
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.ACCOUNTS_RESPONSE
import brs.http.common.Parameters.ACCOUNT_PARAMETER
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAccountsWithRewardRecipientTest : AbstractUnitTest() {

    private var parameterService: ParameterService? = null
    private var accountService: AccountService? = null

    private var t: GetAccountsWithRewardRecipient? = null

    @Before
    fun setUp() {
        parameterService = mock<ParameterService>()
        accountService = mock<AccountService>()

        t = GetAccountsWithRewardRecipient(parameterService!!, accountService!!)
    }

    @Test
    fun processRequest() {
        val targetAccountId = 4L

        val request = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, targetAccountId)
        )

        val targetAccount = mock<Account>()
        whenever(targetAccount.id).doReturn(targetAccountId)

        whenever(parameterService!!.getAccount(eq<HttpServletRequest>(request))).doReturn(targetAccount)

        val assignment = mock<RewardRecipientAssignment>()
        whenever(assignment.getAccountId()).doReturn(targetAccountId)

        val assignmentIterator = mockCollection<RewardRecipientAssignment>(assignment)

        whenever(accountService!!.getAccountsWithRewardRecipient(eq(targetAccountId))).doReturn(assignmentIterator)

        val resultOverview = t!!.processRequest(request) as JsonObject
        assertNotNull(resultOverview)

        val resultList = resultOverview.get(ACCOUNTS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(2, resultList.size().toLong())
    }

    @Test
    fun processRequest_withRewardRecipientAssignmentKnown() {
        val targetAccountId = 4L

        val request = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, targetAccountId)
        )

        val targetAccount = mock<Account>()
        whenever(targetAccount.id).doReturn(targetAccountId)

        whenever(parameterService!!.getAccount(eq<HttpServletRequest>(request))).doReturn(targetAccount)

        val assignment = mock<RewardRecipientAssignment>()
        whenever(assignment.getAccountId()).doReturn(targetAccountId)

        val assignmentIterator = mockCollection<RewardRecipientAssignment>(assignment)

        whenever(accountService!!.getAccountsWithRewardRecipient(eq(targetAccountId))).doReturn(assignmentIterator)
        whenever(accountService!!.getRewardRecipientAssignment(eq(targetAccount))).doReturn(assignment)

        val resultOverview = t!!.processRequest(request) as JsonObject
        assertNotNull(resultOverview)

        val resultList = resultOverview.get(ACCOUNTS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size().toLong())
    }
}
