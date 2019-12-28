package brs.assetexchange

import brs.common.AbstractUnitTest
import brs.db.AssetStore
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.sql.SqlEntityTable
import brs.entity.Account.AccountAsset
import brs.entity.Asset
import brs.entity.AssetTransfer
import brs.entity.Trade
import brs.entity.Transaction
import brs.services.impl.AssetAccountServiceImpl
import brs.services.impl.AssetServiceImpl
import brs.services.impl.AssetTradeServiceImpl
import brs.services.impl.AssetTransferServiceImpl
import brs.transaction.appendix.Attachment.ColoredCoinsAssetIssuance
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jooq.SortField
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class AssetServiceImplTest : AbstractUnitTest() {

    private lateinit var t: AssetServiceImpl

    private lateinit var assetAccountServiceMock: AssetAccountServiceImpl
    private lateinit var assetTransferServicMock: AssetTransferServiceImpl
    private lateinit var tradeServiceMock: AssetTradeServiceImpl
    private lateinit var assetStoreMock: AssetStore
    private lateinit var assetTableMock: SqlEntityTable<Asset>
    private lateinit var assetDbKeyFactoryMock: LongKeyFactory<Asset>

    @Before
    fun setUp() {
        assetAccountServiceMock = mockk(relaxed = true)
        assetTransferServicMock = mockk(relaxed = true)
        tradeServiceMock = mockk(relaxed = true)

        assetStoreMock = mockk(relaxed = true)
        assetTableMock = mockk(relaxed = true)
        assetDbKeyFactoryMock = mockk(relaxed = true)

        every { assetStoreMock.assetTable } returns assetTableMock
        every { assetStoreMock.assetDbKeyFactory } returns assetDbKeyFactoryMock

        t = AssetServiceImpl(
            assetAccountServiceMock,
            tradeServiceMock,
            assetStoreMock,
            assetTransferServicMock
        )
    }

    @Test
    fun getAsset() {
        val assetId = 123L
        val mockAsset = mockk<Asset>(relaxed = true)
        val assetKeyMock = mockk<BurstKey>(relaxed = true)

        every { assetDbKeyFactoryMock.newKey(eq(assetId)) } returns assetKeyMock
        every { assetTableMock[eq(assetKeyMock)] } returns mockAsset

        assertEquals(mockAsset, t.getAsset(assetId))
    }

    @Test
    fun getAccounts() {
        val assetId = 123L
        val from = 1
        val to = 5

        val mockAccountAssetIterator = mockk<Collection<AccountAsset>>()

        every { assetAccountServiceMock.getAssetAccounts(eq(assetId), eq(from), eq(to)) } returns mockAccountAssetIterator

        assertEquals(mockAccountAssetIterator, t.getAccounts(assetId, from, to))
    }

    @Test
    fun getAccounts_forHeight() {
        val assetId = 123L
        val from = 1
        val to = 5
        val height = 3

        val mockAccountAssetIterator = mockk<Collection<AccountAsset>>()

        every { assetAccountServiceMock.getAssetAccounts(eq(assetId), eq(height), eq(from), eq(to)) } returns mockAccountAssetIterator

        assertEquals(mockAccountAssetIterator, t.getAccounts(assetId, height, from, to))
    }

    @Test
    fun getAccounts_forHeight_negativeHeightGivesForZeroHeight() {
        val assetId = 123L
        val from = 1
        val to = 5
        val height = -3

        val mockAccountAssetIterator = mockk<Collection<AccountAsset>>()

        every { assetAccountServiceMock.getAssetAccounts(eq(assetId), eq(from), eq(to)) } returns mockAccountAssetIterator

        assertEquals(mockAccountAssetIterator, t.getAccounts(assetId, height, from, to))
    }

    @Test
    fun getTrades() {
        val assetId = 123L
        val from = 2
        val to = 4

        val mockTradeIterator = mockk<Collection<Trade>>()

        every { tradeServiceMock.getAssetTrades(eq(assetId), eq(from), eq(to)) } returns mockTradeIterator

        assertEquals(mockTradeIterator, t.getTrades(assetId, from, to))
    }

    @Test
    fun getAssetTransfers() {
        val assetId = 123L
        val from = 2
        val to = 4

        val mockTransferIterator = mockk<Collection<AssetTransfer>>()

        every { assetTransferServicMock.getAssetTransfers(eq(assetId), eq(from), eq(to)) } returns mockTransferIterator

        assertEquals(mockTransferIterator, t.getAssetTransfers(assetId, from, to))
    }

    @Test
    fun getAllAssetsTest() {
        val from = 2
        val to = 4

        val mockTradeIterator = mockk<Collection<Asset>>()

        every { assetTableMock.getAll(eq(from), eq(to), any<Collection<SortField<*>>>()) } returns mockTradeIterator

        assertEquals(mockTradeIterator, t.getAllAssets(from, to))
    }

    @Test
    fun getAssetsIssuesBy() {
        val accountId = 123L
        val from = 1
        val to = 2

        val mockAssetIterator = mockCollection<Asset>()
        every { assetStoreMock.getAssetsIssuedBy(eq(accountId), eq(from), eq(to)) } returns mockAssetIterator

        assertEquals(mockAssetIterator, t.getAssetsIssuedBy(accountId, from, to))
    }

    @Test
    fun getCount() {
        every { assetTableMock.count } returns 5

        assertEquals(5, t.assetsCount.toLong())
    }

    @Test
    fun addAsset() {
        val assetKey = mockk<BurstKey>(relaxed = true)

        val transactionId = 123L

        every { assetDbKeyFactoryMock.newKey(eq(transactionId)) } returns assetKey

        val savedAssetCaptor = CapturingSlot<Asset>()

        val transaction = mockk<Transaction>(relaxed = true)
        every { transaction.id } returns transactionId

        val attachment = mockk<ColoredCoinsAssetIssuance>(relaxed = true)
        t.addAsset(transaction, attachment)

        verify { assetTableMock.insert(capture(savedAssetCaptor)) }

        val savedAsset = savedAssetCaptor.captured
        assertNotNull(savedAsset)

        assertEquals(assetKey, savedAsset.dbKey)
        assertEquals(transaction.id, savedAsset.id)
        assertEquals(transaction.senderId, savedAsset.accountId)
        assertEquals(attachment.name, savedAsset.name)
        assertEquals(attachment.description, savedAsset.description)
        assertEquals(attachment.quantity, savedAsset.quantity)
        assertEquals(attachment.decimals.toLong(), savedAsset.decimals.toLong())
    }
}
