package brs.peer

import brs.entity.Account
import brs.services.BlockchainService
import brs.entity.DependencyProvider
import brs.entity.Transaction
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.JSONParam
import brs.common.TestConstants
import brs.services.AccountService
import brs.transaction.type.digitalGoods.DigitalGoodsDelisting
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAccountRecentTransactionsTest : AbstractUnitTest() {

    private lateinit var t: GetAccountRecentTransactions

    private lateinit var mockAccountService: AccountService
    private lateinit var mockBlockchainService: BlockchainService
    private lateinit var dp: DependencyProvider

    @Before
    fun setUp() {
        mockAccountService = mock()
        mockBlockchainService = mock()
        dp = QuickMocker.dependencyProvider(mockAccountService, mockBlockchainService)

        t = GetAccountRecentTransactions(mockAccountService, mockBlockchainService)
    }

    @Test
    fun processRequest() {
        val accountId = TestConstants.TEST_ACCOUNT_NUMERIC_ID

        val request = QuickMocker.jsonObject(JSONParam("account", JsonPrimitive(accountId)))

        val peerMock = mock<Peer>()

        val mockAccount = mock<Account>()

        val mockTransaction = mock<Transaction>()
        whenever(mockTransaction.type).doReturn(DigitalGoodsDelisting(dp))
        val transactionsIterator = mockCollection(mockTransaction)

        whenever(mockAccountService.getAccount(eq(TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED))).doReturn(mockAccount)
        whenever(mockBlockchainService.getTransactions(eq(mockAccount), eq(0), eq((-1).toByte()), eq(0.toByte()), eq(0), eq(0), eq(9), eq(false))).doReturn(transactionsIterator)

        val result = t.processRequest(request, peerMock) as JsonObject
        assertNotNull(result)

        val transactionsResult = result.get("transactions") as JsonArray
        assertNotNull(transactionsResult)
        assertEquals(1, transactionsResult.size().toLong())
    }

}
