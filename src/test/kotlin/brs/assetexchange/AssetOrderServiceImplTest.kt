package brs.assetexchange

import brs.common.QuickMocker
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.MutableEntityTable
import brs.db.OrderStore
import brs.entity.Order.Ask
import brs.entity.Order.Bid
import brs.services.AccountService
import brs.services.impl.AssetOrderServiceImpl
import brs.services.impl.AssetTradeServiceImpl
import io.mockk.every
import io.mockk.mockk
import org.jooq.SortField
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AssetOrderServiceImplTest {
    private lateinit var t: AssetOrderServiceImpl

    private lateinit var orderStoreMock: OrderStore
    private lateinit var mockAskOrderTable: MutableEntityTable<Ask>
    private lateinit var mockAskOrderDbKeyFactory: LongKeyFactory<Ask>
    private lateinit var mockBidOrderTable: MutableEntityTable<Bid>
    private lateinit var mockBidOrderDbKeyFactory: LongKeyFactory<Bid>

    private lateinit var accountServiceMock: AccountService
    private lateinit var tradeServiceMock: AssetTradeServiceImpl

    @Before
    fun setUp() {
        orderStoreMock = mockk(relaxed = true)
        mockAskOrderTable = mockk(relaxed = true)
        mockAskOrderDbKeyFactory = mockk(relaxed = true)
        mockBidOrderTable = mockk(relaxed = true)
        mockBidOrderDbKeyFactory = mockk(relaxed = true)

        accountServiceMock = mockk(relaxed = true)
        tradeServiceMock = mockk(relaxed = true)

        every { orderStoreMock.askOrderTable } returns mockAskOrderTable
        every { orderStoreMock.askOrderDbKeyFactory } returns mockAskOrderDbKeyFactory
        every { orderStoreMock.bidOrderTable } returns mockBidOrderTable
        every { orderStoreMock.bidOrderDbKeyFactory } returns mockBidOrderDbKeyFactory

        t = AssetOrderServiceImpl(
            QuickMocker.dependencyProvider(orderStoreMock, accountServiceMock),
            tradeServiceMock
        )
    }

    @Test
    fun getAskOrder() {
        val mockAskKey = mockk<BurstKey>(relaxed = true)
        val mockAsk = mockk<Ask>(relaxed = true)

        val askKey = 123L

        every { mockAskOrderDbKeyFactory.newKey(eq(askKey)) } returns mockAskKey
        every { mockAskOrderTable.get(eq(mockAskKey)) } returns mockAsk

        assertEquals(mockAsk, t.getAskOrder(askKey))
    }

    @Test
    fun getBidOrder() {
        val mockBidKey = mockk<BurstKey>(relaxed = true)
        val mockBid = mockk<Bid>(relaxed = true)

        val bidKey = 123L

        every { mockBidOrderDbKeyFactory.newKey(eq(bidKey)) } returns mockBidKey
        every { mockBidOrderTable.get(eq(mockBidKey)) } returns mockBid

        assertEquals(mockBid, t.getBidOrder(bidKey))
    }

    @Test
    fun getAllAskOrders() {
        val from = 1
        val to = 5

        val mockAskIterator = mockk<Collection<Ask>>()

        every { mockAskOrderTable.getAll(eq(from), eq(to), any<Collection<SortField<*>>>()) } returns mockAskIterator

        assertEquals(mockAskIterator, t.getAllAskOrders(from, to))
    }

    @Test
    fun getAllBidOrders() {
        val from = 1
        val to = 5

        val mockBidIterator = mockk<Collection<Bid>>()

        every { mockBidOrderTable.getAll(eq(from), eq(to), any<Collection<SortField<*>>>()) } returns mockBidIterator

        assertEquals(mockBidIterator, t.getAllBidOrders(from, to))
    }

    @Test
    fun getSortedBidOrders() {
        val assetId = 123L
        val from = 1
        val to = 5

        val mockBidIterator = mockk<Collection<Bid>>()

        every { orderStoreMock.getSortedBids(eq(assetId), eq(from), eq(to)) } returns mockBidIterator

        assertEquals(mockBidIterator, t.getSortedBidOrders(assetId, from, to))
    }

    @Test
    fun getAskOrdersByAccount() {
        val accountId = 123L
        val from = 1
        val to = 5

        val mockAskIterator = mockk<Collection<Ask>>()

        every { orderStoreMock.getAskOrdersByAccount(eq(accountId), eq(from), eq(to)) } returns mockAskIterator

        assertEquals(mockAskIterator, t.getAskOrdersByAccount(accountId, from, to))
    }

    @Test
    fun getAskOrdersByAccountAsset() {
        val accountId = 123L
        val assetId = 456L
        val from = 1
        val to = 5

        val mockAskIterator = mockk<Collection<Ask>>()

        every { orderStoreMock.getAskOrdersByAccountAsset(eq(accountId), eq(assetId), eq(from), eq(to)) } returns mockAskIterator

        assertEquals(mockAskIterator, t.getAskOrdersByAccountAsset(accountId, assetId, from, to))
    }

    @Test
    fun getSortedAskOrders() {
        val assetId = 456L
        val from = 1
        val to = 5

        val mockAskIterator = mockk<Collection<Ask>>()

        every { orderStoreMock.getSortedAsks(eq(assetId), eq(from), eq(to)) } returns mockAskIterator

        assertEquals(mockAskIterator, t.getSortedAskOrders(assetId, from, to))
    }

    @Test
    fun getBidCount() {
        every { mockBidOrderTable.count } returns 5

        assertEquals(5, t.bidCount.toLong())
    }

    @Test
    fun getAskCount() {
        every { mockAskOrderTable.count } returns 5

        assertEquals(5, t.askCount.toLong())
    }

    @Test
    fun getBidOrdersByAccount() {
        val accountId = 456L
        val from = 1
        val to = 5

        val mockBidIterator = mockk<Collection<Bid>>()

        every { orderStoreMock.getBidOrdersByAccount(eq(accountId), eq(from), eq(to)) } returns mockBidIterator

        assertEquals(mockBidIterator, t.getBidOrdersByAccount(accountId, from, to))
    }

    @Test
    fun getBidOrdersByAccountAsset() {
        val accountId = 123L
        val assetId = 456L
        val from = 1
        val to = 5

        val mockBidIterator = mockk<Collection<Bid>>()

        every { orderStoreMock.getBidOrdersByAccountAsset(eq(accountId), eq(assetId), eq(from), eq(to)) } returns mockBidIterator

        assertEquals(mockBidIterator, t.getBidOrdersByAccountAsset(accountId, assetId, from, to))
    }
}
