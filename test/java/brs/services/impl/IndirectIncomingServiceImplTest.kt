package brs.services.impl

import brs.*
import brs.db.store.IndirectIncomingStore
import brs.props.PropertyService
import brs.props.Props
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import java.util.ArrayList
import java.util.Arrays
import java.util.HashSet
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

import org.junit.Assert.assertEquals

@RunWith(JUnit4::class)
class IndirectIncomingServiceImplTest {
    private val addIndirectIncomingsRunnable = AtomicReference<Consumer<List<IndirectIncomingStore.IndirectIncoming>>>()
    private var indirectIncomingService: IndirectIncomingServiceImpl? = null

    @Before
    fun setUpIndirectIncomingServiceImplTest() {
        val propertyService = mock<PropertyService>()
        val indirectIncomingStore = mock<IndirectIncomingStore>()
        whenever(propertyService.get(Props.INDIRECT_INCOMING_SERVICE_ENABLE)).thenReturn(true)
        doAnswer { invocation ->
            addIndirectIncomingsRunnable.get().accept(invocation.getArgument(0))
            null
        }.whenever(indirectIncomingStore).addIndirectIncomings(any())
        indirectIncomingService = IndirectIncomingServiceImpl(indirectIncomingStore, propertyService)
    }

    @Test
    @Throws(BurstException.NotValidException::class)
    fun testIndirectIncomingServiceImplTestMultiOutTransaction() {
        addIndirectIncomingsRunnable.set(Consumer { indirectIncomings ->
            assertEquals(4, indirectIncomings.size.toLong())
            assertEquals(HashSet(indirectIncomings).size.toLong(), indirectIncomings.size.toLong()) // Assert that there are no duplicates
        })
        val recipients = ArrayList<List<Long>>()
        recipients.add(listOf(1L, Constants.ONE_BURST))
        recipients.add(listOf(2L, Constants.ONE_BURST))
        recipients.add(listOf(3L, Constants.ONE_BURST))
        recipients.add(listOf(4L, Constants.ONE_BURST))
        val attachment = mock<Attachment.PaymentMultiOutCreation> {
            on { getRecipients() } doReturn recipients
        }
        val multiOut = mock<Transaction> {
            on { type } doReturn TransactionType.Payment.MULTI_OUT
            on { it.attachment } doReturn attachment
        }
        indirectIncomingService!!.processTransaction(multiOut)
    }

    @Test
    @Throws(BurstException.NotValidException::class)
    fun testIndirectIncomingServiceImplTestMultiOutSameTransaction() {
        addIndirectIncomingsRunnable.set(Consumer { indirectIncomings ->
            assertEquals(4, indirectIncomings.size.toLong())
            assertEquals(HashSet(indirectIncomings).size.toLong(), indirectIncomings.size.toLong()) // Assert that there are no duplicates
        })
        val recipients = ArrayList<Long>()
        recipients.add(1L)
        recipients.add(2L)
        recipients.add(3L)
        recipients.add(4L)
        val attachment = mock<Attachment.PaymentMultiSameOutCreation>()
        whenever(attachment.getRecipients()).thenReturn(recipients)
        val multiOutSame = mock<Transaction>()
        whenever(multiOutSame.type).thenReturn(TransactionType.Payment.MULTI_SAME_OUT)
        whenever(multiOutSame.attachment).thenReturn(attachment)
        indirectIncomingService!!.processTransaction(multiOutSame)
    }

    @Test
    fun testIndirectIncomingServiceImplTestOrdinaryTransaction() {
        addIndirectIncomingsRunnable.set(Consumer { indirectIncomings -> assertEquals(0, indirectIncomings.size.toLong()) })
        val ordinaryTransaction = mock<Transaction>()
        whenever(ordinaryTransaction.attachment).thenReturn(Attachment.ORDINARY_PAYMENT)
        whenever(ordinaryTransaction.type).thenReturn(TransactionType.Payment.ORDINARY)
        indirectIncomingService!!.processTransaction(ordinaryTransaction)
    }
}
