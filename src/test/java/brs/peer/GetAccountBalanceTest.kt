package brs.peer

import brs.Account
import brs.common.TestConstants.TEST_ACCOUNT_ID
import brs.common.TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED
import brs.peer.GetAccountBalance.Companion.ACCOUNT_ID_PARAMETER_FIELD
import brs.peer.GetAccountBalance.Companion.BALANCE_NQT_RESPONSE_FIELD
import brs.services.AccountService
import brs.util.JSON
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@Deprecated("")
class GetAccountBalanceTest {

    private lateinit var t: GetAccountBalance

    private lateinit var mockAccountService: AccountService

    @Before
    fun setUp() {
        mockAccountService = mock()

        t = GetAccountBalance(mockAccountService)
    }

    @Test
    fun processRequest() = runBlocking {
        val request = JsonObject()
        request.addProperty(ACCOUNT_ID_PARAMETER_FIELD, TEST_ACCOUNT_ID)
        val peer = mock<Peer>()

        val mockBalanceNQT: Long = 5
        val mockAccount = mock<Account>()
        whenever(mockAccount.balanceNQT).doReturn(mockBalanceNQT)

        whenever(mockAccountService.getAccount(eq(TEST_ACCOUNT_NUMERIC_ID_PARSED))).doReturn(mockAccount)

        val result = t.processRequest(request, peer) as JsonObject

        assertEquals("" + mockBalanceNQT, JSON.getAsString(result.get(BALANCE_NQT_RESPONSE_FIELD)))
    }

    @Test
    fun processRequest_notExistingAccount() = runBlocking {
        val request = JsonObject()
        request.addProperty(ACCOUNT_ID_PARAMETER_FIELD, TEST_ACCOUNT_ID)
        val peer = mock<Peer>()

        whenever(mockAccountService.getAccount(eq(TEST_ACCOUNT_NUMERIC_ID_PARSED))).doReturn(null)

        val result = t.processRequest(request, peer) as JsonObject

        assertEquals("0", JSON.getAsString(result.get(BALANCE_NQT_RESPONSE_FIELD)))
    }

}
