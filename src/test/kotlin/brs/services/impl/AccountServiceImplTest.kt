package brs.services.impl

import brs.common.QuickMocker
import brs.db.AccountStore
import brs.db.AssetTransferStore
import brs.db.BatchEntityTable
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.entity.Account
import brs.entity.Account.RewardRecipientAssignment
import brs.objects.Constants.EMPTY_BYTE_ARRAY
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jooq.SortField
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AccountServiceImplTest {
    private lateinit var accountStoreMock: AccountStore
    private lateinit var accountTableMock: BatchEntityTable<Account>
    private lateinit var accountBurstKeyFactoryMock: LongKeyFactory<Account>
    private lateinit var assetTransferStoreMock: AssetTransferStore

    private lateinit var t: AccountServiceImpl

    @Before
    fun setUp() {
        accountStoreMock = mockk(relaxed = true)
        accountTableMock = mockk(relaxed = true)
        accountBurstKeyFactoryMock = mockk(relaxed = true)
        assetTransferStoreMock = mockk(relaxed = true)

        every { accountStoreMock.accountTable } returns accountTableMock
        every { accountStoreMock.accountKeyFactory } returns accountBurstKeyFactoryMock

        t = AccountServiceImpl(QuickMocker.dependencyProvider(accountStoreMock, assetTransferStoreMock))
    }

    @Test
    fun getAccount() {
        val mockId = 123L
        val mockKey = mockk<BurstKey>(relaxed = true)
        val mockResultAccount = mockk<Account>(relaxed = true)

        every { accountBurstKeyFactoryMock.newKey(eq(mockId)) } returns mockKey
        every { accountTableMock[eq(mockKey)] } returns mockResultAccount

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
        val mockKey = mockk<BurstKey>(relaxed = true)
        val mockResultAccount = mockk<Account>(relaxed = true)

        every { accountBurstKeyFactoryMock.newKey(eq(id)) } returns mockKey
        every { accountTableMock[eq(mockKey), eq(height)] } returns mockResultAccount

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

        val mockKey = mockk<BurstKey>(relaxed = true)
        val mockAccount = mockk<Account>(relaxed = true)

        every { accountBurstKeyFactoryMock.newKey(any<Long>()) } returns mockKey
        every { accountTableMock[mockKey] } returns mockAccount

        every { mockAccount.publicKey } returns publicKey

        assertEquals(mockAccount, t.getAccount(publicKey))
    }

    @Test
    fun getAccount_withoutPublicKey() {
        val publicKey = ByteArray(1)
        publicKey[0] = 1.toByte()

        val mockKey = mockk<BurstKey>(relaxed = true)
        val mockAccount = mockk<Account>(relaxed = true)

        every { accountBurstKeyFactoryMock.newKey(any<Long>()) } returns mockKey
        every { accountTableMock[mockKey] } returns mockAccount

        every { mockAccount.publicKey } returns null

        assertEquals(mockAccount, t.getAccount(publicKey))
    }

    @Test
    fun getAccount_withPublicKey_notFoundReturnsNull() {
        val mockKey = mockk<BurstKey>(relaxed = true)

        every { accountBurstKeyFactoryMock.newKey(any<Long>()) } returns mockKey
        every { accountTableMock[mockKey] } returns null

        assertNull(t.getAccount(EMPTY_BYTE_ARRAY))
    }

    @Test(expected = Exception::class)
    fun getAccount_withPublicKey_duplicateKeyForAccount() {
        val publicKey = ByteArray(1)
        publicKey[0] = 1.toByte()
        val otherPublicKey = ByteArray(1)
        otherPublicKey[0] = 2.toByte()

        val mockKey = mockk<BurstKey>(relaxed = true)
        val mockAccount = mockk<Account>(relaxed = true)

        every { accountBurstKeyFactoryMock.newKey(any<Long>()) } returns mockKey
        every { accountTableMock[mockKey] } returns mockAccount

        every { mockAccount.publicKey } returns otherPublicKey

        t.getAccount(publicKey)
    }

    @Test
    fun getAssetTransfers() {
        val accountId = 123L
        val from = 2
        val to = 3

        t.getAssetTransfers(accountId, from, to)

        verify { assetTransferStoreMock.getAccountAssetTransfers(eq(accountId), eq(from), eq(to)) }
    }

    @Test
    fun getAssets() {
        val accountId = 123L
        val from = 2
        val to = 3

        t.getAssets(accountId, from, to)

        verify { accountStoreMock.getAssets(eq(from), eq(to), eq(accountId)) }
    }

    @Test
    fun getAccountsWithRewardRecipient() {
        val recipientId = 123L
        val mockAccountsIterator = mockk<Collection<RewardRecipientAssignment>>()

        every { accountStoreMock.getAccountsWithRewardRecipient(eq(recipientId)) } returns mockAccountsIterator

        assertEquals(mockAccountsIterator, t.getAccountsWithRewardRecipient(recipientId))
    }

    @Test
    fun getAllAccounts() {
        val from = 1
        val to = 5
        val mockAccountsIterator = mockk<Collection<Account>>()

        every { accountTableMock.getAll(eq(from), eq(to), any<Collection<SortField<*>>>()) } returns mockAccountsIterator

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

        val mockKey = mockk<BurstKey>(relaxed = true)

        every { accountBurstKeyFactoryMock.newKey(eq(accountId)) } returns mockKey
        every { accountTableMock[eq(mockKey)] } returns null

        val createdAccount = t.getOrAddAccount(accountId)

        assertNotNull(createdAccount)
        assertEquals(accountId, createdAccount.id)

        verify { accountTableMock.insert(eq(createdAccount)) }
    }

    @Test
    fun getOrAddAccount_getAccount() {
        val accountId = 123L

        val mockKey = mockk<BurstKey>(relaxed = true)
        val mockAccount = mockk<Account>(relaxed = true)

        every { accountBurstKeyFactoryMock.newKey(eq(accountId)) } returns mockKey
        every { accountTableMock[eq(mockKey)] } returns mockAccount

        val retrievedAccount = t.getOrAddAccount(accountId)

        assertNotNull(retrievedAccount)
        assertEquals(mockAccount, retrievedAccount)
    }

    @Test
    fun flushAccountTable() {
        t.flushAccountTable(0)

        verify { accountTableMock.finish(eq(0)) }
    }

    @Test
    fun getCount() {
        val count = 5

        every { accountTableMock.count } returns count

        assertEquals(count.toLong(), t.count.toLong())
    }
}
