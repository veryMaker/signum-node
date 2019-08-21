package brs.unconfirmedtransactions

import brs.*
import brs.Attachment.MessagingAliasSell
import brs.BurstException.NotCurrentlyValidException
import brs.BurstException.ValidationException
import brs.Constants.FEE_QUANT
import brs.Transaction.Builder
import brs.common.QuickMocker
import brs.common.TestConstants
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.TransactionDb
import brs.db.VersionedBatchEntityTable
import brs.db.store.AccountStore
import brs.fluxcapacitor.FluxCapacitor
import brs.fluxcapacitor.FluxEnable
import brs.fluxcapacitor.FluxValues
import brs.peer.Peer
import brs.props.Prop
import brs.props.PropertyService
import brs.props.Props
import brs.services.impl.TimeServiceImpl
import com.nhaarman.mockitokotlin2.*
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Answers

@RunWith(JUnit4::class)
class UnconfirmedTransactionStoreTest {

    private var mockBlockChain: BlockchainImpl? = null

    private var accountStoreMock: AccountStore? = null
    private var accountTableMock: VersionedBatchEntityTable<Account>? = null
    private var accountBurstKeyFactoryMock: LongKeyFactory<Account>? = null

    private val timeService = TimeServiceImpl()
    private var t: UnconfirmedTransactionStore? = null

    @Before
    fun setUp() {
        mockkStatic(Burst::class)

        val mockPropertyService = mock<PropertyService>()
        whenever(mockPropertyService.get(eq<Prop<Int>>(Props.DB_MAX_ROLLBACK))).doReturn(1440)
        whenever(mockPropertyService.get(eq<Prop<Int>>(Props.P2P_MAX_UNCONFIRMED_TRANSACTIONS))).doReturn(8192)
        whenever(mockPropertyService.get(eq<Prop<Int>>(Props.P2P_MAX_PERCENTAGE_UNCONFIRMED_TRANSACTIONS_FULL_HASH_REFERENCE))).doReturn(5)
        whenever(mockPropertyService.get(eq<Prop<Int>>(Props.P2P_MAX_UNCONFIRMED_TRANSACTIONS_RAW_SIZE_BYTES_TO_SEND))).doReturn(175000)
        every { Burst.getPropertyService() } returns mockPropertyService

        mockBlockChain = mock()
        every { Burst.getBlockchain() } returns mockBlockChain

        accountStoreMock = mock()
        accountTableMock = mock()
        accountBurstKeyFactoryMock = mock()
        val transactionDbMock = mock<TransactionDb>(defaultAnswer = Answers.RETURNS_DEFAULTS)
        whenever(accountStoreMock!!.accountTable).doReturn(accountTableMock)
        whenever(accountStoreMock!!.accountKeyFactory).doReturn(accountBurstKeyFactoryMock)

        val mockAccount = mock<Account>()
        val mockAccountKey = mock<BurstKey>()
        whenever(accountBurstKeyFactoryMock!!.newKey(eq(123L))).doReturn(mockAccountKey)
        whenever(accountTableMock!!.get(eq(mockAccountKey))).doReturn(mockAccount)
        whenever(mockAccount.unconfirmedBalanceNQT).doReturn(Constants.MAX_BALANCE_NQT)

        val mockFluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.PRE_DYMAXION, FluxValues.DIGITAL_GOODS_STORE)

        TransactionType.init(mockBlockChain, mockFluxCapacitor, null, null, null, null, null, null)

        t = UnconfirmedTransactionStoreImpl(timeService, mockPropertyService, accountStoreMock, transactionDbMock)
    }

    @DisplayName("When we add Unconfirmed Transactions to the store, they can be retrieved")
    @Test
    @Throws(ValidationException::class)
    fun transactionsCanGetRetrievedAfterAddingThemToStore() {

        whenever(mockBlockChain!!.height).doReturn(20)
        mockkStatic(Burst::class)
        every { Burst.getBlockchain() } returns mockBlockChain

        for (i in 1..100) {
            val transaction = Transaction.Builder(1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, i.toLong(), FEE_QUANT * 100, timeService.epochTime + 50000, 500.toShort(), Attachment.ORDINARY_PAYMENT)
                    .id(i.toLong()).senderId(123L).build()
            transaction.sign(TestConstants.TEST_SECRET_PHRASE)
            t!!.put(transaction, null)
        }

        assertEquals(100, t!!.all.size)
        assertNotNull(t!!.get(1L))
        unmockkStatic(Burst::class)
    }


    @DisplayName("When a transaction got added by a peer, he won't get it reflected at him when getting unconfirmed transactions")
    @Test
    @Throws(ValidationException::class)
    fun transactionsGivenByPeerWontGetReturnedToPeer() {
        val mockPeer = mock<Peer>()
        val otherMockPeer = mock<Peer>()

        whenever(mockBlockChain!!.height).doReturn(20)
        mockkStatic(Burst::class)
        every { Burst.getBlockchain() } returns mockBlockChain

        for (i in 1..100) {
            val transaction = Transaction.Builder(1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, i.toLong(), FEE_QUANT * 100, timeService.epochTime + 50000, 500.toShort(), Attachment.ORDINARY_PAYMENT)
                    .id(i.toLong()).senderId(123L).build()
            transaction.sign(TestConstants.TEST_SECRET_PHRASE)
            t!!.put(transaction, mockPeer)
        }

        assertEquals(0, t!!.getAllFor(mockPeer).size)
        assertEquals(100, t!!.getAllFor(otherMockPeer).size)
        unmockkStatic(Burst::class)
    }

    @DisplayName("When a transactions got handed by a peer and we mark his fingerprints, he won't get it back a second time")
    @Test
    @Throws(ValidationException::class)
    fun transactionsMarkedWithPeerFingerPrintsWontGetReturnedToPeer() {
        val mockPeer = mock<Peer>()

        whenever(mockBlockChain!!.height).doReturn(20)
        mockkStatic(Burst::class)
        every { Burst.getBlockchain() } returns mockBlockChain

        for (i in 1..100) {
            val transaction = Transaction.Builder(1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, i.toLong(), FEE_QUANT * 100, timeService.epochTime + 50000, 500.toShort(), Attachment.ORDINARY_PAYMENT)
                    .id(i.toLong()).senderId(123L).build()
            transaction.sign(TestConstants.TEST_SECRET_PHRASE)
            t!!.put(transaction, null)
        }

        val mockPeerObtainedTransactions = t!!.getAllFor(mockPeer)
        assertEquals(100, mockPeerObtainedTransactions.size)

        t!!.markFingerPrintsOf(mockPeer, mockPeerObtainedTransactions)
        assertEquals(0, t!!.getAllFor(mockPeer).size)
        unmockkStatic(Burst::class)
    }


    @DisplayName("When The amount of unconfirmed transactions exceeds max size, and adding another then the cache size stays the same")
    @Test
    @Throws(ValidationException::class)
    fun numberOfUnconfirmedTransactionsOfSameSlotExceedsMaxSizeAddAnotherThenCacheSizeStaysMaxSize() {

        whenever(mockBlockChain!!.height).doReturn(20)
        mockkStatic(Burst::class)
        every { Burst.getBlockchain() } returns mockBlockChain

        for (i in 1..8192) {
            val transaction = Transaction.Builder(1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, i.toLong(), FEE_QUANT * 100, timeService.epochTime + 50000, 500.toShort(), Attachment.ORDINARY_PAYMENT)
                    .id(i.toLong()).senderId(123L).build()
            transaction.sign(TestConstants.TEST_SECRET_PHRASE)
            t!!.put(transaction, null)
        }

        assertEquals(8192, t!!.all.size)
        assertNotNull(t!!.get(1L))

        val oneTransactionTooMany = Transaction.Builder(1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 9999, FEE_QUANT * 100, timeService.epochTime + 50000, 500.toShort(), Attachment.ORDINARY_PAYMENT)
                .id(8193L).senderId(123L).build()
        oneTransactionTooMany.sign(TestConstants.TEST_SECRET_PHRASE)
        t!!.put(oneTransactionTooMany, null)

        assertEquals(8192, t!!.all.size)
        assertNull(t!!.get(1L))
        unmockkStatic(Burst::class)
    }

    @DisplayName("When the amount of unconfirmed transactions exceeds max size, and adding another of a higher slot, the cache size stays the same, and a lower slot transaction gets removed")
    @Test
    @Throws(ValidationException::class)
    fun numberOfUnconfirmedTransactionsOfSameSlotExceedsMaxSizeAddAnotherThenCacheSizeStaysMaxSizeAndLowerSlotTransactionGetsRemoved() {

        whenever(mockBlockChain!!.height).doReturn(20)
        mockkStatic(Burst::class)
        every { Burst.getBlockchain() } returns mockBlockChain

        for (i in 1..8192) {
            val transaction = Transaction.Builder(1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, i.toLong(), FEE_QUANT * 100, timeService.epochTime + 50000, 500.toShort(), Attachment.ORDINARY_PAYMENT)
                    .id(i.toLong()).senderId(123L).build()
            transaction.sign(TestConstants.TEST_SECRET_PHRASE)
            t!!.put(transaction, null)
        }

        assertEquals(8192, t!!.all.size)
        assertEquals(8192, t!!.all.stream().filter { t -> t.feeNQT == FEE_QUANT * 100 }.count())
        assertNotNull(t!!.get(1L))

        val oneTransactionTooMany = Transaction.Builder(1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 9999, FEE_QUANT * 200, timeService.epochTime + 50000, 500.toShort(), Attachment.ORDINARY_PAYMENT)
                .id(8193L).senderId(123L).build()
        oneTransactionTooMany.sign(TestConstants.TEST_SECRET_PHRASE)
        t!!.put(oneTransactionTooMany, null)

        assertEquals(8192, t!!.all.size)
        assertEquals((8192 - 1).toLong(), t!!.all.stream().filter { t -> t.feeNQT == FEE_QUANT * 100 }.count())
        assertEquals(1, t!!.all.stream().filter { t -> t.feeNQT == FEE_QUANT * 200 }.count())
        unmockkStatic(Burst::class)
    }

    @DisplayName("The unconfirmed transaction gets denied in case the account is unknown")
    @Test(expected = NotCurrentlyValidException::class)
    @Throws(ValidationException::class)
    fun unconfirmedTransactionGetsDeniedForUnknownAccount() {
        whenever(mockBlockChain!!.height).doReturn(20)
        mockkStatic(Burst::class)
        every { Burst.getBlockchain() } returns mockBlockChain

        val transaction = Transaction.Builder(1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 735000, timeService.epochTime + 50000, 500.toShort(), Attachment.ORDINARY_PAYMENT)
                .id(1).senderId(124L).build()
        transaction.sign(TestConstants.TEST_SECRET_PHRASE)
        t!!.put(transaction, null)
        unmockkStatic(Burst::class)
    }

    @DisplayName("The unconfirmed transaction gets denied in case the account does not have enough unconfirmed balance")
    @Test(expected = NotCurrentlyValidException::class)
    @Throws(ValidationException::class)
    fun unconfirmedTransactionGetsDeniedForNotEnoughUnconfirmedBalance() {
        whenever(mockBlockChain!!.height).doReturn(20)
        mockkStatic(Burst::class)
        every { Burst.getBlockchain() } returns mockBlockChain

        val transaction = Transaction.Builder(1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, Constants.MAX_BALANCE_NQT, timeService.epochTime + 50000, 500.toShort(), Attachment.ORDINARY_PAYMENT)
                .id(1).senderId(123L).build()
        transaction.sign(TestConstants.TEST_SECRET_PHRASE)

        try {
            t!!.put(transaction, null)
        } catch (ex: NotCurrentlyValidException) {
            assertTrue(t!!.all.isEmpty())
            throw ex
        }
        unmockkStatic(Burst::class)
    }

    @DisplayName("When adding the same unconfirmed transaction, nothing changes")
    @Test
    @Throws(ValidationException::class)
    fun addingNewUnconfirmedTransactionWithSameIDResultsInNothingChanging() {
        whenever(mockBlockChain!!.height).doReturn(20)
        mockkStatic(Burst::class)
        every { Burst.getBlockchain() } returns mockBlockChain

        val mockPeer = mock<Peer>()

        whenever(mockPeer.peerAddress).doReturn("mockPeer")

        val transactionBuilder = Builder(1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, Constants.MAX_BALANCE_NQT - 100000, timeService.epochTime + 50000,
                500.toShort(), Attachment.ORDINARY_PAYMENT)
                .id(1).senderId(123L)

        val transaction1 = transactionBuilder.build()
        transaction1.sign(TestConstants.TEST_SECRET_PHRASE)
        t!!.put(transaction1, mockPeer)

        val transaction2 = transactionBuilder.build()
        transaction2.sign(TestConstants.TEST_SECRET_PHRASE)

        t!!.put(transaction2, mockPeer)

        assertEquals(1, t!!.all.size)
        unmockkStatic(Burst::class)
    }

    @DisplayName("When the maximum number of transactions with full hash reference is reached, following ones are ignored")
    @Test
    @Throws(ValidationException::class)
    fun whenMaximumNumberOfTransactionsWithFullHashReferenceIsReachedFollowingOnesAreIgnored() {

        whenever(mockBlockChain!!.height).doReturn(20)
        mockkStatic(Burst::class)
        every { Burst.getBlockchain() } returns mockBlockChain

        for (i in 1..414) {
            val transaction = Transaction.Builder(1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, i.toLong(), FEE_QUANT * 2, timeService.epochTime + 50000, 500.toShort(), Attachment.ORDINARY_PAYMENT)
                    .id(i.toLong()).senderId(123L).referencedTransactionFullHash("b33f").build()
            transaction.sign(TestConstants.TEST_SECRET_PHRASE)
            t!!.put(transaction, null)
        }

        assertEquals(409, t!!.all.size)
        unmockkStatic(Burst::class)
    }

    @DisplayName("When the maximum number of transactions for a slot size is reached, following ones are ignored")
    @Test
    @Throws(ValidationException::class)
    fun whenMaximumNumberOfTransactionsForSlotSizeIsReachedFollowingOnesAreIgnored() {

        whenever(mockBlockChain!!.height).doReturn(20)
        mockkStatic(Burst::class)
        every { Burst.getBlockchain() } returns mockBlockChain

        for (i in 1..365) {
            val transaction = Transaction.Builder(1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, i.toLong(), FEE_QUANT, timeService.epochTime + 50000, 500.toShort(), Attachment.ORDINARY_PAYMENT)
                    .id(i.toLong()).senderId(123L).build()
            transaction.sign(TestConstants.TEST_SECRET_PHRASE)
            t!!.put(transaction, null)
        }

        assertEquals(360, t!!.all.size)

        for (i in 1..725) {
            val transaction = Transaction.Builder(1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, i.toLong(), FEE_QUANT * 2, timeService.epochTime + 50000, 500.toShort(), Attachment.ORDINARY_PAYMENT)
                    .id(i.toLong()).senderId(123L).build()
            transaction.sign(TestConstants.TEST_SECRET_PHRASE)
            t!!.put(transaction, null)
        }

        assertEquals(1080, t!!.all.size)
        unmockkStatic(Burst::class)
    }

    @Test
    @Throws(ValidationException::class)
    fun cheaperDuplicateTransactionGetsRemoved() {
        val fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.PRE_DYMAXION, FluxValues.DIGITAL_GOODS_STORE)
        mockkStatic(Burst::class)
        every { Burst.getBlockchain() } returns mockBlockChain
        every { Burst.getFluxCapacitor() } returns fluxCapacitor
        val cheap = Transaction.Builder(1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, FEE_QUANT, timeService.epochTime + 50000, 500.toShort(),
                MessagingAliasSell("aliasName", 123, 5))
                .id(1).senderId(123L).build()

        val expensive = Transaction.Builder(1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, FEE_QUANT * 2, timeService.epochTime + 50000, 500.toShort(),
                MessagingAliasSell("aliasName", 123, 5))
                .id(2).senderId(123L).build()

        t!!.put(cheap, null)

        assertEquals(1, t!!.all.size)
        assertNotNull(t!!.get(cheap.id))

        t!!.put(expensive, null)

        assertEquals(1, t!!.all.size)
        assertNull(t!!.get(cheap.id))
        assertNotNull(t!!.get(expensive.id))
        unmockkStatic(Burst::class)
    }

    @Test
    @Throws(ValidationException::class)
    fun cheaperDuplicateTransactionNeverGetsAdded() {
        val fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.PRE_DYMAXION, FluxValues.DIGITAL_GOODS_STORE)
        mockkStatic(Burst::class)
        every { Burst.getBlockchain() } returns mockBlockChain
        every { Burst.getFluxCapacitor() } returns fluxCapacitor
        val cheap = Transaction.Builder(1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, FEE_QUANT, timeService.epochTime + 50000, 500.toShort(),
                MessagingAliasSell("aliasName", 123, 5))
                .id(1).senderId(123L).build()

        val expensive = Transaction.Builder(1.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, FEE_QUANT * 2, timeService.epochTime + 50000, 500.toShort(),
                MessagingAliasSell("aliasName", 123, 5))
                .id(2).senderId(123L).build()

        t!!.put(expensive, null)

        assertEquals(1, t!!.all.size)
        assertNotNull(t!!.get(expensive.id))

        t!!.put(cheap, null)

        assertEquals(1, t!!.all.size)
        assertNull(t!!.get(cheap.id))
        assertNotNull(t!!.get(expensive.id))
        unmockkStatic(Burst::class)
    }

}
