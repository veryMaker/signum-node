package brs.http

import brs.*
import brs.Escrow.DecisionType
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxCapacitor
import brs.fluxcapacitor.FluxValues
import brs.services.EscrowService
import brs.services.ParameterService
import brs.util.JSON
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import javax.servlet.http.HttpServletRequest

import brs.TransactionType.AdvancedPayment.ESCROW_SIGN
import brs.http.common.Parameters.DECISION_PARAMETER
import brs.http.common.Parameters.ESCROW_PARAMETER
import brs.http.common.ResultFields.ERROR_CODE_RESPONSE
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class EscrowSignTest : AbstractTransactionTest() {

    private var parameterServiceMock: ParameterService? = null
    private var blockchainMock: Blockchain? = null
    private var escrowServiceMock: EscrowService? = null
    private var apiTransactionManagerMock: APITransactionManager? = null

    private var t: EscrowSign? = null

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        blockchainMock = mock<Blockchain>()
        escrowServiceMock = mock<EscrowService>()
        apiTransactionManagerMock = mock<APITransactionManager>()

        t = EscrowSign(parameterServiceMock!!, blockchainMock!!, escrowServiceMock!!, apiTransactionManagerMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_positiveAsEscrowSender() {
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

        QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)

        whenever(escrowServiceMock!!.getEscrowTransaction(eq(escrowId))).doReturn(escrow)
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(sender)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) },
                apiTransactionManagerMock!!) as Attachment.AdvancedPaymentEscrowSign
        assertNotNull(attachment)

        assertEquals(ESCROW_SIGN, attachment.transactionType)
        assertEquals(DecisionType.RELEASE, attachment.decision)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_positiveAsEscrowRecipient() {
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

        QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) },
                apiTransactionManagerMock!!) as Attachment.AdvancedPaymentEscrowSign
        assertNotNull(attachment)

        assertEquals(ESCROW_SIGN, attachment.transactionType)
        assertEquals(DecisionType.REFUND, attachment.decision)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_positiveAsEscrowSigner() {
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

        QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)

        whenever(escrowServiceMock!!.isIdSigner(eq(senderId), eq(escrow))).doReturn(true)

        whenever(escrowServiceMock!!.getEscrowTransaction(eq(escrowId))).doReturn(escrow)
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(sender)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) },
                apiTransactionManagerMock!!) as Attachment.AdvancedPaymentEscrowSign
        assertNotNull(attachment)

        assertEquals(ESCROW_SIGN, attachment.transactionType)
        assertEquals(DecisionType.REFUND, attachment.decision)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_invalidEscrowId() {
        val request = QuickMocker.httpServletRequest(
                MockParam(ESCROW_PARAMETER, "NotANumber")
        )

        val result = t!!.processRequest(request) as JsonObject

        assertEquals(3, JSON.getAsInt(result.get(ERROR_CODE_RESPONSE)).toLong())
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_escrowNotFound() {
        val escrowId: Long = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(ESCROW_PARAMETER, escrowId)
        )

        whenever(escrowServiceMock!!.getEscrowTransaction(eq(escrowId))).doReturn(null)

        val result = t!!.processRequest(request) as JsonObject

        assertEquals(5, JSON.getAsInt(result.get(ERROR_CODE_RESPONSE)).toLong())
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_invalidDecisionType() {
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
    @Throws(BurstException::class)
    fun processRequest_invalidSender() {
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
    @Throws(BurstException::class)
    fun processRequest_senderCanOnlyRelease() {
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
    @Throws(BurstException::class)
    fun processRequest_recipientCanOnlyRefund() {
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
