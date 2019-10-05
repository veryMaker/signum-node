package brs.assetexchange

import brs.Order.Ask
import brs.Order.Bid
import brs.common.QuickMocker
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.VersionedEntityTable
import brs.db.store.OrderStore
import brs.services.AccountService
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class OrderServiceImplTest {

    private lateinit var t: OrderServiceImpl

    private lateinit var orderStoreMock: OrderStore
    private lateinit var mockAskOrderTable: VersionedEntityTable<Ask>
    private lateinit var mockAskOrderDbKeyFactory: LongKeyFactory<Ask>
    private lateinit var mockBidOrderTable: VersionedEntityTable<Bid>
    private lateinit var mockBidOrderDbKeyFactory: LongKeyFactory<Bid>

    private lateinit var accountServiceMock: AccountService
    private lateinit var tradeServiceMock: TradeServiceImpl

    @Before
    fun setUp() {
        orderStoreMock = mock<OrderStore>()
        mockAskOrderTable = mock()
        mockAskOrderDbKeyFactory = mock()
        mockBidOrderTable = mock()
        mockBidOrderDbKeyFactory = mock()

        accountServiceMock = mock<AccountService>()
        tradeServiceMock = mock<TradeServiceImpl>()

        whenever(orderStoreMock!!.askOrderTable).doReturn(mockAskOrderTable!!)
        whenever(orderStoreMock!!.askOrderDbKeyFactory).doReturn(mockAskOrderDbKeyFactory!!)
        whenever(orderStoreMock!!.bidOrderTable).doReturn(mockBidOrderTable!!)
        whenever(orderStoreMock!!.bidOrderDbKeyFactory).doReturn(mockBidOrderDbKeyFactory!!)

        t = OrderServiceImpl(QuickMocker.dependencyProvider(orderStoreMock!!, accountServiceMock!!), tradeServiceMock!!)
    }

    @Test
    fun getAskOrder() {
        val mockAskKey = mock<BurstKey>()
        val mockAsk = mock<Ask>()

        val askKey = 123L

        whenever(mockAskOrderDbKeyFactory!!.newKey(eq(askKey))).doReturn(mockAskKey)
        whenever(mockAskOrderTable!!.get(eq(mockAskKey))).doReturn(mockAsk)

        assertEquals(mockAsk, t!!.getAskOrder(askKey))
    }

    @Test
    fun getBidOrder() {
        val mockBidKey = mock<BurstKey>()
        val mockBid = mock<Bid>()

        val bidKey = 123L

        whenever(mockBidOrderDbKeyFactory!!.newKey(eq(bidKey))).doReturn(mockBidKey)
        whenever(mockBidOrderTable!!.get(eq(mockBidKey))).doReturn(mockBid)

        assertEquals(mockBid, t!!.getBidOrder(bidKey))
    }

    @Test
    fun getAllAskOrders() {
        val from = 1
        val to = 5

        val mockAskIterator = mock<Collection<Ask>>()

        whenever(mockAskOrderTable!!.getAll(eq(from), eq(to))).doReturn(mockAskIterator)

        assertEquals(mockAskIterator, t!!.getAllAskOrders(from, to))
    }

    @Test
    fun getAllBidOrders() {
        val from = 1
        val to = 5

        val mockBidIterator = mock<Collection<Bid>>()

        whenever(mockBidOrderTable!!.getAll(eq(from), eq(to))).doReturn(mockBidIterator)

        assertEquals(mockBidIterator, t!!.getAllBidOrders(from, to))
    }

    @Test
    fun getSortedBidOrders() {
        val assetId = 123L
        val from = 1
        val to = 5

        val mockBidIterator = mock<Collection<Bid>>()

        whenever(orderStoreMock!!.getSortedBids(eq(assetId), eq(from), eq(to))).doReturn(mockBidIterator)

        assertEquals(mockBidIterator, t!!.getSortedBidOrders(assetId, from, to))
    }

    @Test
    fun getAskOrdersByAccount() {
        val accountId = 123L
        val from = 1
        val to = 5

        val mockAskIterator = mock<Collection<Ask>>()

        whenever(orderStoreMock!!.getAskOrdersByAccount(eq(accountId), eq(from), eq(to))).doReturn(mockAskIterator)

        assertEquals(mockAskIterator, t!!.getAskOrdersByAccount(accountId, from, to))
    }

    @Test
    fun getAskOrdersByAccountAsset() {
        val accountId = 123L
        val assetId = 456L
        val from = 1
        val to = 5

        val mockAskIterator = mock<Collection<Ask>>()

        whenever(orderStoreMock!!.getAskOrdersByAccountAsset(eq(accountId), eq(assetId), eq(from), eq(to))).doReturn(mockAskIterator)

        assertEquals(mockAskIterator, t!!.getAskOrdersByAccountAsset(accountId, assetId, from, to))
    }

    @Test
    fun getSortedAskOrders() {
        val assetId = 456L
        val from = 1
        val to = 5

        val mockAskIterator = mock<Collection<Ask>>()

        whenever(orderStoreMock!!.getSortedAsks(eq(assetId), eq(from), eq(to))).doReturn(mockAskIterator)

        assertEquals(mockAskIterator, t!!.getSortedAskOrders(assetId, from, to))
    }

    @Test
    fun getBidCount() {
        whenever(mockBidOrderTable!!.count).doReturn(5)

        assertEquals(5, t!!.bidCount.toLong())
    }

    @Test
    fun getAskCount() {
        whenever(mockAskOrderTable!!.count).doReturn(5)

        assertEquals(5, t!!.askCount.toLong())
    }

    @Test
    fun getBidOrdersByAccount() {
        val accountId = 456L
        val from = 1
        val to = 5

        val mockBidIterator = mock<Collection<Bid>>()

        whenever(orderStoreMock!!.getBidOrdersByAccount(eq(accountId), eq(from), eq(to))).doReturn(mockBidIterator)

        assertEquals(mockBidIterator, t!!.getBidOrdersByAccount(accountId, from, to))
    }

    @Test
    fun getBidOrdersByAccountAsset() {
        val accountId = 123L
        val assetId = 456L
        val from = 1
        val to = 5

        val mockBidIterator = mock<Collection<Bid>>()

        whenever(orderStoreMock!!.getBidOrdersByAccountAsset(eq(accountId), eq(assetId), eq(from), eq(to))).doReturn(mockBidIterator)

        assertEquals(mockBidIterator, t!!.getBidOrdersByAccountAsset(accountId, assetId, from, to))
    }
}
