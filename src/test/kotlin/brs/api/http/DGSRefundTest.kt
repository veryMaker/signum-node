package brs.api.http

import brs.*
import brs.entity.DigitalGoodsStore.Purchase
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
import brs.objects.Constants
import brs.services.AccountService
import brs.services.ParameterService
import brs.transaction.appendix.Attachment
import brs.transaction.type.TransactionType
import brs.transaction.type.digitalGoods.DigitalGoodsRefund
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
class DGSRefundTest : AbstractTransactionTest() {

    private lateinit var t: DGSRefund
    private lateinit var dp: DependencyProvider
    private lateinit var mockParameterService: ParameterService
    private lateinit var mockBlockchainService: BlockchainService
    private lateinit var mockAccountService: AccountService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        mockParameterService = mock()
        mockBlockchainService = mock()
        mockAccountService = mock()
        apiTransactionManagerMock = mock()
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

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.id).doReturn(1L)

        val mockPurchaseId = 123L
        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.id).doReturn(mockPurchaseId)
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.buyerId).doReturn(2L)
        whenever(mockPurchase.refundNote).doReturn(null)
        whenever(mockPurchase.encryptedGoods).doReturn(mock())

        whenever(mockParameterService.getSenderAccount(eq(request))).doReturn(mockSellerAccount)
        whenever(mockParameterService.getPurchase(eq(request))).doReturn(mockPurchase)

        val mockBuyerAccount = mock<Account>()

        whenever(mockAccountService.getAccount(eq(mockPurchase.buyerId))).doReturn(mockBuyerAccount)
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

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.id).doReturn(1L)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.sellerId).doReturn(2L)

        whenever(mockParameterService.getSenderAccount(eq(request))).doReturn(mockSellerAccount)
        whenever(mockParameterService.getPurchase(eq(request))).doReturn(mockPurchase)

        assertEquals(INCORRECT_PURCHASE, t.processRequest(request))
    }

    @Test
    fun processRequest_duplicateRefund() {
        val request = QuickMocker.httpServletRequest()

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.id).doReturn(1L)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.refundNote).doReturn(mock())

        whenever(mockParameterService.getSenderAccount(eq(request))).doReturn(mockSellerAccount)
        whenever(mockParameterService.getPurchase(eq(request))).doReturn(mockPurchase)

        assertEquals(DUPLICATE_REFUND, t.processRequest(request))
    }

    @Test
    fun processRequest_goodsNotDelivered() {
        val request = QuickMocker.httpServletRequest()

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.id).doReturn(1L)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.refundNote).doReturn(null)
        whenever(mockPurchase.encryptedGoods).doReturn(null)

        whenever(mockParameterService.getSenderAccount(eq(request))).doReturn(mockSellerAccount)
        whenever(mockParameterService.getPurchase(eq(request))).doReturn(mockPurchase)

        assertEquals(GOODS_NOT_DELIVERED, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectDgsRefundWrongFormat() {
        val request = QuickMocker.httpServletRequest(
                MockParam(REFUND_PLANCK_PARAMETER, "Bob")
        )

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.id).doReturn(1L)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.refundNote).doReturn(null)
        whenever(mockPurchase.encryptedGoods).doReturn(mock())

        whenever(mockParameterService.getSenderAccount(eq(request))).doReturn(mockSellerAccount)
        whenever(mockParameterService.getPurchase(eq(request))).doReturn(mockPurchase)

        assertEquals(INCORRECT_DGS_REFUND, t.processRequest(request))
    }

    @Test
    fun processRequest_negativeIncorrectDGSRefund() {
        val request = QuickMocker.httpServletRequest(
                MockParam(REFUND_PLANCK_PARAMETER, -5L)
        )

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.id).doReturn(1L)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.refundNote).doReturn(null)
        whenever(mockPurchase.encryptedGoods).doReturn(mock())

        whenever(mockParameterService.getSenderAccount(eq(request))).doReturn(mockSellerAccount)
        whenever(mockParameterService.getPurchase(eq(request))).doReturn(mockPurchase)

        assertEquals(INCORRECT_DGS_REFUND, t.processRequest(request))
    }

    @Test
    fun processRequest_overMaxBalancePlanckIncorrectDGSRefund() {
        val request = QuickMocker.httpServletRequest(
                MockParam(REFUND_PLANCK_PARAMETER, Constants.MAX_BALANCE_PLANCK + 1)
        )

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.id).doReturn(1L)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.refundNote).doReturn(null)
        whenever(mockPurchase.encryptedGoods).doReturn(mock())

        whenever(mockParameterService.getSenderAccount(eq(request))).doReturn(mockSellerAccount)
        whenever(mockParameterService.getPurchase(eq(request))).doReturn(mockPurchase)

        assertEquals(INCORRECT_DGS_REFUND, t.processRequest(request))
    }

}
