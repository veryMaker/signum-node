package brs.services.impl

import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.DigitalGoodsStoreStore
import brs.db.VersionedEntityTable
import brs.entity.Goods
import brs.entity.Purchase
import brs.services.AccountService
import brs.services.BlockchainService
import io.mockk.every
import io.mockk.mockk
import org.jooq.SortField
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DigitalGoodsStoreServiceImplTest : AbstractUnitTest() {

    private lateinit var blockchainService: BlockchainService

    private lateinit var mockAccountService: AccountService
    private lateinit var mockDigitalGoodsStoreStore: DigitalGoodsStoreStore

    private lateinit var mockGoodsTable: VersionedEntityTable<Goods>
    private lateinit var mockPurchaseTable: VersionedEntityTable<Purchase>
    private lateinit var mockGoodsDbKeyFactory: LongKeyFactory<Goods>

    private lateinit var t: DigitalGoodsStoreServiceImpl

    @Before
    fun setUp() {
        blockchainService = mockk(relaxed = true)
        mockGoodsTable = mockk(relaxed = true)
        mockPurchaseTable = mockk(relaxed = true)
        mockDigitalGoodsStoreStore = mockk(relaxed = true)
        mockGoodsDbKeyFactory = mockk(relaxed = true)
        mockAccountService = mockk(relaxed = true)

        every { mockDigitalGoodsStoreStore.goodsTable } returns mockGoodsTable
        every { mockDigitalGoodsStoreStore.purchaseTable } returns mockPurchaseTable
        every { mockDigitalGoodsStoreStore.goodsDbKeyFactory } returns mockGoodsDbKeyFactory

        t = DigitalGoodsStoreServiceImpl(QuickMocker.dependencyProvider(
            blockchainService,
            mockDigitalGoodsStoreStore,
            mockAccountService
        ))
    }

    @Test
    fun getGoods() {
        val mockKey = mockk<BurstKey>(relaxed = true)
        val mockGoods = mockk<Goods>(relaxed = true)

        every { mockGoodsDbKeyFactory.newKey(eq(1L)) } returns mockKey
        every { mockGoodsTable.get(eq(mockKey)) } returns mockGoods

        assertEquals(mockGoods, t.getGoods(1L))
    }

    @Test
    fun getAllGoods() {
        val from = 1
        val to = 2

        val mockIterator = mockCollection<Goods>()
        every { mockGoodsTable.getAll(eq(from), eq(to), any<Collection<SortField<*>>>()) } returns mockIterator

        assertEquals(mockIterator, t.getAllGoods(from, to))
    }

    @Test
    fun getGoodsInStock() {
        val from = 1
        val to = 2

        val mockIterator = mockCollection<Goods>()
        every { mockDigitalGoodsStoreStore.getGoodsInStock(eq(from), eq(to)) } returns mockIterator

        assertEquals(mockIterator, t.getGoodsInStock(from, to))
    }

    @Test
    fun getSellerGoods() {
        val sellerId = 1L
        val inStockOnly = false
        val from = 1
        val to = 2

        val mockIterator = mockCollection<Goods>()
        every { mockDigitalGoodsStoreStore.getSellerGoods(eq(sellerId), eq(inStockOnly), eq(from), eq(to)) } returns mockIterator

        assertEquals(mockIterator, t.getSellerGoods(sellerId, inStockOnly, from, to))
    }

    @Test
    fun getAllPurchases() {
        val from = 1
        val to = 2

        val mockIterator = mockCollection<Purchase>()
        every { mockPurchaseTable.getAll(eq(from), eq(to), any<Collection<SortField<*>>>()) } returns mockIterator

        assertEquals(mockIterator, t.getAllPurchases(from, to))
    }

    @Test
    fun getSellerPurchases() {
        val sellerId = 1L
        val from = 2
        val to = 3

        val mockIterator = mockCollection<Purchase>()
        every { mockDigitalGoodsStoreStore.getSellerPurchases(eq(sellerId), eq(from), eq(to)) } returns mockIterator

        assertEquals(mockIterator, t.getSellerPurchases(sellerId, from, to))
    }

    @Test
    fun getBuyerPurchases() {
        val buyerId = 1L
        val from = 2
        val to = 3

        val mockIterator = mockCollection<Purchase>()
        every { mockDigitalGoodsStoreStore.getBuyerPurchases(eq(buyerId), eq(from), eq(to)) } returns mockIterator

        assertEquals(mockIterator, t.getBuyerPurchases(buyerId, from, to))
    }

    @Test
    fun getSellerBuyerPurchases() {
        val sellerId = 1L
        val buyerId = 2L
        val from = 3
        val to = 4

        val mockIterator = mockCollection<Purchase>()
        every { mockDigitalGoodsStoreStore.getSellerBuyerPurchases(eq(sellerId), eq(buyerId), eq(from), eq(to)) } returns mockIterator

        assertEquals(mockIterator, t.getSellerBuyerPurchases(sellerId, buyerId, from, to))
    }

    @Test
    fun getPendingSellerPurchases() {
        val sellerId = 123L
        val from = 1
        val to = 2

        val mockPurchaseIterator = mockCollection<Purchase>()
        every { mockDigitalGoodsStoreStore.getPendingSellerPurchases(eq(sellerId), eq(from), eq(to)) } returns mockPurchaseIterator

        assertEquals(mockPurchaseIterator, t.getPendingSellerPurchases(sellerId, from, to))
    }

}
