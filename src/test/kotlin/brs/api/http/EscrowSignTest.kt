package brs.api.http

import brs.entity.Escrow.DecisionType
import brs.entity.Account
import brs.services.BlockchainService
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.objects.FluxValues
import brs.api.http.common.Parameters.DECISION_PARAMETER
import brs.api.http.common.Parameters.ESCROW_PARAMETER
import brs.api.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.entity.DependencyProvider
import brs.entity.Escrow
import brs.services.EscrowService
import brs.services.ParameterService
import brs.transaction.appendix.Attachment
import brs.transaction.type.TransactionType
import brs.util.json.safeGetAsLong
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class EscrowSignTest : AbstractTransactionTest() {

    private lateinit var parameterServiceMock: ParameterService
    private lateinit var blockchainServiceMock: BlockchainService
    private lateinit var escrowServiceMock: EscrowService
    private lateinit var apiTransactionManagerMock: APITransactionManager
    private lateinit var dp: DependencyProvider
    private lateinit var t: EscrowSign

    @Before
    fun setUp() {
        parameterServiceMock = mock()
        blockchainServiceMock = mock()
        escrowServiceMock = mock()
        apiTransactionManagerMock = mock()
        dp = QuickMocker.dependencyProvider(
            parameterServiceMock,
            blockchainServiceMock,
            escrowServiceMock,
            apiTransactionManagerMock
        )
        t = EscrowSign(dp)
    }

    @Test
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
        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        whenever(escrowServiceMock.getEscrowTransaction(eq(escrowId))).doReturn(escrow)
        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(sender)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) },
            apiTransactionManagerMock
        ) as Attachment.AdvancedPaymentEscrowSign
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is brs.transaction.type.advancedPayment.EscrowSign)
        assertEquals(DecisionType.RELEASE, attachment.decision)
    }

    @Test
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

        whenever(escrowServiceMock.getEscrowTransaction(eq(escrowId))).doReturn(escrow)
        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(sender)
        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) },
            apiTransactionManagerMock
        ) as Attachment.AdvancedPaymentEscrowSign
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is brs.transaction.type.advancedPayment.EscrowSign)
        assertEquals(DecisionType.REFUND, attachment.decision)
    }

    @Test
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
        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        whenever(escrowServiceMock.isIdSigner(eq(senderId), eq(escrow))).doReturn(true)

        whenever(escrowServiceMock.getEscrowTransaction(eq(escrowId))).doReturn(escrow)
        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(sender)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) },
            apiTransactionManagerMock
        ) as Attachment.AdvancedPaymentEscrowSign
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is brs.transaction.type.advancedPayment.EscrowSign)
        assertEquals(DecisionType.REFUND, attachment.decision)
    }

    @Test
    fun processRequest_invalidEscrowId() {
        val request = QuickMocker.httpServletRequest(
                MockParam(ESCROW_PARAMETER, "NotANumber")
        )

        val result = t.processRequest(request) as JsonObject

        assertEquals(3L, result.get(ERROR_CODE_RESPONSE).safeGetAsLong())
    }

    @Test
    fun processRequest_escrowNotFound() {
        val escrowId: Long = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(ESCROW_PARAMETER, escrowId)
        )

        whenever(escrowServiceMock.getEscrowTransaction(eq(escrowId))).doReturn(null)

        val result = t.processRequest(request) as JsonObject

        assertEquals(5L, result.get(ERROR_CODE_RESPONSE).safeGetAsLong())
    }

    @Test
    fun processRequest_invalidDecisionType() {
        val escrowId: Long = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(ESCROW_PARAMETER, escrowId),
                MockParam(DECISION_PARAMETER, "notADecisionValue")
        )

        val escrow = mock<Escrow>()

        whenever(escrowServiceMock.getEscrowTransaction(eq(escrowId))).doReturn(escrow)

        val result = t.processRequest(request) as JsonObject

        assertEquals(5L, result.get(ERROR_CODE_RESPONSE).safeGetAsLong())
    }

    @Test
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

        whenever(escrowServiceMock.isIdSigner(eq(senderId), eq(escrow))).doReturn(false)

        val sender = mock<Account>()
        whenever(sender.id).doReturn(senderId)

        whenever(escrowServiceMock.getEscrowTransaction(eq(escrowId))).doReturn(escrow)
        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(sender)

        val result = t.processRequest(request) as JsonObject

        assertEquals(5L, result.get(ERROR_CODE_RESPONSE).safeGetAsLong())
    }

    @Test
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

        whenever(escrowServiceMock.getEscrowTransaction(eq(escrowId))).doReturn(escrow)
        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(sender)

        val result = t.processRequest(request) as JsonObject

        assertEquals(4L, result.get(ERROR_CODE_RESPONSE).safeGetAsLong())
    }

    @Test
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

        whenever(escrowServiceMock.getEscrowTransaction(eq(escrowId))).doReturn(escrow)
        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(sender)

        val result = t.processRequest(request) as JsonObject

        assertEquals(4L, result.get(ERROR_CODE_RESPONSE).safeGetAsLong())
    }
}
