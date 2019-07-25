package brs.assetexchange

import brs.AssetTransfer
import brs.db.sql.EntitySqlTable
import brs.db.store.AssetTransferStore
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test

import org.junit.Assert.assertEquals

class AssetTransferServiceImplTest {

    private var t: AssetTransferServiceImpl? = null

    private var mockAssetTransferStore: AssetTransferStore? = null
    private var mockAssetTransferTable: EntitySqlTable<AssetTransfer>? = null

    @Before
    fun setUp() {
        mockAssetTransferStore = mock()
        mockAssetTransferTable = mock()

        whenever(mockAssetTransferStore!!.assetTransferTable).thenReturn(mockAssetTransferTable)

        t = AssetTransferServiceImpl(mockAssetTransferStore!!)
    }

    @Test
    fun getAssetTransfers() {
        val assetId = 123L
        val from = 1
        val to = 4

        val mockAssetTransferIterator = mock<Collection<AssetTransfer>>()

        whenever(mockAssetTransferStore!!.getAssetTransfers(eq(assetId), eq(from), eq(to))).thenReturn(mockAssetTransferIterator)

        assertEquals(mockAssetTransferIterator, t!!.getAssetTransfers(assetId, from, to))
    }

    @Test
    fun getAccountAssetTransfers() {
        val accountId = 12L
        val assetId = 123L
        val from = 1
        val to = 4

        val mockAccountAssetTransferIterator = mock<Collection<AssetTransfer>>()

        whenever(mockAssetTransferStore!!.getAccountAssetTransfers(eq(accountId), eq(assetId), eq(from), eq(to))).thenReturn(mockAccountAssetTransferIterator)

        assertEquals(mockAccountAssetTransferIterator, t!!.getAccountAssetTransfers(accountId, assetId, from, to))
    }

    @Test
    fun getTransferCount() {
        whenever(mockAssetTransferStore!!.getTransferCount(eq(123L))).thenReturn(5)

        assertEquals(5, t!!.getTransferCount(123L).toLong())
    }

    @Test
    fun getAssetTransferCount() {
        whenever(mockAssetTransferTable!!.count).thenReturn(5)

        assertEquals(5, t!!.assetTransferCount.toLong())
    }
}

