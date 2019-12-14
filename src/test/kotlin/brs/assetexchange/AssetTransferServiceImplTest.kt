package brs.assetexchange

import brs.entity.AssetTransfer
import brs.db.sql.SqlEntityTable
import brs.db.AssetTransferStore
import brs.services.impl.AssetTransferServiceImpl
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AssetTransferServiceImplTest {

    private lateinit var t: AssetTransferServiceImpl

    private lateinit var mockAssetTransferStore: AssetTransferStore
    private lateinit var mockAssetTransferTable: SqlEntityTable<AssetTransfer>

    @Before
    fun setUp() {
        mockAssetTransferStore = mock()
        mockAssetTransferTable = mock()

        whenever(mockAssetTransferStore.assetTransferTable).doReturn(mockAssetTransferTable)

        t = AssetTransferServiceImpl(mockAssetTransferStore)
    }

    @Test
    fun getAssetTransfers() {
        val assetId = 123L
        val from = 1
        val to = 4

        val mockAssetTransferIterator = mock<Collection<AssetTransfer>>()

        whenever(mockAssetTransferStore.getAssetTransfers(eq(assetId), eq(from), eq(to))).doReturn(mockAssetTransferIterator)

        assertEquals(mockAssetTransferIterator, t.getAssetTransfers(assetId, from, to))
    }

    @Test
    fun getAccountAssetTransfers() {
        val accountId = 12L
        val assetId = 123L
        val from = 1
        val to = 4

        val mockAccountAssetTransferIterator = mock<Collection<AssetTransfer>>()

        whenever(mockAssetTransferStore.getAccountAssetTransfers(eq(accountId), eq(assetId), eq(from), eq(to))).doReturn(mockAccountAssetTransferIterator)

        assertEquals(mockAccountAssetTransferIterator, t.getAccountAssetTransfers(accountId, assetId, from, to))
    }

    @Test
    fun getTransferCount() {
        whenever(mockAssetTransferStore.getTransferCount(eq(123L))).doReturn(5)

        assertEquals(5, t.getTransferCount(123L).toLong())
    }

    @Test
    fun getAssetTransferCount() {
        whenever(mockAssetTransferTable.count).doReturn(5)

        assertEquals(5, t.assetTransferCount.toLong())
    }
}

