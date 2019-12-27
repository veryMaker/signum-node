package brs.assetexchange

import brs.entity.Account.AccountAsset
import brs.db.AccountStore
import brs.services.impl.AssetAccountServiceImpl
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AssetAccountServiceImplTest {

    private lateinit var t: AssetAccountServiceImpl

    private lateinit var mockAccountStore: AccountStore

    @Before
    fun setUp() {
        mockAccountStore = mockk()
        t = AssetAccountServiceImpl(mockAccountStore)
    }

    @Test
    fun getAssetAccounts() {
        val assetId = 4L
        val from = 1
        val to = 5

        val mockAccountIterator = mockk<Collection<AccountAsset>>()

        every { mockAccountStore.getAssetAccounts(eq(assetId), eq(from), eq(to)) } returns mockAccountIterator

        assertEquals(mockAccountIterator, t.getAssetAccounts(assetId, from, to))
    }

    @Test
    fun getAssetAccounts_withHeight() {
        val assetId = 4L
        val from = 1
        val to = 5
        val height = 3

        val mockAccountIterator = mockk<Collection<AccountAsset>>()

        every { mockAccountStore.getAssetAccounts(eq(assetId), eq(height), eq(from), eq(to)) } returns mockAccountIterator

        assertEquals(mockAccountIterator, t.getAssetAccounts(assetId, height, from, to))
    }

    @Test
    fun getAssetAccounts_withHeight_negativeHeightGivesForZeroHeight() {
        val assetId = 4L
        val from = 1
        val to = 5
        val height = -2

        val mockAccountIterator = mockk<Collection<AccountAsset>>()

        every { mockAccountStore.getAssetAccounts(eq(assetId), eq(from), eq(to)) } returns mockAccountIterator

        assertEquals(mockAccountIterator, t.getAssetAccounts(assetId, height, from, to))
    }

    @Test
    fun getAssetAccountsCount() {
        every { mockAccountStore.getAssetAccountsCount(eq(123L)) } returns 5

        assertEquals(5L, t.getAssetAccountsCount(123).toLong())
    }
}
