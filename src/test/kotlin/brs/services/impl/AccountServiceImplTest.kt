package brs.services.impl

import brs.common.QuickMocker
import brs.db.AccountStore
import brs.db.AssetTransferStore
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.VersionedBatchEntityTable
import brs.entity.Account
import brs.entity.Account.RewardRecipientAssignment
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AccountServiceImplTest {

    private lateinit var accountStoreMock: AccountStore
    private lateinit var accountTableMock: VersionedBatchEntityTable<Account>
    private lateinit var accountBurstKeyFactoryMock: LongKeyFactory<Account>
    private lateinit var assetTransferStoreMock: AssetTransferStore

    private lateinit var t: AccountServiceImpl

    @Before
    fun setUp() {
        accountStoreMock = mock()
        accountTableMock = mock()
        accountBurstKeyFactoryMock = mock()
        assetTransferStoreMock = mock()

        whenever(accountStoreMock.accountTable).doReturn(accountTableMock)
        whenever(accountStoreMock.accountKeyFactory).doReturn(accountBurstKeyFactoryMock)

        t = AccountServiceImpl(QuickMocker.dependencyProvider(accountStoreMock, assetTransferStoreMock))
    }

    @Test
    fun getAccount() {
        val mockId = 123L
        val mockKey = mock<BurstKey>()
        val mockResultAccount = mock<Account>()

        whenever(accountBurstKeyFactoryMock.newKey(eq(mockId))).doReturn(mockKey)
        whenever(accountTableMock[eq(mockKey)]).doReturn(mockResultAccount)

        assertEquals(mockResultAccount, t.getAccount(mockId))
    }

    @Test
    fun getAccount_id0ReturnsNull() {
        assertNull(t.getAccount(0))
    }

    @Test
    fun getAccount_withHeight() {
        val id = 123L
        val height = 2
        val mockKey = mock<BurstKey>()
        val mockResultAccount = mock<Account>()

        whenever(accountBurstKeyFactoryMock.newKey(eq(id))).doReturn(mockKey)
        whenever(accountTableMock[eq(mockKey), eq(height)]).doReturn(mockResultAccount)

        assertEquals(mockResultAccount, t.getAccount(id, height))
    }

    @Test
    fun getAccount_withHeight_0returnsNull() {
        assertNull(t.getAccount(0, 2))
    }

    @Test
    fun getAccount_withPublicKey() {
        val publicKey = ByteArray(1)
        publicKey[0] = 1.toByte()

        val mockKey = mock<BurstKey>()
        val mockAccount = mock<Account>()

        whenever(accountBurstKeyFactoryMock.newKey(any<Long>())).doReturn(mockKey)
        whenever(accountTableMock[mockKey]).doReturn(mockAccount)

        whenever(mockAccount.publicKey).doReturn(publicKey)

        assertEquals(mockAccount, t.getAccount(publicKey))
    }

    @Test
    fun getAccount_withoutPublicKey() {
        val publicKey = ByteArray(1)
        publicKey[0] = 1.toByte()

        val mockKey = mock<BurstKey>()
        val mockAccount = mock<Account>()

        whenever(accountBurstKeyFactoryMock.newKey(any<Long>())).doReturn(mockKey)
        whenever(accountTableMock[mockKey]).doReturn(mockAccount)

        whenever(mockAccount.publicKey).doReturn(null)

        assertEquals(mockAccount, t.getAccount(publicKey))
    }

    @Test
    fun getAccount_withPublicKey_notFoundReturnsNull() {
        val publicKey = ByteArray(0)
        val mockKey = mock<BurstKey>()

        whenever(accountBurstKeyFactoryMock.newKey(any<Long>())).doReturn(mockKey)
        whenever(accountTableMock[mockKey]).doReturn(null)

        assertNull(t.getAccount(publicKey))
    }

    @Test(expected = Exception::class)
    fun getAccount_withPublicKey_duplicateKeyForAccount() {
        val publicKey = ByteArray(1)
        publicKey[0] = 1.toByte()
        val otherPublicKey = ByteArray(1)
        otherPublicKey[0] = 2.toByte()

        val mockKey = mock<BurstKey>()
        val mockAccount = mock<Account>()

        whenever(accountBurstKeyFactoryMock.newKey(any<Long>())).doReturn(mockKey)
        whenever(accountTableMock[mockKey]).doReturn(mockAccount)

        whenever(mockAccount.publicKey).doReturn(otherPublicKey)

        t.getAccount(publicKey)
    }

    @Test
    fun getAssetTransfers() {
        val accountId = 123L
        val from = 2
        val to = 3

        t.getAssetTransfers(accountId, from, to)

        verify(assetTransferStoreMock).getAccountAssetTransfers(eq(accountId), eq(from), eq(to))
    }

    @Test
    fun getAssets() {
        val accountId = 123L
        val from = 2
        val to = 3

        t.getAssets(accountId, from, to)

        verify(accountStoreMock).getAssets(eq(from), eq(to), eq(accountId))
    }

    @Test
    fun getAccountsWithRewardRecipient() {
        val recipientId = 123L
        val mockAccountsIterator = mock<Collection<RewardRecipientAssignment>>()

        whenever(accountStoreMock.getAccountsWithRewardRecipient(eq(recipientId))).doReturn(mockAccountsIterator)

        assertEquals(mockAccountsIterator, t.getAccountsWithRewardRecipient(recipientId))
    }

    @Test
    fun getAllAccounts() {
        val from = 1
        val to = 5
        val mockAccountsIterator = mock<Collection<Account>>()

        whenever(accountTableMock.getAll(eq(from), eq(to))).doReturn(mockAccountsIterator)

        assertEquals(mockAccountsIterator, t.getAllAccounts(from, to))
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

        whenever(accountBurstKeyFactoryMock.newKey(eq(accountId))).doReturn(mockKey)
        whenever(accountTableMock[eq(mockKey)]).doReturn(null)

        val createdAccount = t.getOrAddAccount(accountId)

        assertNotNull(createdAccount)
        assertEquals(accountId, createdAccount.id)

        verify(accountTableMock).insert(eq(createdAccount))
    }

    @Test
    fun getOrAddAccount_getAccount() {
        val accountId = 123L

        val mockKey = mock<BurstKey>()
        val mockAccount = mock<Account>()

        whenever(accountBurstKeyFactoryMock.newKey(eq(accountId))).doReturn(mockKey)
        whenever(accountTableMock[eq(mockKey)]).doReturn(mockAccount)

        val retrievedAccount = t.getOrAddAccount(accountId)

        assertNotNull(retrievedAccount)
        assertEquals(mockAccount, retrievedAccount)
    }

    @Test
    fun flushAccountTable() {
        t.flushAccountTable()

        verify(accountTableMock).finish()
    }

    @Test
    fun getCount() {
        val count = 5

        whenever(accountTableMock.count).doReturn(count)

        assertEquals(count.toLong(), t.count.toLong())
    }

}
