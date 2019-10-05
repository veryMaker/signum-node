package brs.http

import brs.Account
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.http.common.Parameters.ACCOUNTS_RESPONSE
import brs.http.common.Parameters.NAME_PARAMETER
import brs.services.AccountService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAccountsWithNameTest : AbstractUnitTest() {

    private lateinit var accountService: AccountService

    private lateinit var t: GetAccountsWithName

    @Before
    fun setUp() {
        accountService = mock()

        t = GetAccountsWithName(accountService)
    }

    @Test
    fun processRequest() = runBlocking {
        val targetAccountId = 4L
        val targetAccountName = "exampleAccountName"

        val request = QuickMocker.httpServletRequest(
                QuickMocker.MockParam(NAME_PARAMETER, targetAccountName)
        )

        val targetAccount = mock<Account>()
        whenever(targetAccount.id).doReturn(targetAccountId)
        whenever(targetAccount.name).doReturn(targetAccountName)

        val mockIterator = mockCollection(targetAccount)

        whenever(accountService.getAccountsWithName(targetAccountName)).doReturn(mockIterator)

        val resultOverview = t.processRequest(request) as JsonObject
        assertNotNull(resultOverview)

        val resultList = resultOverview.get(ACCOUNTS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size().toLong())
    }

    @Test
    fun processRequest_noAccountFound() = runBlocking {
        val targetAccountName = "exampleAccountName"

        val request = QuickMocker.httpServletRequest(
                QuickMocker.MockParam(NAME_PARAMETER, targetAccountName)
        )

        val mockIterator = mockCollection<Account>()

        whenever(accountService.getAccountsWithName(targetAccountName)).doReturn(mockIterator)

        val resultOverview = t.processRequest(request) as JsonObject
        assertNotNull(resultOverview)

        val resultList = resultOverview.get(ACCOUNTS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(0, resultList.size().toLong())
    }
}
