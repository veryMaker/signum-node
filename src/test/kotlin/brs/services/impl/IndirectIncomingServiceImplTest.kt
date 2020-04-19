package brs.services.impl

import brs.common.QuickMocker
import brs.db.IndirectIncomingStore
import brs.entity.DependencyProvider
import brs.entity.IndirectIncoming
import brs.entity.Transaction
import brs.objects.Constants
import brs.services.PropertyService
import brs.transaction.appendix.Attachment
import brs.transaction.type.payment.MultiOutPayment
import brs.transaction.type.payment.MultiOutSamePayment
import brs.transaction.type.payment.OrdinaryPayment
import brs.util.delegates.AtomicLateinit
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class IndirectIncomingServiceImplTest {
    private var addIndirectIncomingsRunnable by AtomicLateinit<(List<IndirectIncoming>) -> Unit>()
    private lateinit var indirectIncomingService: IndirectIncomingServiceImpl
    private lateinit var dp: DependencyProvider

    @Before
    fun setUpIndirectIncomingServiceImplTest() {
        val propertyService = mockk<PropertyService>(relaxed = true)
        val indirectIncomingStore = mockk<IndirectIncomingStore>(relaxed = true)
        @Suppress("UNCHECKED_CAST")
        every { indirectIncomingStore.addIndirectIncomings(any()) } answers { addIndirectIncomingsRunnable(args[0] as List<IndirectIncoming>) }
        dp = QuickMocker.dependencyProvider(QuickMocker.mockDb(indirectIncomingStore), propertyService)
        indirectIncomingService = IndirectIncomingServiceImpl(dp)
        dp.indirectIncomingService = indirectIncomingService
    }

    @Test
    fun testIndirectIncomingServiceImplTestMultiOutTransaction() {
        addIndirectIncomingsRunnable = { indirectIncomings ->
            assertEquals(4, indirectIncomings.size.toLong())
            assertEquals(indirectIncomings.toSet().size.toLong(), indirectIncomings.size.toLong()) // Assert that there are no duplicates
        }
        val recipients = mutableListOf<List<Long>>()
        recipients.add(listOf(1L, Constants.ONE_BURST))
        recipients.add(listOf(2L, Constants.ONE_BURST))
        recipients.add(listOf(3L, Constants.ONE_BURST))
        recipients.add(listOf(4L, Constants.ONE_BURST))
        val attachment = mockk<Attachment.PaymentMultiOutCreation> {
            every { getRecipients() } returns recipients
        }
        val multiOut = mockk<Transaction>(relaxed = true)
        every { multiOut.type } returns MultiOutPayment(dp)
        every { multiOut.attachment } returns attachment
        indirectIncomingService.processTransaction(multiOut)
    }

    @Test
    fun testIndirectIncomingServiceImplTestMultiOutSameTransaction() {
        addIndirectIncomingsRunnable = { indirectIncomings ->
            assertEquals(4, indirectIncomings.size.toLong())
            assertEquals(indirectIncomings.toSet().size.toLong(), indirectIncomings.size.toLong()) // Assert that there are no duplicates
        }
        val recipients = mutableListOf<Long>()
        recipients.add(1L)
        recipients.add(2L)
        recipients.add(3L)
        recipients.add(4L)
        val attachment = mockk<Attachment.PaymentMultiSameOutCreation>(relaxed = true)
        every { attachment.getRecipients() } returns recipients
        val multiOutSame = mockk<Transaction>(relaxed = true)
        every { multiOutSame.type } returns MultiOutSamePayment(dp)
        every { multiOutSame.attachment } returns attachment
        indirectIncomingService.processTransaction(multiOutSame)
    }

    @Test
    fun testIndirectIncomingServiceImplTestOrdinaryTransaction() {
        addIndirectIncomingsRunnable = { indirectIncomings -> assertEquals(0, indirectIncomings.size.toLong()) }
        val ordinaryTransaction = mockk<Transaction>(relaxed = true)
        every { ordinaryTransaction.type } answers { OrdinaryPayment(dp) }
        every { ordinaryTransaction.attachment } returns Attachment.OrdinaryPayment(dp)
        indirectIncomingService.processTransaction(ordinaryTransaction)
    }
}
