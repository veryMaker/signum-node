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
import io.mockk.mockk
import io.mockk.every
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
        parameterServiceMock = mockk(relaxed = true)
        blockchainServiceMock = mockk(relaxed = true)
        escrowServiceMock = mockk(relaxed = true)
        apiTransactionManagerMock = mockk(relaxed = true)
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

        val escrow = mockk<Escrow>(relaxed = true)
        every { escrow.senderId } returns senderId
        every { escrow.recipientId } returns 2L

        val sender = mockk<Account>(relaxed = true)
        every { sender.id } returns senderId
        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        every { escrowServiceMock.getEscrowTransaction(eq(escrowId)) } returns escrow
        every { parameterServiceMock.getSenderAccount(eq(request)) } returns sender

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

        val escrow = mockk<Escrow>(relaxed = true)
        every { escrow.senderId } returns 1L
        every { escrow.recipientId } returns senderId

        val sender = mockk<Account>(relaxed = true)
        every { sender.id } returns senderId

        every { escrowServiceMock.getEscrowTransaction(eq(escrowId)) } returns escrow
        every { parameterServiceMock.getSenderAccount(eq(request)) } returns sender
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

        val escrow = mockk<Escrow>(relaxed = true)
        every { escrow.recipientId } returns 1L
        every { escrow.senderId } returns 2L

        val sender = mockk<Account>(relaxed = true)
        every { sender.id } returns senderId
        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        every { escrowServiceMock.isIdSigner(eq(senderId), eq(escrow)) } returns true

        every { escrowServiceMock.getEscrowTransaction(eq(escrowId)) } returns escrow
        every { parameterServiceMock.getSenderAccount(eq(request)) } returns sender

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

        every { escrowServiceMock.getEscrowTransaction(eq(escrowId)) } returns null

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

        val escrow = mockk<Escrow>(relaxed = true)

        every { escrowServiceMock.getEscrowTransaction(eq(escrowId)) } returns escrow

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

        val escrow = mockk<Escrow>(relaxed = true)
        every { escrow.senderId } returns 1L
        every { escrow.recipientId } returns 2L

        every { escrowServiceMock.isIdSigner(eq(senderId), eq(escrow)) } returns false

        val sender = mockk<Account>(relaxed = true)
        every { sender.id } returns senderId

        every { escrowServiceMock.getEscrowTransaction(eq(escrowId)) } returns escrow
        every { parameterServiceMock.getSenderAccount(eq(request)) } returns sender

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

        val escrow = mockk<Escrow>(relaxed = true)
        every { escrow.senderId } returns senderId

        val sender = mockk<Account>(relaxed = true)
        every { sender.id } returns senderId

        every { escrowServiceMock.getEscrowTransaction(eq(escrowId)) } returns escrow
        every { parameterServiceMock.getSenderAccount(eq(request)) } returns sender

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

        val escrow = mockk<Escrow>(relaxed = true)
        every { escrow.recipientId } returns senderId

        val sender = mockk<Account>(relaxed = true)
        every { sender.id } returns senderId

        every { escrowServiceMock.getEscrowTransaction(eq(escrowId)) } returns escrow
        every { parameterServiceMock.getSenderAccount(eq(request)) } returns sender

        val result = t.processRequest(request) as JsonObject

        assertEquals(4L, result.get(ERROR_CODE_RESPONSE).safeGetAsLong())
    }
}
