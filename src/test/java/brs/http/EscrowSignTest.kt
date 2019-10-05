package brs.http

import brs.*
import brs.Escrow.DecisionType
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxValues
import brs.http.common.Parameters.DECISION_PARAMETER
import brs.http.common.Parameters.ESCROW_PARAMETER
import brs.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.services.EscrowService
import brs.services.ParameterService
import brs.transaction.TransactionType
import brs.util.JSON
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import javax.servlet.http.HttpServletRequest

@RunWith(JUnit4::class)
class EscrowSignTest : AbstractTransactionTest() {

    private lateinit var parameterServiceMock: ParameterService
    private lateinit var blockchainMock: Blockchain
    private lateinit var escrowServiceMock: EscrowService
    private lateinit var apiTransactionManagerMock: APITransactionManager
    private lateinit var dp: DependencyProvider
    private lateinit var t: EscrowSign

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        blockchainMock = mock<Blockchain>()
        escrowServiceMock = mock<EscrowService>()
        apiTransactionManagerMock = mock<APITransactionManager>()
        dp = QuickMocker.dependencyProvider(parameterServiceMock!!, blockchainMock!!, escrowServiceMock!!, apiTransactionManagerMock!!)
        t = EscrowSign(dp)
    }

    @Test
    fun processRequest_positiveAsEscrowSender() = runBlocking {
        val escrowId: Long = 5
        val senderId: Long = 6

        val request = QuickMocker.httpServletRequest(
                MockParam(ESCROW_PARAMETER, escrowId),
                MockParam(DECISION_PARAMETER, "release")
        )

        val escrow = mock<Escrow>()
        whenever(escrow.senderId).doReturn(senderId)
        whenever(escrow.recipientId).doReturn(2L)

        val sender = mock<Account>()
        whenever(sender.id).doReturn(senderId)
        dp.fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        whenever(escrowServiceMock!!.getEscrowTransaction(eq(escrowId))).doReturn(escrow)
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(sender)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) },
                apiTransactionManagerMock!!) as Attachment.AdvancedPaymentEscrowSign
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is brs.transaction.advancedPayment.EscrowSign)
        assertEquals(DecisionType.RELEASE, attachment.decision)
    }

    @Test
    fun processRequest_positiveAsEscrowRecipient() = runBlocking {
        val escrowId: Long = 5
        val senderId: Long = 6

        val request = QuickMocker.httpServletRequest(
                MockParam(ESCROW_PARAMETER, escrowId),
                MockParam(DECISION_PARAMETER, "refund")
        )

        val escrow = mock<Escrow>()
        whenever(escrow.senderId).doReturn(1L)
        whenever(escrow.recipientId).doReturn(senderId)

        val sender = mock<Account>()
        whenever(sender.id).doReturn(senderId)

        whenever(escrowServiceMock!!.getEscrowTransaction(eq(escrowId))).doReturn(escrow)
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(sender)
        dp.fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) },
                apiTransactionManagerMock!!) as Attachment.AdvancedPaymentEscrowSign
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is brs.transaction.advancedPayment.EscrowSign)
        assertEquals(DecisionType.REFUND, attachment.decision)
    }

    @Test
    fun processRequest_positiveAsEscrowSigner() = runBlocking {
        val escrowId: Long = 5
        val senderId: Long = 6

        val request = QuickMocker.httpServletRequest(
                MockParam(ESCROW_PARAMETER, escrowId),
                MockParam(DECISION_PARAMETER, "refund")
        )

        val escrow = mock<Escrow>()
        whenever(escrow.recipientId).doReturn(1L)
        whenever(escrow.senderId).doReturn(2L)

        val sender = mock<Account>()
        whenever(sender.id).doReturn(senderId)
        dp.fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        whenever(escrowServiceMock!!.isIdSigner(eq(senderId), eq(escrow))).doReturn(true)

        whenever(escrowServiceMock!!.getEscrowTransaction(eq(escrowId))).doReturn(escrow)
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(sender)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) },
                apiTransactionManagerMock!!) as Attachment.AdvancedPaymentEscrowSign
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is brs.transaction.advancedPayment.EscrowSign)
        assertEquals(DecisionType.REFUND, attachment.decision)
    }

    @Test
    fun processRequest_invalidEscrowId() = runBlocking {
        val request = QuickMocker.httpServletRequest(
                MockParam(ESCROW_PARAMETER, "NotANumber")
        )

        val result = t!!.processRequest(request) as JsonObject

        assertEquals(3, JSON.getAsInt(result.get(ERROR_CODE_RESPONSE)).toLong())
    }

    @Test
    fun processRequest_escrowNotFound() = runBlocking {
        val escrowId: Long = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(ESCROW_PARAMETER, escrowId)
        )

        whenever(escrowServiceMock!!.getEscrowTransaction(eq(escrowId))).doReturn(null)

        val result = t!!.processRequest(request) as JsonObject

        assertEquals(5, JSON.getAsInt(result.get(ERROR_CODE_RESPONSE)).toLong())
    }

    @Test
    fun processRequest_invalidDecisionType() = runBlocking {
        val escrowId: Long = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(ESCROW_PARAMETER, escrowId),
                MockParam(DECISION_PARAMETER, "notADecisionValue")
        )

        val escrow = mock<Escrow>()

        whenever(escrowServiceMock!!.getEscrowTransaction(eq(escrowId))).doReturn(escrow)

        val result = t!!.processRequest(request) as JsonObject

        assertEquals(5, JSON.getAsInt(result.get(ERROR_CODE_RESPONSE)).toLong())
    }

    @Test
    fun processRequest_invalidSender() = runBlocking {
        val escrowId: Long = 5
        val senderId: Long = 6

        val request = QuickMocker.httpServletRequest(
                MockParam(ESCROW_PARAMETER, escrowId),
                MockParam(DECISION_PARAMETER, "refund")
        )

        val escrow = mock<Escrow>()
        whenever(escrow.senderId).doReturn(1L)
        whenever(escrow.recipientId).doReturn(2L)

        whenever(escrowServiceMock!!.isIdSigner(eq(senderId), eq(escrow))).doReturn(false)

        val sender = mock<Account>()
        whenever(sender.id).doReturn(senderId)

        whenever(escrowServiceMock!!.getEscrowTransaction(eq(escrowId))).doReturn(escrow)
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(sender)

        val result = t!!.processRequest(request) as JsonObject

        assertEquals(5, JSON.getAsInt(result.get(ERROR_CODE_RESPONSE)).toLong())
    }

    @Test
    fun processRequest_senderCanOnlyRelease() = runBlocking {
        val escrowId: Long = 5
        val senderId: Long = 6

        val request = QuickMocker.httpServletRequest(
                MockParam(ESCROW_PARAMETER, escrowId),
                MockParam(DECISION_PARAMETER, "refund")
        )

        val escrow = mock<Escrow>()
        whenever(escrow.senderId).doReturn(senderId)

        val sender = mock<Account>()
        whenever(sender.id).doReturn(senderId)

        whenever(escrowServiceMock!!.getEscrowTransaction(eq(escrowId))).doReturn(escrow)
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(sender)

        val result = t!!.processRequest(request) as JsonObject

        assertEquals(4, JSON.getAsInt(result.get(ERROR_CODE_RESPONSE)).toLong())
    }

    @Test
    fun processRequest_recipientCanOnlyRefund() = runBlocking {
        val escrowId: Long = 5
        val senderId: Long = 6

        val request = QuickMocker.httpServletRequest(
                MockParam(ESCROW_PARAMETER, escrowId),
                MockParam(DECISION_PARAMETER, "release")
        )

        val escrow = mock<Escrow>()
        whenever(escrow.recipientId).doReturn(senderId)

        val sender = mock<Account>()
        whenever(sender.id).doReturn(senderId)

        whenever(escrowServiceMock!!.getEscrowTransaction(eq(escrowId))).doReturn(escrow)
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(sender)

        val result = t!!.processRequest(request) as JsonObject

        assertEquals(4, JSON.getAsInt(result.get(ERROR_CODE_RESPONSE)).toLong())
    }
}
