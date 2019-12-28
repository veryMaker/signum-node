package brs.api.http

import brs.entity.Account
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.api.http.common.Parameters.ACCOUNTS_RESPONSE
import brs.api.http.common.Parameters.NAME_PARAMETER
import brs.services.AccountService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAccountsWithNameTest : AbstractUnitTest() {

    private lateinit var accountService: AccountService

    private lateinit var t: GetAccountsWithName

    @Before
    fun setUp() {
        accountService = mockk(relaxed = true)

        t = GetAccountsWithName(accountService)
    }

    @Test
    fun processRequest() {
        val targetAccountId = 4L
        val targetAccountName = "exampleAccountName"

        val request = QuickMocker.httpServletRequest(
                QuickMocker.MockParam(NAME_PARAMETER, targetAccountName)
        )

        val targetAccount = mockk<Account>(relaxed = true)
        every { targetAccount.id } returns targetAccountId
        every { targetAccount.name } returns targetAccountName

        val mockIterator = mockCollection(targetAccount)

        every { accountService.getAccountsWithName(targetAccountName) } returns mockIterator

        val resultOverview = t.processRequest(request) as JsonObject
        assertNotNull(resultOverview)

        val resultList = resultOverview.get(ACCOUNTS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size().toLong())
    }

    @Test
    fun processRequest_noAccountFound() {
        val targetAccountName = "exampleAccountName"

        val request = QuickMocker.httpServletRequest(
                QuickMocker.MockParam(NAME_PARAMETER, targetAccountName)
        )

        val mockIterator = mockCollection<Account>()

        every { accountService.getAccountsWithName(targetAccountName) } returns mockIterator

        val resultOverview = t.processRequest(request) as JsonObject
        assertNotNull(resultOverview)

        val resultList = resultOverview.get(ACCOUNTS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(0, resultList.size().toLong())
    }
}
