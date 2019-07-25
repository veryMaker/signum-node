package brs.assetexchange

import brs.Account.AccountAsset
import brs.Asset
import brs.AssetTransfer
import brs.Attachment.ColoredCoinsAssetIssuance
import brs.Trade
import brs.Transaction
import brs.common.AbstractUnitTest
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.sql.EntitySqlTable
import brs.db.store.AssetStore
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class AssetServiceImplTest : AbstractUnitTest() {

    private var t: AssetServiceImpl? = null

    private var assetAccountServiceMock: AssetAccountServiceImpl? = null
    private var assetTransferServicMock: AssetTransferServiceImpl? = null
    private var tradeServiceMock: TradeServiceImpl? = null
    private var assetStoreMock: AssetStore? = null
    private var assetTableMock: EntitySqlTable<Asset>? = null
    private var assetDbKeyFactoryMock: LongKeyFactory<Asset>? = null

    @Before
    fun setUp() {
        assetAccountServiceMock = mock()
        assetTransferServicMock = mock()
        tradeServiceMock = mock()

        assetStoreMock = mock()
        assetTableMock = mock()
        assetDbKeyFactoryMock = mock()

        whenever(assetStoreMock!!.assetTable).thenReturn(assetTableMock)
        whenever(assetStoreMock!!.assetDbKeyFactory).thenReturn(assetDbKeyFactoryMock)

        t = AssetServiceImpl(assetAccountServiceMock, tradeServiceMock, assetStoreMock!!, assetTransferServicMock)
    }

    @Test
    fun getAsset() {
        val assetId = 123L
        val mockAsset = mock<Asset>()
        val assetKeyMock = mock<BurstKey>()

        whenever(assetDbKeyFactoryMock!!.newKey(eq(assetId))).thenReturn(assetKeyMock)
        whenever(assetTableMock!!.get(eq(assetKeyMock))).thenReturn(mockAsset)

        assertEquals(mockAsset, t!!.getAsset(assetId))
    }

    @Test
    fun getAccounts() {
        val assetId = 123L
        val from = 1
        val to = 5

        val mockAccountAssetIterator = mock<Collection<AccountAsset>>()

        whenever(assetAccountServiceMock!!.getAssetAccounts(eq(assetId), eq(from), eq(to))).thenReturn(mockAccountAssetIterator)

        assertEquals(mockAccountAssetIterator, t!!.getAccounts(assetId, from, to))
    }

    @Test
    fun getAccounts_forHeight() {
        val assetId = 123L
        val from = 1
        val to = 5
        val height = 3

        val mockAccountAssetIterator = mock<Collection<AccountAsset>>()

        whenever(assetAccountServiceMock!!.getAssetAccounts(eq(assetId), eq(height), eq(from), eq(to))).thenReturn(mockAccountAssetIterator)

        assertEquals(mockAccountAssetIterator, t!!.getAccounts(assetId, height, from, to))
    }

    @Test
    fun getAccounts_forHeight_negativeHeightGivesForZeroHeight() {
        val assetId = 123L
        val from = 1
        val to = 5
        val height = -3

        val mockAccountAssetIterator = mock<Collection<AccountAsset>>()

        whenever(assetAccountServiceMock!!.getAssetAccounts(eq(assetId), eq(from), eq(to))).thenReturn(mockAccountAssetIterator)

        assertEquals(mockAccountAssetIterator, t!!.getAccounts(assetId, height, from, to))
    }

    @Test
    fun getTrades() {
        val assetId = 123L
        val from = 2
        val to = 4

        val mockTradeIterator = mock<Collection<Trade>>()

        whenever(tradeServiceMock!!.getAssetTrades(eq(assetId), eq(from), eq(to))).thenReturn(mockTradeIterator)

        assertEquals(mockTradeIterator, t!!.getTrades(assetId, from, to))
    }

    @Test
    fun getAssetTransfers() {
        val assetId = 123L
        val from = 2
        val to = 4

        val mockTransferIterator = mock<Collection<AssetTransfer>>()

        whenever(assetTransferServicMock!!.getAssetTransfers(eq(assetId), eq(from), eq(to))).thenReturn(mockTransferIterator)

        assertEquals(mockTransferIterator, t!!.getAssetTransfers(assetId, from, to))
    }

    @Test
    fun getAllAssetsTest() {
        val from = 2
        val to = 4

        val mockTradeIterator = mock<Collection<Asset>>()

        whenever(assetTableMock!!.getAll(eq(from), eq(to))).thenReturn(mockTradeIterator)

        assertEquals(mockTradeIterator, t!!.getAllAssets(from, to))
    }

    @Test
    fun getAssetsIssuesBy() {
        val accountId = 123L
        val from = 1
        val to = 2

        val mockAssetIterator = mockCollection<Asset>()
        whenever(assetStoreMock!!.getAssetsIssuedBy(eq(accountId), eq(from), eq(to))).thenReturn(mockAssetIterator)

        assertEquals(mockAssetIterator, t!!.getAssetsIssuedBy(accountId, from, to))
    }

    @Test
    fun getCount() {
        whenever(assetTableMock!!.count).thenReturn(5)

        assertEquals(5, t!!.assetsCount.toLong())
    }

    @Test
    fun addAsset() {
        val assetKey = mock<BurstKey>()

        val transactionId = 123L

        whenever(assetDbKeyFactoryMock!!.newKey(eq(transactionId))).thenReturn(assetKey)

        val savedAssetCaptor = argumentCaptor<Asset>()

        val transaction = mock<Transaction>()
        whenever(transaction.id).thenReturn(transactionId)

        val attachment = mock<ColoredCoinsAssetIssuance>()
        t!!.addAsset(transaction, attachment)

        verify(assetTableMock!!).insert(savedAssetCaptor.capture())

        val savedAsset = savedAssetCaptor.firstValue
        assertNotNull(savedAsset)

        assertEquals(assetKey, savedAsset.dbKey)
        assertEquals(transaction.id, savedAsset.id)
        assertEquals(transaction.senderId, savedAsset.accountId)
        assertEquals(attachment.name, savedAsset.name)
        assertEquals(attachment.description, savedAsset.description)
        assertEquals(attachment.quantityQNT, savedAsset.quantityQNT)
        assertEquals(attachment.decimals.toLong(), savedAsset.decimals.toLong())
    }
}
