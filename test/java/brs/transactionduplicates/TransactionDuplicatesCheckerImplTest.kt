package brs.transactionduplicates

import brs.Attachment.AdvancedPaymentEscrowResult
import brs.Attachment.AdvancedPaymentSubscriptionSubscribe
import brs.Attachment.MessagingAliasSell
import brs.BlockchainImpl
import brs.Burst
import brs.BurstException.NotValidException
import brs.Escrow.DecisionType
import brs.Transaction
import brs.TransactionType
import brs.common.QuickMocker
import brs.common.TestConstants
import brs.fluxcapacitor.FluxValues
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TransactionDuplicatesCheckerImplTest {

    private var t = TransactionDuplicatesCheckerImpl()

    @Before
    fun setUp() {
        mockkStatic(Burst::class)
        val mockFluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.PRE_DYMAXION)
        val mockBlockchain = mock<BlockchainImpl>()
        whenever(mockBlockchain.height).thenReturn(4)
        every { Burst.getBlockchain() } returns mockBlockchain

        TransactionType.init(mockBlockchain, mockFluxCapacitor, null, null, null, null, null, null)

        t = TransactionDuplicatesCheckerImpl()
    }

    @DisplayName("First transaction is never a duplicate when checking for any duplicate")
    @Test
    @Throws(NotValidException::class)
    fun firstTransactionIsNeverADuplicateWhenCheckingForAnyDuplicate() {
        val transaction = Transaction.Builder(0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                MessagingAliasSell("aliasName", 123, 5))
                .id(1).senderId(123L).build()

        assertFalse(t.hasAnyDuplicate(transaction))
    }

    @DisplayName("Adding same transaction twice counts as a duplicate")
    @Test
    @Throws(NotValidException::class)
    fun addingSameTransactionTwiceCountsAsADuplicate() {
        val transaction = Transaction.Builder(0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                MessagingAliasSell("aliasName", 123, 5))
                .id(1).senderId(123L).build()

        assertFalse(t.hasAnyDuplicate(transaction))
        assertTrue(t.hasAnyDuplicate(transaction))
    }


    @DisplayName("Duplicate transaction is duplicate when checking for any duplicate")
    @Test
    @Throws(NotValidException::class)
    fun duplicateTransactionIsDuplicateWhenCheckingForAnyDuplicate() {
        val transaction = Transaction.Builder(0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                MessagingAliasSell("aliasName", 123, 5))
                .id(1).senderId(123L).build()

        val duplicate = Transaction.Builder(0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                MessagingAliasSell("aliasName", 123, 5))
                .id(2).senderId(345L).build()

        assertFalse(t.hasAnyDuplicate(transaction))
        assertTrue(t.hasAnyDuplicate(duplicate))
    }

    @DisplayName("Duplicate transaction removes cheaper duplicate when checking for cheapest duplicate")
    @Test
    @Throws(NotValidException::class)
    fun duplicateTransactionRemovesCheaperDuplicateWhenCheckingForCheapestDuplicate() {
        val cheaper = Transaction.Builder(0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 999999, 50000, 500.toShort(),
                MessagingAliasSell("aliasName", 123, 5))
                .id(1).senderId(123L).build()

        val moreExpensive = Transaction.Builder(0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 999999999, 50000, 500.toShort(),
                MessagingAliasSell("aliasName", 123, 5))
                .id(2).senderId(345L).build()

        val hasCheaperFirst = t.removeCheaperDuplicate(cheaper)
        assertFalse(hasCheaperFirst.duplicate)
        assertNull(hasCheaperFirst.transaction)

        val hasCheaperSecond = t.removeCheaperDuplicate(moreExpensive)
        assertTrue(hasCheaperSecond.duplicate)
        assertNotNull(hasCheaperSecond.transaction)
        assertEquals(cheaper, hasCheaperSecond.transaction)

        val hasCheaperThird = t.removeCheaperDuplicate(cheaper)
        assertTrue(hasCheaperThird.duplicate)
        assertNotNull(hasCheaperThird.transaction)
        assertEquals(cheaper, hasCheaperThird.transaction)
    }

    @DisplayName("Some transactions are always a duplicate")
    @Test
    @Throws(NotValidException::class)
    fun someTransactionsAreAlwaysADuplicate() {
        val transaction = Transaction.Builder(0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                AdvancedPaymentEscrowResult(123L, DecisionType.REFUND, 5))
                .id(1).senderId(123L).build()

        assertTrue(t.hasAnyDuplicate(transaction))
        assertTrue(t.removeCheaperDuplicate(transaction).duplicate)
    }


    @DisplayName("Some transaction are never a duplicate")
    @Test
    @Throws(NotValidException::class)
    fun someTransactionsAreNeverADuplicate() {
        val transaction = Transaction.Builder(0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                AdvancedPaymentSubscriptionSubscribe(123, 5))
                .id(1).senderId(123L).build()

        assertFalse(t.hasAnyDuplicate(transaction))
        assertFalse(t.hasAnyDuplicate(transaction))
        assertFalse(t.removeCheaperDuplicate(transaction).duplicate)
    }

    @DisplayName("Removing transaction makes it not a duplicate anymore")
    @Test
    @Throws(NotValidException::class)
    fun removingTransactionMakesItNotADuplicateAnymore() {
        val transaction = Transaction.Builder(0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                MessagingAliasSell("aliasName", 123, 5))
                .id(1).senderId(123L).build()

        val duplicate = Transaction.Builder(0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                MessagingAliasSell("aliasName", 123, 5))
                .id(2).senderId(345L).build()

        assertFalse(t.hasAnyDuplicate(transaction))
        assertTrue(t.hasAnyDuplicate(duplicate))

        t.removeTransaction(transaction)

        assertFalse(t.hasAnyDuplicate(duplicate))
    }

    @DisplayName("Clearing removes all transactions")
    @Test
    @Throws(NotValidException::class)
    fun clearingRemovesAllTransactions() {
        val transaction = Transaction.Builder(0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                MessagingAliasSell("aliasName", 123, 5))
                .id(1).senderId(123L).build()

        val duplicate = Transaction.Builder(0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                MessagingAliasSell("aliasName", 123, 5))
                .id(2).senderId(345L).build()

        assertFalse(t.hasAnyDuplicate(transaction))
        assertTrue(t.hasAnyDuplicate(duplicate))

        t.clear()

        assertFalse(t.hasAnyDuplicate(duplicate))
    }
}
