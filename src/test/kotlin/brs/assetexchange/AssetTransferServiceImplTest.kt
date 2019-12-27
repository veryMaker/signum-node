package brs.assetexchange

import brs.entity.AssetTransfer
import brs.db.sql.SqlEntityTable
import brs.db.AssetTransferStore
import brs.services.impl.AssetTransferServiceImpl
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AssetTransferServiceImplTest {

    private lateinit var t: AssetTransferServiceImpl

    private lateinit var mockAssetTransferStore: AssetTransferStore
    private lateinit var mockAssetTransferTable: SqlEntityTable<AssetTransfer>

    @Before
    fun setUp() {
        mockAssetTransferStore = mockk()
        mockAssetTransferTable = mockk()

        every { mockAssetTransferStore.assetTransferTable } returns mockAssetTransferTable

        t = AssetTransferServiceImpl(mockAssetTransferStore)
    }

    @Test
    fun getAssetTransfers() {
        val assetId = 123L
        val from = 1
        val to = 4

        val mockAssetTransferIterator = mockk<Collection<AssetTransfer>>()

        every { mockAssetTransferStore.getAssetTransfers(eq(assetId), eq(from), eq(to)) } returns mockAssetTransferIterator

        assertEquals(mockAssetTransferIterator, t.getAssetTransfers(assetId, from, to))
    }

    @Test
    fun getAccountAssetTransfers() {
        val accountId = 12L
        val assetId = 123L
        val from = 1
        val to = 4

        val mockAccountAssetTransferIterator = mockk<Collection<AssetTransfer>>()

        every { mockAssetTransferStore.getAccountAssetTransfers(eq(accountId), eq(assetId), eq(from), eq(to)) } returns mockAccountAssetTransferIterator

        assertEquals(mockAccountAssetTransferIterator, t.getAccountAssetTransfers(accountId, assetId, from, to))
    }

    @Test
    fun getTransferCount() {
        every { mockAssetTransferStore.getTransferCount(eq(123L)) } returns 5

        assertEquals(5, t.getTransferCount(123L).toLong())
    }

    @Test
    fun getAssetTransferCount() {
        every { mockAssetTransferTable.count } returns 5

        assertEquals(5, t.assetTransferCount.toLong())
    }
}

