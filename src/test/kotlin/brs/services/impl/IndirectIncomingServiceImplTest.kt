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
import com.nhaarman.mockitokotlin2.*
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
        val propertyService = mock<PropertyService>()
        val indirectIncomingStore = mock<IndirectIncomingStore>()
        doAnswer { invocation ->
            addIndirectIncomingsRunnable(invocation.getArgument(0))
            null
        }.whenever(indirectIncomingStore).addIndirectIncomings(any())
        indirectIncomingService = IndirectIncomingServiceImpl(QuickMocker.dependencyProvider(indirectIncomingStore, propertyService))
        dp = QuickMocker.dependencyProvider(indirectIncomingService, indirectIncomingStore, propertyService)
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
        val attachment = mock<Attachment.PaymentMultiOutCreation> {
            on { getRecipients() } doReturn recipients
        }
        val multiOut = mock<Transaction> {
            on { it.type } doReturn MultiOutPayment(dp)
            on { it.attachment } doReturn attachment
        }
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
        val attachment = mock<Attachment.PaymentMultiSameOutCreation>()
        whenever(attachment.getRecipients()).doReturn(recipients)
        val multiOutSame = mock<Transaction>()
        whenever(multiOutSame.type).doReturn(MultiOutSamePayment(dp))
        whenever(multiOutSame.attachment).doReturn(attachment)
        indirectIncomingService.processTransaction(multiOutSame)
    }

    @Test
    fun testIndirectIncomingServiceImplTestOrdinaryTransaction() {
        addIndirectIncomingsRunnable = { indirectIncomings -> assertEquals(0, indirectIncomings.size.toLong()) }
        val ordinaryTransaction = mock<Transaction>()
        whenever(ordinaryTransaction.attachment).doReturn(Attachment.OrdinaryPayment(dp))
        whenever(ordinaryTransaction.type).doReturn(OrdinaryPayment(dp))
        indirectIncomingService.processTransaction(ordinaryTransaction)
    }
}
