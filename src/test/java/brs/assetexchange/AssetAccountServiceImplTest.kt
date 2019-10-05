package brs.assetexchange

import brs.Account.AccountAsset
import brs.db.store.AccountStore
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AssetAccountServiceImplTest {

    private lateinit var t: AssetAccountServiceImpl

    private lateinit var mockAccountStore: AccountStore

    @Before
    fun setUp() {
        mockAccountStore = mock()
        t = AssetAccountServiceImpl(mockAccountStore)
    }

    @Test
    fun getAssetAccounts() {
        val assetId = 4L
        val from = 1
        val to = 5

        val mockAccountIterator = mock<Collection<AccountAsset>>()

        whenever(mockAccountStore!!.getAssetAccounts(eq(assetId), eq(from), eq(to))).doReturn(mockAccountIterator)

        assertEquals(mockAccountIterator, t!!.getAssetAccounts(assetId, from, to))
    }

    @Test
    fun getAssetAccounts_withHeight() {
        val assetId = 4L
        val from = 1
        val to = 5
        val height = 3

        val mockAccountIterator = mock<Collection<AccountAsset>>()

        whenever(mockAccountStore!!.getAssetAccounts(eq(assetId), eq(height), eq(from), eq(to))).doReturn(mockAccountIterator)

        assertEquals(mockAccountIterator, t!!.getAssetAccounts(assetId, height, from, to))
    }

    @Test
    fun getAssetAccounts_withHeight_negativeHeightGivesForZeroHeight() {
        val assetId = 4L
        val from = 1
        val to = 5
        val height = -2

        val mockAccountIterator = mock<Collection<AccountAsset>>()

        whenever(mockAccountStore!!.getAssetAccounts(eq(assetId), eq(from), eq(to))).doReturn(mockAccountIterator)

        assertEquals(mockAccountIterator, t!!.getAssetAccounts(assetId, height, from, to))
    }

    @Test
    fun getAssetAccountsCount() {
        whenever(mockAccountStore!!.getAssetAccountsCount(eq(123L))).doReturn(5)

        assertEquals(5L, t!!.getAssetAccountsCount(123).toLong())
    }
}
