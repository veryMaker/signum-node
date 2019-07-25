package brs.services.impl

import brs.Account
import brs.Account.RewardRecipientAssignment
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.VersionedBatchEntityTable
import brs.db.store.AccountStore
import brs.db.store.AssetTransferStore
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

class AccountServiceImplTest {

    private var accountStoreMock: AccountStore? = null
    private var accountTableMock: VersionedBatchEntityTable<Account>? = null
    private var accountBurstKeyFactoryMock: LongKeyFactory<Account>? = null
    private var assetTransferStoreMock: AssetTransferStore? = null

    private var t: AccountServiceImpl? = null

    @Before
    fun setUp() {
        accountStoreMock = mock()
        accountTableMock = mock()
        accountBurstKeyFactoryMock = mock()
        assetTransferStoreMock = mock()

        whenever(accountStoreMock!!.accountTable).thenReturn(accountTableMock)
        whenever(accountStoreMock!!.accountKeyFactory).thenReturn(accountBurstKeyFactoryMock)

        t = AccountServiceImpl(accountStoreMock!!, assetTransferStoreMock)
    }

    @Test
    fun getAccount() {
        val mockId = 123L
        val mockKey = mock<BurstKey>()
        val mockResultAccount = mock<Account>()

        whenever(accountBurstKeyFactoryMock!!.newKey(eq(mockId))).thenReturn(mockKey)
        whenever(accountTableMock!!.get(eq(mockKey))).thenReturn(mockResultAccount)

        assertEquals(mockResultAccount, t!!.getAccount(mockId))
    }

    @Test
    fun getAccount_id0ReturnsNull() {
        assertNull(t!!.getAccount(0))
    }

    @Test
    fun getAccount_withHeight() {
        val id = 123L
        val height = 2
        val mockKey = mock<BurstKey>()
        val mockResultAccount = mock<Account>()

        whenever(accountBurstKeyFactoryMock!!.newKey(eq(id))).thenReturn(mockKey)
        whenever(accountTableMock!!.get(eq(mockKey), eq(height))).thenReturn(mockResultAccount)

        assertEquals(mockResultAccount, t!!.getAccount(id, height))
    }

    @Test
    fun getAccount_withHeight_0returnsNull() {
        assertNull(t!!.getAccount(0, 2))
    }

    @Test
    fun getAccount_withPublicKey() {
        val publicKey = ByteArray(1)
        publicKey[0] = 1.toByte()

        val mockKey = mock<BurstKey>()
        val mockAccount = mock<Account>()

        whenever(accountBurstKeyFactoryMock!!.newKey(any<Long>())).thenReturn(mockKey)
        whenever(accountTableMock!!.get(mockKey)).thenReturn(mockAccount)

        whenever(mockAccount.publicKey).thenReturn(publicKey)

        assertEquals(mockAccount, t!!.getAccount(publicKey))
    }

    @Test
    fun getAccount_withoutPublicKey() {
        val publicKey = ByteArray(1)
        publicKey[0] = 1.toByte()

        val mockKey = mock<BurstKey>()
        val mockAccount = mock<Account>()

        whenever(accountBurstKeyFactoryMock!!.newKey(any<Long>())).thenReturn(mockKey)
        whenever(accountTableMock!!.get(mockKey)).thenReturn(mockAccount)

        whenever(mockAccount.publicKey).thenReturn(null)

        assertEquals(mockAccount, t!!.getAccount(publicKey))
    }

    @Test
    fun getAccount_withPublicKey_notFoundReturnsNull() {
        val publicKey = ByteArray(0)
        val mockKey = mock<BurstKey>()

        whenever(accountBurstKeyFactoryMock!!.newKey(any<Long>())).thenReturn(mockKey)
        whenever(accountTableMock!!.get(mockKey)).thenReturn(null)

        assertNull(t!!.getAccount(publicKey))
    }

    @Test(expected = RuntimeException::class)
    fun getAccount_withPublicKey_duplicateKeyForAccount() {
        val publicKey = ByteArray(1)
        publicKey[0] = 1.toByte()
        val otherPublicKey = ByteArray(1)
        otherPublicKey[0] = 2.toByte()

        val mockKey = mock<BurstKey>()
        val mockAccount = mock<Account>()

        whenever(accountBurstKeyFactoryMock!!.newKey(any<Long>())).thenReturn(mockKey)
        whenever(accountTableMock!!.get(mockKey)).thenReturn(mockAccount)

        whenever(mockAccount.publicKey).thenReturn(otherPublicKey)

        t!!.getAccount(publicKey)
    }

    @Test
    fun getAssetTransfers() {
        val accountId = 123L
        val from = 2
        val to = 3

        t!!.getAssetTransfers(accountId, from, to)

        verify(assetTransferStoreMock!!).getAccountAssetTransfers(eq(accountId), eq(from), eq(to))
    }

    @Test
    fun getAssets() {
        val accountId = 123L
        val from = 2
        val to = 3

        t!!.getAssets(accountId, from, to)

        verify(accountStoreMock!!).getAssets(eq(from), eq(to), eq(accountId))
    }

    @Test
    fun getAccountsWithRewardRecipient() {
        val recipientId = 123L
        val mockAccountsIterator = mock<Collection<RewardRecipientAssignment>>()

        whenever(accountStoreMock!!.getAccountsWithRewardRecipient(eq<Long>(recipientId))).thenReturn(mockAccountsIterator)

        assertEquals(mockAccountsIterator, t!!.getAccountsWithRewardRecipient(recipientId))
    }

    @Test
    fun getAllAccounts() {
        val from = 1
        val to = 5
        val mockAccountsIterator = mock<Collection<Account>>()

        whenever(accountTableMock!!.getAll(eq(from), eq(to))).thenReturn(mockAccountsIterator)

        assertEquals(mockAccountsIterator, t!!.getAllAccounts(from, to))
    }

    @Test
    fun getId() {
        val publicKeyMock = ByteArray(1)
        publicKeyMock[0] = 1.toByte()
        assertEquals(-4227678059763665589L, AccountServiceImpl.getId(publicKeyMock))
    }

    // @Test
    fun getOrAddAccount_addAccount() {
        val accountId = 123L

        val mockKey = mock<BurstKey>()

        whenever(accountBurstKeyFactoryMock!!.newKey(eq(accountId))).thenReturn(mockKey)
        whenever(accountTableMock!!.get(eq(mockKey))).thenReturn(null)

        val createdAccount = t!!.getOrAddAccount(accountId)

        assertNotNull(createdAccount)
        assertEquals(accountId, createdAccount.getId())

        verify(accountTableMock!!).insert(eq(createdAccount))
    }

    @Test
    fun getOrAddAccount_getAccount() {
        val accountId = 123L

        val mockKey = mock<BurstKey>()
        val mockAccount = mock<Account>()

        whenever(accountBurstKeyFactoryMock!!.newKey(eq(accountId))).thenReturn(mockKey)
        whenever(accountTableMock!!.get(eq(mockKey))).thenReturn(mockAccount)

        val retrievedAccount = t!!.getOrAddAccount(accountId)

        assertNotNull(retrievedAccount)
        assertEquals(mockAccount, retrievedAccount)
    }

    @Test
    fun flushAccountTable() {
        t!!.flushAccountTable()

        verify(accountTableMock!!).finish()
    }

    @Test
    fun getCount() {
        val count = 5

        whenever(accountTableMock!!.count).thenReturn(count)

        assertEquals(count.toLong(), t!!.count.toLong())
    }

}
