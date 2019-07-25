package brs.services.impl

import brs.Blockchain
import brs.DigitalGoodsStore
import brs.DigitalGoodsStore.Goods
import brs.DigitalGoodsStore.Purchase
import brs.common.AbstractUnitTest
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.VersionedEntityTable
import brs.db.store.DigitalGoodsStoreStore
import brs.services.AccountService
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test

import org.junit.Assert.assertEquals

class DGSGoodsStoreServiceImplTest : AbstractUnitTest() {

    private var blockchain: Blockchain? = null

    private var mockAccountService: AccountService? = null
    private var mockDigitalGoodsStoreStore: DigitalGoodsStoreStore? = null

    private var mockGoodsTable: VersionedEntityTable<DigitalGoodsStore.Goods>? = null
    private var mockPurchaseTable: VersionedEntityTable<DigitalGoodsStore.Purchase>? = null
    private var mockGoodsDbKeyFactory: LongKeyFactory<DigitalGoodsStore.Goods>? = null

    private var t: DGSGoodsStoreServiceImpl? = null

    @Before
    fun setUp() {
        blockchain = mock()
        mockGoodsTable = mock()
        mockPurchaseTable = mock()
        mockDigitalGoodsStoreStore = mock()
        mockGoodsDbKeyFactory = mock()
        mockAccountService = mock()

        whenever(mockDigitalGoodsStoreStore!!.goodsTable).thenReturn(mockGoodsTable)
        whenever(mockDigitalGoodsStoreStore!!.purchaseTable).thenReturn(mockPurchaseTable)
        whenever(mockDigitalGoodsStoreStore!!.goodsDbKeyFactory).thenReturn(mockGoodsDbKeyFactory)

        t = DGSGoodsStoreServiceImpl(blockchain, mockDigitalGoodsStoreStore!!, mockAccountService)
    }

    @Test
    fun getGoods() {
        val mockKey = mock<BurstKey>()
        val mockGoods = mock<Goods>()

        whenever(mockGoodsDbKeyFactory!!.newKey(eq(1L))).thenReturn(mockKey)
        whenever(mockGoodsTable!!.get(eq(mockKey))).thenReturn(mockGoods)

        assertEquals(mockGoods, t!!.getGoods(1L))
    }

    @Test
    fun getAllGoods() {
        val from = 1
        val to = 2

        val mockIterator = mockCollection<DigitalGoodsStore.Goods>()
        whenever(mockGoodsTable!!.getAll(eq(from), eq(to))).thenReturn(mockIterator)

        assertEquals(mockIterator, t!!.getAllGoods(from, to))
    }

    @Test
    fun getGoodsInStock() {
        val from = 1
        val to = 2

        val mockIterator = mockCollection<DigitalGoodsStore.Goods>()
        whenever(mockDigitalGoodsStoreStore!!.getGoodsInStock(eq(from), eq(to))).thenReturn(mockIterator)

        assertEquals(mockIterator, t!!.getGoodsInStock(from, to))
    }

    @Test
    fun getSellerGoods() {
        val sellerId = 1L
        val inStockOnly = false
        val from = 1
        val to = 2

        val mockIterator = mockCollection<DigitalGoodsStore.Goods>()
        whenever(mockDigitalGoodsStoreStore!!.getSellerGoods(eq(sellerId), eq(inStockOnly), eq(from), eq(to))).thenReturn(mockIterator)

        assertEquals(mockIterator, t!!.getSellerGoods(sellerId, inStockOnly, from, to))
    }

    @Test
    fun getAllPurchases() {
        val from = 1
        val to = 2

        val mockIterator = mockCollection<DigitalGoodsStore.Purchase>()
        whenever(mockPurchaseTable!!.getAll(eq(from), eq(to))).thenReturn(mockIterator)

        assertEquals(mockIterator, t!!.getAllPurchases(from, to))
    }

    @Test
    fun getSellerPurchases() {
        val sellerId = 1L
        val from = 2
        val to = 3

        val mockIterator = mockCollection<DigitalGoodsStore.Purchase>()
        whenever(mockDigitalGoodsStoreStore!!.getSellerPurchases(eq(sellerId), eq(from), eq(to))).thenReturn(mockIterator)

        assertEquals(mockIterator, t!!.getSellerPurchases(sellerId, from, to))
    }

    @Test
    fun getBuyerPurchases() {
        val buyerId = 1L
        val from = 2
        val to = 3

        val mockIterator = mockCollection<DigitalGoodsStore.Purchase>()
        whenever(mockDigitalGoodsStoreStore!!.getBuyerPurchases(eq(buyerId), eq(from), eq(to))).thenReturn(mockIterator)

        assertEquals(mockIterator, t!!.getBuyerPurchases(buyerId, from, to))
    }

    @Test
    fun getSellerBuyerPurchases() {
        val sellerId = 1L
        val buyerId = 2L
        val from = 3
        val to = 4

        val mockIterator = mockCollection<DigitalGoodsStore.Purchase>()
        whenever(mockDigitalGoodsStoreStore!!.getSellerBuyerPurchases(eq(sellerId), eq(buyerId), eq(from), eq(to))).thenReturn(mockIterator)

        assertEquals(mockIterator, t!!.getSellerBuyerPurchases(sellerId, buyerId, from, to))
    }

    @Test
    fun getPendingSellerPurchases() {
        val sellerId = 123L
        val from = 1
        val to = 2

        val mockPurchaseIterator = mockCollection<Purchase>()
        whenever(mockDigitalGoodsStoreStore!!.getPendingSellerPurchases(eq(sellerId), eq(from), eq(to))).thenReturn(mockPurchaseIterator)

        assertEquals(mockPurchaseIterator, t!!.getPendingSellerPurchases(sellerId, from, to))
    }

}
