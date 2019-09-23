package brs.transactionduplicates

import brs.Attachment.*
import brs.BlockchainImpl
import brs.DependencyProvider
import brs.Escrow.DecisionType
import brs.Transaction
import brs.common.QuickMocker
import brs.common.TestConstants
import brs.fluxcapacitor.FluxValues
import brs.transaction.TransactionType
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TransactionDuplicatesCheckerImplTest {

    private var t = TransactionDuplicatesCheckerImpl()
    private lateinit var dp: DependencyProvider

    @Before
    fun setUp() {
        val mockFluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.PRE_DYMAXION)
        val mockBlockchain = mock<BlockchainImpl>()
        whenever(mockBlockchain.height).doReturn(4)

        dp = QuickMocker.dependencyProvider(mockBlockchain, mockFluxCapacitor)

        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        t = TransactionDuplicatesCheckerImpl()
    }

    @DisplayName("First transaction is never a duplicate when checking for any duplicate")
    @Test
    fun firstTransactionIsNeverADuplicateWhenCheckingForAnyDuplicate() {
        val transaction = Transaction.Builder(dp, 0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                MessagingAliasSell(dp, "aliasName", 123, 5))
                .id(1).senderId(123L).build()

        assertFalse(t.hasAnyDuplicate(transaction))
    }

    @DisplayName("Adding same transaction twice counts as a duplicate")
    @Test
    fun addingSameTransactionTwiceCountsAsADuplicate() {
        val transaction = Transaction.Builder(dp, 0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                MessagingAliasSell(dp, "aliasName", 123, 5))
                .id(1).senderId(123L).build()

        assertFalse(t.hasAnyDuplicate(transaction))
        assertTrue(t.hasAnyDuplicate(transaction))
    }


    @DisplayName("Duplicate transaction is duplicate when checking for any duplicate")
    @Test
    fun duplicateTransactionIsDuplicateWhenCheckingForAnyDuplicate() {
        val transaction = Transaction.Builder(dp, 0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                MessagingAliasSell(dp, "aliasName", 123, 5))
                .id(1).senderId(123L).build()

        val duplicate = Transaction.Builder(dp, 0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                MessagingAliasSell(dp, "aliasName", 123, 5))
                .id(2).senderId(345L).build()

        assertFalse(t.hasAnyDuplicate(transaction))
        assertTrue(t.hasAnyDuplicate(duplicate))
    }

    @DisplayName("Duplicate transaction removes cheaper duplicate when checking for cheapest duplicate")
    @Test
    fun duplicateTransactionRemovesCheaperDuplicateWhenCheckingForCheapestDuplicate() {
        val cheaper = Transaction.Builder(dp, 0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 999999, 50000, 500.toShort(),
                MessagingAliasSell(dp, "aliasName", 123, 5))
                .id(1).senderId(123L).build()

        val moreExpensive = Transaction.Builder(dp, 0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 999999999, 50000, 500.toShort(),
                MessagingAliasSell(dp, "aliasName", 123, 5))
                .id(2).senderId(345L).build()

        val hasCheaperFirst = t.removeCheaperDuplicate(cheaper)
        assertFalse(hasCheaperFirst.isDuplicate)
        assertNull(hasCheaperFirst.transaction)

        val hasCheaperSecond = t.removeCheaperDuplicate(moreExpensive)
        assertTrue(hasCheaperSecond.isDuplicate)
        assertNotNull(hasCheaperSecond.transaction)
        assertEquals(cheaper, hasCheaperSecond.transaction)

        val hasCheaperThird = t.removeCheaperDuplicate(cheaper)
        assertTrue(hasCheaperThird.isDuplicate)
        assertNotNull(hasCheaperThird.transaction)
        assertEquals(cheaper, hasCheaperThird.transaction)
    }

    @DisplayName("Some transactions are always a duplicate")
    @Test
    fun someTransactionsAreAlwaysADuplicate() {
        val transaction = Transaction.Builder(dp, 0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                AdvancedPaymentEscrowResult(dp, 123L, DecisionType.REFUND, 5))
                .id(1).senderId(123L).build()

        assertTrue(t.hasAnyDuplicate(transaction))
        assertTrue(t.removeCheaperDuplicate(transaction).isDuplicate)
    }


    @DisplayName("Some transaction are never a duplicate")
    @Test
    fun someTransactionsAreNeverADuplicate() {
        val transaction = Transaction.Builder(dp, 0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                AdvancedPaymentSubscriptionSubscribe(dp, 123, 5))
                .id(1).senderId(123L).build()

        assertFalse(t.hasAnyDuplicate(transaction))
        assertFalse(t.hasAnyDuplicate(transaction))
        assertFalse(t.removeCheaperDuplicate(transaction).isDuplicate)
    }

    @DisplayName("Removing transaction makes it not a duplicate anymore")
    @Test
    fun removingTransactionMakesItNotADuplicateAnymore() {
        val transaction = Transaction.Builder(dp, 0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                MessagingAliasSell(dp, "aliasName", 123, 5))
                .id(1).senderId(123L).build()

        val duplicate = Transaction.Builder(dp, 0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                MessagingAliasSell(dp, "aliasName", 123, 5))
                .id(2).senderId(345L).build()

        assertFalse(t.hasAnyDuplicate(transaction))
        assertTrue(t.hasAnyDuplicate(duplicate))

        t.removeTransaction(transaction)

        assertFalse(t.hasAnyDuplicate(duplicate))
    }

    @DisplayName("Clearing removes all transactions")
    @Test
    fun clearingRemovesAllTransactions() {
        val transaction = Transaction.Builder(dp, 0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                MessagingAliasSell(dp, "aliasName", 123, 5))
                .id(1).senderId(123L).build()

        val duplicate = Transaction.Builder(dp, 0.toByte(), TestConstants.TEST_PUBLIC_KEY_BYTES, 1, 99999999, 50000, 500.toShort(),
                MessagingAliasSell(dp, "aliasName", 123, 5))
                .id(2).senderId(345L).build()

        assertFalse(t.hasAnyDuplicate(transaction))
        assertTrue(t.hasAnyDuplicate(duplicate))

        t.clear()

        assertFalse(t.hasAnyDuplicate(duplicate))
    }
}
