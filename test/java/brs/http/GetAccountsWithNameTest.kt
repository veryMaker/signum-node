package brs.http

import brs.Account
import brs.BurstException
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.services.AccountService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.ACCOUNTS_RESPONSE
import brs.http.common.Parameters.NAME_PARAMETER
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAccountsWithNameTest : AbstractUnitTest() {

    private var accountService: AccountService? = null

    private var t: GetAccountsWithName? = null

    @Before
    fun setUp() {
        accountService = mock<AccountService>()

        t = GetAccountsWithName(accountService!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val targetAccountId = 4L
        val targetAccountName = "exampleAccountName"

        val req = QuickMocker.httpServletRequest(
                QuickMocker.MockParam(NAME_PARAMETER, targetAccountName)
        )

        val targetAccount = mock<Account>()
        whenever(targetAccount.getId()).thenReturn(targetAccountId)
        whenever(targetAccount.name).thenReturn(targetAccountName)

        val mockIterator = mockCollection<Account>(targetAccount)

        whenever(accountService!!.getAccountsWithName(targetAccountName)).thenReturn(mockIterator)

        val resultOverview = t!!.processRequest(req) as JsonObject
        assertNotNull(resultOverview)

        val resultList = resultOverview.get(ACCOUNTS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size().toLong())
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_noAccountFound() {
        val targetAccountName = "exampleAccountName"

        val req = QuickMocker.httpServletRequest(
                QuickMocker.MockParam(NAME_PARAMETER, targetAccountName)
        )

        val mockIterator = mockCollection<Account>()

        whenever(accountService!!.getAccountsWithName(targetAccountName)).thenReturn(mockIterator)

        val resultOverview = t!!.processRequest(req) as JsonObject
        assertNotNull(resultOverview)

        val resultList = resultOverview.get(ACCOUNTS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(0, resultList.size().toLong())
    }
}
