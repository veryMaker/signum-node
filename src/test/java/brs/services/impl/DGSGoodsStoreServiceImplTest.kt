package brs.services.impl

import brs.Blockchain
import brs.DigitalGoodsStore
import brs.DigitalGoodsStore.Goods
import brs.DigitalGoodsStore.Purchase
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.VersionedEntityTable
import brs.db.store.DigitalGoodsStoreStore
import brs.services.AccountService
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DGSGoodsStoreServiceImplTest : AbstractUnitTest() {

    private lateinit var blockchain: Blockchain

    private lateinit var mockAccountService: AccountService
    private lateinit var mockDigitalGoodsStoreStore: DigitalGoodsStoreStore

    private lateinit var mockGoodsTable: VersionedEntityTable<Goods>
    private lateinit var mockPurchaseTable: VersionedEntityTable<Purchase>
    private lateinit var mockGoodsDbKeyFactory: LongKeyFactory<Goods>

    private lateinit var t: DGSGoodsStoreServiceImpl

    @Before
    fun setUp() {
        blockchain = mock()
        mockGoodsTable = mock()
        mockPurchaseTable = mock()
        mockDigitalGoodsStoreStore = mock()
        mockGoodsDbKeyFactory = mock()
        mockAccountService = mock()

        whenever(mockDigitalGoodsStoreStore.goodsTable).doReturn(mockGoodsTable)
        whenever(mockDigitalGoodsStoreStore.purchaseTable).doReturn(mockPurchaseTable)
        whenever(mockDigitalGoodsStoreStore.goodsDbKeyFactory).doReturn(mockGoodsDbKeyFactory)

        t = DGSGoodsStoreServiceImpl(QuickMocker.dependencyProvider(
            blockchain,
            mockDigitalGoodsStoreStore,
            mockAccountService
        ))
    }

    @Test
    fun getGoods() {
        val mockKey = mock<BurstKey>()
        val mockGoods = mock<Goods>()

        whenever(mockGoodsDbKeyFactory.newKey(eq(1L))).doReturn(mockKey)
        whenever(mockGoodsTable[eq(mockKey)]).doReturn(mockGoods)

        assertEquals(mockGoods, t.getGoods(1L))
    }

    @Test
    fun getAllGoods() {
        val from = 1
        val to = 2

        val mockIterator = mockCollection<Goods>()
        whenever(mockGoodsTable.getAll(eq(from), eq(to))).doReturn(mockIterator)

        assertEquals(mockIterator, t.getAllGoods(from, to))
    }

    @Test
    fun getGoodsInStock() {
        val from = 1
        val to = 2

        val mockIterator = mockCollection<Goods>()
        whenever(mockDigitalGoodsStoreStore.getGoodsInStock(eq(from), eq(to))).doReturn(mockIterator)

        assertEquals(mockIterator, t.getGoodsInStock(from, to))
    }

    @Test
    fun getSellerGoods() {
        val sellerId = 1L
        val inStockOnly = false
        val from = 1
        val to = 2

        val mockIterator = mockCollection<Goods>()
        whenever(mockDigitalGoodsStoreStore.getSellerGoods(eq(sellerId), eq(inStockOnly), eq(from), eq(to))).doReturn(mockIterator)

        assertEquals(mockIterator, t.getSellerGoods(sellerId, inStockOnly, from, to))
    }

    @Test
    fun getAllPurchases() {
        val from = 1
        val to = 2

        val mockIterator = mockCollection<Purchase>()
        whenever(mockPurchaseTable.getAll(eq(from), eq(to))).doReturn(mockIterator)

        assertEquals(mockIterator, t.getAllPurchases(from, to))
    }

    @Test
    fun getSellerPurchases() {
        val sellerId = 1L
        val from = 2
        val to = 3

        val mockIterator = mockCollection<Purchase>()
        whenever(mockDigitalGoodsStoreStore.getSellerPurchases(eq(sellerId), eq(from), eq(to))).doReturn(mockIterator)

        assertEquals(mockIterator, t.getSellerPurchases(sellerId, from, to))
    }

    @Test
    fun getBuyerPurchases() {
        val buyerId = 1L
        val from = 2
        val to = 3

        val mockIterator = mockCollection<Purchase>()
        whenever(mockDigitalGoodsStoreStore.getBuyerPurchases(eq(buyerId), eq(from), eq(to))).doReturn(mockIterator)

        assertEquals(mockIterator, t.getBuyerPurchases(buyerId, from, to))
    }

    @Test
    fun getSellerBuyerPurchases() {
        val sellerId = 1L
        val buyerId = 2L
        val from = 3
        val to = 4

        val mockIterator = mockCollection<Purchase>()
        whenever(mockDigitalGoodsStoreStore.getSellerBuyerPurchases(eq(sellerId), eq(buyerId), eq(from), eq(to))).doReturn(mockIterator)

        assertEquals(mockIterator, t.getSellerBuyerPurchases(sellerId, buyerId, from, to))
    }

    @Test
    fun getPendingSellerPurchases() {
        val sellerId = 123L
        val from = 1
        val to = 2

        val mockPurchaseIterator = mockCollection<Purchase>()
        whenever(mockDigitalGoodsStoreStore.getPendingSellerPurchases(eq(sellerId), eq(from), eq(to))).doReturn(mockPurchaseIterator)

        assertEquals(mockPurchaseIterator, t.getPendingSellerPurchases(sellerId, from, to))
    }

}
