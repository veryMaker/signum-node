package brs.http

import brs.*
import brs.DigitalGoodsStore.Purchase
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.crypto.EncryptedData
import brs.fluxcapacitor.FluxCapacitor
import brs.fluxcapacitor.FluxValues
import brs.services.AccountService
import brs.services.ParameterService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import javax.servlet.http.HttpServletRequest

import brs.TransactionType.DigitalGoods.REFUND
import brs.http.JSONResponses.DUPLICATE_REFUND
import brs.http.JSONResponses.GOODS_NOT_DELIVERED
import brs.http.JSONResponses.INCORRECT_DGS_REFUND
import brs.http.JSONResponses.INCORRECT_PURCHASE
import brs.http.common.Parameters.REFUND_NQT_PARAMETER
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DGSRefundTest : AbstractTransactionTest() {

    private var t: DGSRefund? = null

    private var mockParameterService: ParameterService? = null
    private var mockBlockchain: Blockchain? = null
    private var mockAccountService: AccountService? = null
    private var apiTransactionManagerMock: APITransactionManager? = null

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        mockBlockchain = mock<Blockchain>()
        mockAccountService = mock<AccountService>()
        apiTransactionManagerMock = mock<APITransactionManager>()

        t = DGSRefund(mockParameterService!!, mockBlockchain!!, mockAccountService!!, apiTransactionManagerMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val refundNQTParameter: Long = 5

        val req = QuickMocker.httpServletRequest(
                MockParam(REFUND_NQT_PARAMETER, refundNQTParameter)
        )

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.getId()).doReturn(1L)

        val mockPurchaseId = 123L
        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.id).doReturn(mockPurchaseId)
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.buyerId).doReturn(2L)
        whenever(mockPurchase.refundNote).doReturn(null)
        whenever(mockPurchase.encryptedGoods).doReturn(mock<EncryptedData>())

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(req))).doReturn(mockSellerAccount)
        whenever(mockParameterService!!.getPurchase(eq<HttpServletRequest>(req))).doReturn(mockPurchase)

        val mockBuyerAccount = mock<Account>()

        whenever(mockAccountService!!.getAccount(eq(mockPurchase.buyerId))).doReturn(mockBuyerAccount)

        QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(req) }, apiTransactionManagerMock!!) as Attachment.DigitalGoodsRefund
        assertNotNull(attachment)

        assertEquals(REFUND, attachment.transactionType)
        assertEquals(refundNQTParameter, attachment.refundNQT)
        assertEquals(mockPurchaseId, attachment.purchaseId)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectPurchase() {
        val req = QuickMocker.httpServletRequest()

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.getId()).doReturn(1L)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.sellerId).doReturn(2L)

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(req))).doReturn(mockSellerAccount)
        whenever(mockParameterService!!.getPurchase(eq<HttpServletRequest>(req))).doReturn(mockPurchase)

        assertEquals(INCORRECT_PURCHASE, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_duplicateRefund() {
        val req = QuickMocker.httpServletRequest()

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.getId()).doReturn(1L)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.refundNote).doReturn(mock<EncryptedData>())

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(req))).doReturn(mockSellerAccount)
        whenever(mockParameterService!!.getPurchase(eq<HttpServletRequest>(req))).doReturn(mockPurchase)

        assertEquals(DUPLICATE_REFUND, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_goodsNotDelivered() {
        val req = QuickMocker.httpServletRequest()

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.getId()).doReturn(1L)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.refundNote).doReturn(null)
        whenever(mockPurchase.encryptedGoods).doReturn(null)

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(req))).doReturn(mockSellerAccount)
        whenever(mockParameterService!!.getPurchase(eq<HttpServletRequest>(req))).doReturn(mockPurchase)

        assertEquals(GOODS_NOT_DELIVERED, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectDgsRefundWrongFormat() {
        val req = QuickMocker.httpServletRequest(
                MockParam(REFUND_NQT_PARAMETER, "Bob")
        )

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.getId()).doReturn(1L)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.refundNote).doReturn(null)
        whenever(mockPurchase.encryptedGoods).doReturn(mock<EncryptedData>())

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(req))).doReturn(mockSellerAccount)
        whenever(mockParameterService!!.getPurchase(eq<HttpServletRequest>(req))).doReturn(mockPurchase)

        assertEquals(INCORRECT_DGS_REFUND, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_negativeIncorrectDGSRefund() {
        val req = QuickMocker.httpServletRequest(
                MockParam(REFUND_NQT_PARAMETER, -5L)
        )

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.getId()).doReturn(1L)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.refundNote).doReturn(null)
        whenever(mockPurchase.encryptedGoods).doReturn(mock<EncryptedData>())

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(req))).doReturn(mockSellerAccount)
        whenever(mockParameterService!!.getPurchase(eq<HttpServletRequest>(req))).doReturn(mockPurchase)

        assertEquals(INCORRECT_DGS_REFUND, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_overMaxBalanceNQTIncorrectDGSRefund() {
        val req = QuickMocker.httpServletRequest(
                MockParam(REFUND_NQT_PARAMETER, Constants.MAX_BALANCE_NQT + 1)
        )

        val mockSellerAccount = mock<Account>()
        whenever(mockSellerAccount.getId()).doReturn(1L)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.refundNote).doReturn(null)
        whenever(mockPurchase.encryptedGoods).doReturn(mock<EncryptedData>())

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(req))).doReturn(mockSellerAccount)
        whenever(mockParameterService!!.getPurchase(eq<HttpServletRequest>(req))).doReturn(mockPurchase)

        assertEquals(INCORRECT_DGS_REFUND, t!!.processRequest(req))
    }

}
