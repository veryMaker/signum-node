package brs.peer

import brs.Account
import brs.services.AccountService
import brs.util.JSON
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import brs.common.TestConstants.TEST_ACCOUNT_ID
import brs.common.TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED
import brs.peer.GetAccountBalance.ACCOUNT_ID_PARAMETER_FIELD
import brs.peer.GetAccountBalance.BALANCE_NQT_RESPONSE_FIELD
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals

@Deprecated("")
class GetAccountBalanceTest {

    private var t: GetAccountBalance? = null

    private var mockAccountService: AccountService? = null

    @Before
    fun setUp() {
        mockAccountService = mock<AccountService>()

        t = GetAccountBalance(mockAccountService)
    }

    @Test
    fun processRequest() {
        val req = JsonObject()
        req.addProperty(ACCOUNT_ID_PARAMETER_FIELD, TEST_ACCOUNT_ID)
        val peer = mock<Peer>()

        val mockBalanceNQT: Long = 5
        val mockAccount = mock<Account>()
        whenever(mockAccount.balanceNQT).doReturn(mockBalanceNQT)

        whenever(mockAccountService!!.getAccount(eq(TEST_ACCOUNT_NUMERIC_ID_PARSED))).doReturn(mockAccount)

        val result = t!!.processRequest(req, peer) as JsonObject

        assertEquals("" + mockBalanceNQT, JSON.getAsString(result.get(BALANCE_NQT_RESPONSE_FIELD)))
    }

    @Test
    fun processRequest_notExistingAccount() {
        val req = JsonObject()
        req.addProperty(ACCOUNT_ID_PARAMETER_FIELD, TEST_ACCOUNT_ID)
        val peer = mock<Peer>()

        whenever(mockAccountService!!.getAccount(eq(TEST_ACCOUNT_NUMERIC_ID_PARSED))).doReturn(null)

        val result = t!!.processRequest(req, peer) as JsonObject

        assertEquals("0", JSON.getAsString(result.get(BALANCE_NQT_RESPONSE_FIELD)))
    }

}
