package brs.api.http

import brs.entity.Purchase
import brs.entity.Account
import brs.services.BlockchainService
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.objects.FluxValues
import brs.api.http.JSONResponses.DUPLICATE_REFUND
import brs.api.http.JSONResponses.GOODS_NOT_DELIVERED
import brs.api.http.JSONResponses.INCORRECT_DGS_REFUND
import brs.api.http.JSONResponses.INCORRECT_PURCHASE
import brs.api.http.common.Parameters.REFUND_PLANCK_PARAMETER
import brs.entity.DependencyProvider
import brs.objects.Constants
import brs.services.AccountService
import brs.services.ParameterService
import brs.transaction.appendix.Attachment
import brs.transaction.type.TransactionType
import brs.transaction.type.digitalGoods.DigitalGoodsRefund
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DGSRefundTest : AbstractTransactionTest() {

    private lateinit var t: DGSRefund
    private lateinit var dp: DependencyProvider
    private lateinit var mockParameterService: ParameterService
    private lateinit var mockBlockchainService: BlockchainService
    private lateinit var mockAccountService: AccountService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        mockParameterService = mockk()
        mockBlockchainService = mockk()
        mockAccountService = mockk()
        apiTransactionManagerMock = mockk()
        dp = QuickMocker.dependencyProvider(
            mockParameterService,
            mockBlockchainService,
            mockAccountService,
            apiTransactionManagerMock
        )
        t = DGSRefund(dp)
    }

    @Test
    fun processRequest() {
        val refundPlanckParameter: Long = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(REFUND_PLANCK_PARAMETER, refundPlanckParameter)
        )

        val mockSellerAccount = mockk<Account>()
        every { mockSellerAccount.id } returns 1L

        val mockPurchaseId = 123L
        val mockPurchase = mockk<Purchase>()
        every { mockPurchase.id } returns mockPurchaseId
        every { mockPurchase.sellerId } returns 1L
        every { mockPurchase.buyerId } returns 2L
        every { mockPurchase.refundNote } returns null
        every { mockPurchase.encryptedGoods } returns mockk()

        every { mockParameterService.getSenderAccount(eq(request)) } returns mockSellerAccount
        every { mockParameterService.getPurchase(eq(request)) } returns mockPurchase

        val mockBuyerAccount = mockk<Account>()

        every { mockAccountService.getAccount(eq(mockPurchase.buyerId)) } returns mockBuyerAccount
        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.DigitalGoodsRefund
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is DigitalGoodsRefund)
        assertEquals(refundPlanckParameter, attachment.refundPlanck)
        assertEquals(mockPurchaseId, attachment.purchaseId)
    }

    @Test
    fun processRequest_incorrectPurchase() {
        val request = QuickMocker.httpServletRequest()

        val mockSellerAccount = mockk<Account>()
        every { mockSellerAccount.id } returns 1L

        val mockPurchase = mockk<Purchase>()
        every { mockPurchase.sellerId } returns 2L

        every { mockParameterService.getSenderAccount(eq(request)) } returns mockSellerAccount
        every { mockParameterService.getPurchase(eq(request)) } returns mockPurchase

        assertEquals(INCORRECT_PURCHASE, t.processRequest(request))
    }

    @Test
    fun processRequest_duplicateRefund() {
        val request = QuickMocker.httpServletRequest()

        val mockSellerAccount = mockk<Account>()
        every { mockSellerAccount.id } returns 1L

        val mockPurchase = mockk<Purchase>()
        every { mockPurchase.sellerId } returns 1L
        every { mockPurchase.refundNote } returns mockk()

        every { mockParameterService.getSenderAccount(eq(request)) } returns mockSellerAccount
        every { mockParameterService.getPurchase(eq(request)) } returns mockPurchase

        assertEquals(DUPLICATE_REFUND, t.processRequest(request))
    }

    @Test
    fun processRequest_goodsNotDelivered() {
        val request = QuickMocker.httpServletRequest()

        val mockSellerAccount = mockk<Account>()
        every { mockSellerAccount.id } returns 1L

        val mockPurchase = mockk<Purchase>()
        every { mockPurchase.sellerId } returns 1L
        every { mockPurchase.refundNote } returns null
        every { mockPurchase.encryptedGoods } returns null

        every { mockParameterService.getSenderAccount(eq(request)) } returns mockSellerAccount
        every { mockParameterService.getPurchase(eq(request)) } returns mockPurchase

        assertEquals(GOODS_NOT_DELIVERED, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectDgsRefundWrongFormat() {
        val request = QuickMocker.httpServletRequest(
                MockParam(REFUND_PLANCK_PARAMETER, "Bob")
        )

        val mockSellerAccount = mockk<Account>()
        every { mockSellerAccount.id } returns 1L

        val mockPurchase = mockk<Purchase>()
        every { mockPurchase.sellerId } returns 1L
        every { mockPurchase.refundNote } returns null
        every { mockPurchase.encryptedGoods } returns mockk()

        every { mockParameterService.getSenderAccount(eq(request)) } returns mockSellerAccount
        every { mockParameterService.getPurchase(eq(request)) } returns mockPurchase

        assertEquals(INCORRECT_DGS_REFUND, t.processRequest(request))
    }

    @Test
    fun processRequest_negativeIncorrectDGSRefund() {
        val request = QuickMocker.httpServletRequest(
                MockParam(REFUND_PLANCK_PARAMETER, -5L)
        )

        val mockSellerAccount = mockk<Account>()
        every { mockSellerAccount.id } returns 1L

        val mockPurchase = mockk<Purchase>()
        every { mockPurchase.sellerId } returns 1L
        every { mockPurchase.refundNote } returns null
        every { mockPurchase.encryptedGoods } returns mockk()

        every { mockParameterService.getSenderAccount(eq(request)) } returns mockSellerAccount
        every { mockParameterService.getPurchase(eq(request)) } returns mockPurchase

        assertEquals(INCORRECT_DGS_REFUND, t.processRequest(request))
    }

    @Test
    fun processRequest_overMaxBalancePlanckIncorrectDGSRefund() {
        val request = QuickMocker.httpServletRequest(
                MockParam(REFUND_PLANCK_PARAMETER, Constants.MAX_BALANCE_PLANCK + 1)
        )

        val mockSellerAccount = mockk<Account>()
        every { mockSellerAccount.id } returns 1L

        val mockPurchase = mockk<Purchase>()
        every { mockPurchase.sellerId } returns 1L
        every { mockPurchase.refundNote } returns null
        every { mockPurchase.encryptedGoods } returns mockk()

        every { mockParameterService.getSenderAccount(eq(request)) } returns mockSellerAccount
        every { mockParameterService.getPurchase(eq(request)) } returns mockPurchase

        assertEquals(INCORRECT_DGS_REFUND, t.processRequest(request))
    }

}
