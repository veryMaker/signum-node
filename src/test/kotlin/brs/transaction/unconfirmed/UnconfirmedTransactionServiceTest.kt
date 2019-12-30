package brs.transaction.unconfirmed

import brs.common.QuickMocker
import brs.common.TestConstants
import brs.db.AccountStore
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.TransactionDb
import brs.db.VersionedBatchEntityTable
import brs.entity.Account
import brs.entity.DependencyProvider
import brs.entity.Transaction.Builder
import brs.objects.Constants
import brs.objects.Constants.FEE_QUANT
import brs.objects.FluxValues
import brs.objects.Props
import brs.peer.Peer
import brs.services.PropertyService
import brs.services.UnconfirmedTransactionService
import brs.services.impl.BlockchainServiceImpl
import brs.services.impl.TimeServiceImpl
import brs.services.impl.UnconfirmedTransactionServiceImpl
import brs.transaction.appendix.Attachment
import brs.transaction.appendix.Attachment.MessagingAliasSell
import brs.transaction.type.TransactionType
import brs.util.BurstException.NotCurrentlyValidException
import brs.util.convert.parseHexString
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class UnconfirmedTransactionServiceTest {

    private lateinit var mockBlockChain: BlockchainServiceImpl

    private lateinit var accountStoreMock: AccountStore
    private lateinit var accountTableMock: VersionedBatchEntityTable<Account>
    private lateinit var accountBurstKeyFactoryMock: LongKeyFactory<Account>

    private val timeService = TimeServiceImpl()
    private lateinit var t: UnconfirmedTransactionService

    private lateinit var dp: DependencyProvider

    @Before
    fun setUp() {

        val mockPropertyService = mockk<PropertyService>(relaxed = true)
        every { mockPropertyService.get(eq(Props.DB_MAX_ROLLBACK)) } returns 1440
        every { mockPropertyService.get(eq(Props.P2P_MAX_UNCONFIRMED_TRANSACTIONS)) } returns 8192
        every { mockPropertyService.get(eq(Props.P2P_MAX_PERCENTAGE_UNCONFIRMED_TRANSACTIONS_FULL_HASH_REFERENCE)) } returns 5
        every { mockPropertyService.get(eq(Props.P2P_MAX_UNCONFIRMED_TRANSACTIONS_RAW_SIZE_BYTES_TO_SEND)) } returns 175000

        mockBlockChain = mockk(relaxed = true)

        accountStoreMock = mockk(relaxed = true)
        accountTableMock = mockk(relaxed = true)
        accountBurstKeyFactoryMock = mockk(relaxed = true)
        val transactionDbMock = mockk<TransactionDb>(relaxed = true)
        every { accountStoreMock.accountTable } returns accountTableMock
        every { accountStoreMock.accountKeyFactory } returns accountBurstKeyFactoryMock

        val mockAccount = mockk<Account>(relaxed = true)
        val mockAccountKey = mockk<BurstKey>(relaxed = true)
        every { accountBurstKeyFactoryMock.newKey(eq(123L)) } returns mockAccountKey
        every { accountTableMock[any()] } returns null
        every { accountTableMock[eq(mockAccountKey)] } returns mockAccount
        every { mockAccount.unconfirmedBalancePlanck } returns Constants.MAX_BALANCE_PLANCK

        val mockFluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.PRE_DYMAXION, FluxValues.DIGITAL_GOODS_STORE)

        dp = QuickMocker.dependencyProvider(
            mockBlockChain, mockFluxCapacitor,
            accountStoreMock, timeService, mockPropertyService, transactionDbMock)

        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        t = UnconfirmedTransactionServiceImpl(dp)
    }

    @DisplayName("When we add Unconfirmed Transactions to the store, they can be retrieved")
    @Test
    fun transactionsCanGetRetrievedAfterAddingThemToStore() {
        every { mockBlockChain.height } returns 20
        (1..100).forEach { i ->
            val transaction = Builder(dp, 1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, i.toLong(), FEE_QUANT * 100, timeService.epochTime + 50000, 500.toShort(), Attachment.OrdinaryPayment(dp))
                    .id(i.toLong()).senderId(123L).build()
            transaction.sign(TestConstants.TEST_SECRET_PHRASE)
            t.put(transaction, null)
        }

        assertEquals(100, t.all.size)
        assertNotNull(t.get(1L))
    }


    @DisplayName("When a transaction got added by a peer, he won't get it reflected at him when getting unconfirmed transactions")
    @Test
    fun transactionsGivenByPeerWontGetReturnedToPeer() {
        val mockPeer = mockk<Peer>(relaxed = true)
        val otherMockPeer = mockk<Peer>(relaxed = true)

        every { mockBlockChain.height } returns 20

        (1..100).forEach { i ->
            val transaction = Builder(dp, 1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, i.toLong(), FEE_QUANT * 100, timeService.epochTime + 50000, 500.toShort(), Attachment.OrdinaryPayment(dp))
                    .id(i.toLong()).senderId(123L).build()
            transaction.sign(TestConstants.TEST_SECRET_PHRASE)
            t.put(transaction, mockPeer)
        }

        assertEquals(0, t.getAllFor(mockPeer).size)
        assertEquals(100, t.getAllFor(otherMockPeer).size)
    }

    @DisplayName("When a transactions got handed by a peer and we mark his fingerprints, he won't get it back a second time")
    @Test
    fun transactionsMarkedWithPeerFingerPrintsWontGetReturnedToPeer() {
        val mockPeer = mockk<Peer>(relaxed = true)

        every { mockBlockChain.height } returns 20

        (1..100).forEach { i ->
            val transaction = Builder(dp, 1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, i.toLong(), FEE_QUANT * 100, timeService.epochTime + 50000, 500.toShort(), Attachment.OrdinaryPayment(dp))
                    .id(i.toLong()).senderId(123L).build()
            transaction.sign(TestConstants.TEST_SECRET_PHRASE)
            t.put(transaction, null)
        }

        val mockPeerObtainedTransactions = t.getAllFor(mockPeer)
        assertEquals(100, mockPeerObtainedTransactions.size)

        t.markFingerPrintsOf(mockPeer, mockPeerObtainedTransactions)
        assertEquals(0, t.getAllFor(mockPeer).size)
    }


    @DisplayName("When The amount of unconfirmed transactions exceeds max size, and adding another then the cache size stays the same")
    @Test
    fun numberOfUnconfirmedTransactionsOfSameSlotExceedsMaxSizeAddAnotherThenCacheSizeStaysMaxSize() {

        every { mockBlockChain.height } returns 20

        (1..8192).forEach { i ->
            val transaction = Builder(dp, 1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, i.toLong(), FEE_QUANT * 100, timeService.epochTime + 50000, 500.toShort(), Attachment.OrdinaryPayment(dp))
                    .id(i.toLong()).senderId(123L).build()
            transaction.sign(TestConstants.TEST_SECRET_PHRASE)
            t.put(transaction, null)
        }

        assertEquals(8192, t.all.size)
        assertNotNull(t.get(1L))

        val oneTransactionTooMany = Builder(dp, 1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 9999, FEE_QUANT * 100, timeService.epochTime + 50000, 500.toShort(), Attachment.OrdinaryPayment(dp))
                .id(8193L).senderId(123L).build()
        oneTransactionTooMany.sign(TestConstants.TEST_SECRET_PHRASE)
        t.put(oneTransactionTooMany, null)

        assertEquals(8192, t.all.size)
        assertNull(t.get(1L))
    }

    @DisplayName("When the amount of unconfirmed transactions exceeds max size, and adding another of a higher slot, the cache size stays the same, and a lower slot transaction gets removed")
    @Test
    fun numberOfUnconfirmedTransactionsOfSameSlotExceedsMaxSizeAddAnotherThenCacheSizeStaysMaxSizeAndLowerSlotTransactionGetsRemoved() {

        every { mockBlockChain.height } returns 20

        (1..8192).forEach { i ->
            val transaction = Builder(dp, 1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, i.toLong(), FEE_QUANT * 100, timeService.epochTime + 50000, 500.toShort(), Attachment.OrdinaryPayment(dp))
                    .id(i.toLong()).senderId(123L).build()
            transaction.sign(TestConstants.TEST_SECRET_PHRASE)
            t.put(transaction, null)
        }

        assertEquals(8192, t.all.size)
        assertEquals(8192, t.all.filter { t -> t.feePlanck == FEE_QUANT * 100 }.count())
        assertNotNull(t.get(1L))

        val oneTransactionTooMany = Builder(dp, 1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 9999, FEE_QUANT * 200, timeService.epochTime + 50000, 500.toShort(), Attachment.OrdinaryPayment(dp))
                .id(8193L).senderId(123L).build()
        oneTransactionTooMany.sign(TestConstants.TEST_SECRET_PHRASE)
        t.put(oneTransactionTooMany, null)

        assertEquals(8192, t.all.size)
        assertEquals(8192 - 1, t.all.filter { t -> t.feePlanck == FEE_QUANT * 100 }.count())
        assertEquals(1, t.all.filter { t -> t.feePlanck == FEE_QUANT * 200 }.count())
    }

    @DisplayName("The unconfirmed transaction gets denied in case the account is unknown")
    @Test(expected = NotCurrentlyValidException::class)
    fun unconfirmedTransactionGetsDeniedForUnknownAccount() {
        every { mockBlockChain.height } returns 20

        val transaction = Builder(dp, 1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 735000, timeService.epochTime + 50000, 500.toShort(), Attachment.OrdinaryPayment(dp))
                .id(1).senderId(124L).build()
        transaction.sign(TestConstants.TEST_SECRET_PHRASE)
        t.put(transaction, null)
    }

    @DisplayName("The unconfirmed transaction gets denied in case the account does not have enough unconfirmed balance")
    @Test(expected = NotCurrentlyValidException::class)
    fun unconfirmedTransactionGetsDeniedForNotEnoughUnconfirmedBalance() {
        every { mockBlockChain.height } returns 20

        val transaction = Builder(dp, 1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, Constants.MAX_BALANCE_PLANCK, timeService.epochTime + 50000, 500.toShort(), Attachment.OrdinaryPayment(dp))
                .id(1).senderId(123L).build()
        transaction.sign(TestConstants.TEST_SECRET_PHRASE)

        try {
            t.put(transaction, null)
        } catch (ex: NotCurrentlyValidException) {
            assertTrue(t.all.isEmpty())
            throw ex
        }
    }

    @DisplayName("When adding the same unconfirmed transaction, nothing changes")
    @Test
    fun addingNewUnconfirmedTransactionWithSameIDResultsInNothingChanging() {
        every { mockBlockChain.height } returns 20

        val mockPeer = mockk<Peer>(relaxed = true)

        every { mockPeer.peerAddress } returns "mockPeer"

        val transactionBuilder = Builder(dp, 1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, Constants.MAX_BALANCE_PLANCK - 100000, timeService.epochTime + 50000,
                500.toShort(), Attachment.OrdinaryPayment(dp))
                .id(1).senderId(123L)

        val transaction1 = transactionBuilder.build()
        transaction1.sign(TestConstants.TEST_SECRET_PHRASE)
        t.put(transaction1, mockPeer)

        val transaction2 = transactionBuilder.build()
        transaction2.sign(TestConstants.TEST_SECRET_PHRASE)

        t.put(transaction2, mockPeer)

        assertEquals(1, t.all.size)
    }

    @DisplayName("When the maximum number of transactions with full hash reference is reached, following ones are ignored")
    @Test
    fun whenMaximumNumberOfTransactionsWithFullHashReferenceIsReachedFollowingOnesAreIgnored() {

        every { mockBlockChain.height } returns 20

        (1..414).forEach { i ->
            val transaction = Builder(dp, 1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, i.toLong(), FEE_QUANT * 2, timeService.epochTime + 50000, 500.toShort(), Attachment.OrdinaryPayment(dp))
                    .id(i.toLong()).senderId(123L).referencedTransactionFullHash("b33f".parseHexString()).build()
            transaction.sign(TestConstants.TEST_SECRET_PHRASE)
            t.put(transaction, null)
        }

        assertEquals(409, t.all.size)
    }

    @DisplayName("When the maximum number of transactions for a slot size is reached, following ones are ignored")
    @Test
    fun whenMaximumNumberOfTransactionsForSlotSizeIsReachedFollowingOnesAreIgnored() {

        every { mockBlockChain.height } returns 20

        (1..365).forEach { i ->
            val transaction = Builder(dp, 1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, i.toLong(), FEE_QUANT, timeService.epochTime + 50000, 500.toShort(), Attachment.OrdinaryPayment(dp))
                    .id(i.toLong()).senderId(123L).build()
            transaction.sign(TestConstants.TEST_SECRET_PHRASE)
            t.put(transaction, null)
        }

        assertEquals(360, t.all.size)

        (1..725).forEach { i ->
            val transaction = Builder(dp, 1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, i.toLong(), FEE_QUANT * 2, timeService.epochTime + 50000, 500.toShort(), Attachment.OrdinaryPayment(dp))
                    .id(i.toLong()).senderId(123L).build()
            transaction.sign(TestConstants.TEST_SECRET_PHRASE)
            t.put(transaction, null)
        }

        assertEquals(1080, t.all.size)
    }

    @Test
    fun cheaperDuplicateTransactionGetsRemoved() {
        val cheap = Builder(dp, 1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, FEE_QUANT, timeService.epochTime + 50000, 500.toShort(),
                MessagingAliasSell(dp, "aliasName", 123, 5))
                .id(1).senderId(123L).build()

        val expensive = Builder(dp, 1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, FEE_QUANT * 2, timeService.epochTime + 50000, 500.toShort(),
                MessagingAliasSell(dp, "aliasName", 123, 5))
                .id(2).senderId(123L).build()

        t.put(cheap, null)

        assertEquals(1, t.all.size)
        assertNotNull(t.get(cheap.id))

        t.put(expensive, null)

        assertEquals(1, t.all.size)
        assertNull(t.get(cheap.id))
        assertNotNull(t.get(expensive.id))
    }

    @Test
    fun cheaperDuplicateTransactionNeverGetsAdded() {
        val cheap = Builder(dp, 1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, FEE_QUANT, timeService.epochTime + 50000, 500.toShort(),
                MessagingAliasSell(dp, "aliasName", 123, 5))
                .id(1).senderId(123L).build()

        val expensive = Builder(dp, 1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, FEE_QUANT * 2, timeService.epochTime + 50000, 500.toShort(),
                MessagingAliasSell(dp, "aliasName", 123, 5))
                .id(2).senderId(123L).build()

        t.put(expensive, null)

        assertEquals(1, t.all.size)
        assertNotNull(t.get(expensive.id))

        t.put(cheap, null)

        assertEquals(1, t.all.size)
        assertNull(t.get(cheap.id))
        assertNotNull(t.get(expensive.id))
    }
}
