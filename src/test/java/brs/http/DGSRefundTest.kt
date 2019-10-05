package brs.http

import brs.*
import brs.DigitalGoodsStore.Purchase
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.crypto.EncryptedData
import brs.fluxcapacitor.FluxValues
import brs.http.JSONResponses.DUPLICATE_REFUND
import brs.http.JSONResponses.GOODS_NOT_DELIVERED
import brs.http.JSONResponses.INCORRECT_DGS_REFUND
import brs.http.JSONResponses.INCORRECT_PURCHASE
import brs.http.common.Parameters.REFUND_NQT_PARAMETER
import brs.services.AccountService
import brs.services.ParameterService
import brs.transaction.TransactionType
import brs.transaction.digitalGoods.DigitalGoodsRefund
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
class DGSRefundTest : AbstractTransactionTest() {

    private lateinit var t: DGSRefund
    private lateinit var dp: DependencyProvider
    private lateinit var mockParameterService: ParameterService
    private lateinit var mockBlockchain: Blockchain
    private lateinit var mockAccountService: AccountService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        mockBlockchain = mock<Blockchain>()
        mockAccountService = mock<AccountService>()
        apiTransactionManagerMock = mock<APITransactionManager>()
        dp = QuickMocker.dependencyProvider(mockParameterService!!, mockBlockchain!!, mockAccountService!!, apiTransactionManagerMock!!)
        t = DGSRefund(dp)
    }

    @Test
    fun processRequest() = runBlocking {
        val refundNQTParameter: Long = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(REFUND_NQT_PARAMETER, refundNQTParameter)
        )

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.id).doReturn(1L)

        val mockPurchaseId = 123L
        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.id).doReturn(mockPurchaseId)
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.buyerId).doReturn(2L)
        whenever(mockPurchase.refundNote).doReturn(null)
        whenever(mockPurchase.encryptedGoods).doReturn(mock<EncryptedData>())

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSellerAccount)
        whenever(mockParameterService!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)

        val mockBuyerAccount = mock<Account>()

        whenever(mockAccountService!!.getAccount(eq(mockPurchase.buyerId))).doReturn(mockBuyerAccount)
        dp.fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) }, apiTransactionManagerMock!!) as Attachment.DigitalGoodsRefund
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is DigitalGoodsRefund)
        assertEquals(refundNQTParameter, attachment.refundNQT)
        assertEquals(mockPurchaseId, attachment.purchaseId)
    }

    @Test
    fun processRequest_incorrectPurchase() = runBlocking {
        val request = QuickMocker.httpServletRequest()

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.id).doReturn(1L)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.sellerId).doReturn(2L)

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSellerAccount)
        whenever(mockParameterService!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)

        assertEquals(INCORRECT_PURCHASE, t!!.processRequest(request))
    }

    @Test
    fun processRequest_duplicateRefund() = runBlocking {
        val request = QuickMocker.httpServletRequest()

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.id).doReturn(1L)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.refundNote).doReturn(mock<EncryptedData>())

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSellerAccount)
        whenever(mockParameterService!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)

        assertEquals(DUPLICATE_REFUND, t!!.processRequest(request))
    }

    @Test
    fun processRequest_goodsNotDelivered() = runBlocking {
        val request = QuickMocker.httpServletRequest()

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.id).doReturn(1L)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.refundNote).doReturn(null)
        whenever(mockPurchase.encryptedGoods).doReturn(null)

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSellerAccount)
        whenever(mockParameterService!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)

        assertEquals(GOODS_NOT_DELIVERED, t!!.processRequest(request))
    }

    @Test
    fun processRequest_incorrectDgsRefundWrongFormat() = runBlocking {
        val request = QuickMocker.httpServletRequest(
                MockParam(REFUND_NQT_PARAMETER, "Bob")
        )

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.id).doReturn(1L)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.refundNote).doReturn(null)
        whenever(mockPurchase.encryptedGoods).doReturn(mock<EncryptedData>())

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSellerAccount)
        whenever(mockParameterService!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)

        assertEquals(INCORRECT_DGS_REFUND, t!!.processRequest(request))
    }

    @Test
    fun processRequest_negativeIncorrectDGSRefund() = runBlocking {
        val request = QuickMocker.httpServletRequest(
                MockParam(REFUND_NQT_PARAMETER, -5L)
        )

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.id).doReturn(1L)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.refundNote).doReturn(null)
        whenever(mockPurchase.encryptedGoods).doReturn(mock<EncryptedData>())

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSellerAccount)
        whenever(mockParameterService!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)

        assertEquals(INCORRECT_DGS_REFUND, t!!.processRequest(request))
    }

    @Test
    fun processRequest_overMaxBalanceNQTIncorrectDGSRefund() = runBlocking {
        val request = QuickMocker.httpServletRequest(
                MockParam(REFUND_NQT_PARAMETER, Constants.MAX_BALANCE_NQT + 1)
        )

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.id).doReturn(1L)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.refundNote).doReturn(null)
        whenever(mockPurchase.encryptedGoods).doReturn(mock<EncryptedData>())

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSellerAccount)
        whenever(mockParameterService!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)

        assertEquals(INCORRECT_DGS_REFUND, t!!.processRequest(request))
    }

}
