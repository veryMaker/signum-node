package brs.http

import brs.Account
import brs.Attachment
import brs.Blockchain
import brs.DigitalGoodsStore.Purchase
import brs.common.QuickMocker
import brs.crypto.EncryptedData
import brs.fluxcapacitor.FluxValues
import brs.http.JSONResponses.GOODS_NOT_DELIVERED
import brs.http.JSONResponses.INCORRECT_PURCHASE
import brs.services.AccountService
import brs.services.ParameterService
import brs.transaction.digitalGoods.DigitalGoodsFeedback
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import javax.servlet.http.HttpServletRequest

class DGSFeedbackTest : AbstractTransactionTest() {

    private lateinit var t: DGSFeedback

    private lateinit var parameterServiceMock: ParameterService
    private lateinit var accountServiceMock: AccountService
    private lateinit var blockchainMock: Blockchain
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        accountServiceMock = mock<AccountService>()
        blockchainMock = mock<Blockchain>()
        apiTransactionManagerMock = mock<APITransactionManager>()

        t = DGSFeedback(QuickMocker.dependencyProvider(parameterServiceMock!!, blockchainMock!!, accountServiceMock!!, apiTransactionManagerMock!!))
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val mockPurchaseId = 123L
        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.id).doReturn(mockPurchaseId)
        val mockAccount = mock<Account>()
        val mockSellerAccount = mock<Account>()
        val mockEncryptedGoods = mock<EncryptedData>()

        whenever(parameterServiceMock!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)
        whenever(accountServiceMock!!.getAccount(eq(2L))).doReturn(mockSellerAccount)

        whenever(mockAccount.id).doReturn(1L)
        whenever(mockPurchase.buyerId).doReturn(1L)
        whenever(mockPurchase.encryptedGoods).doReturn(mockEncryptedGoods)
        whenever(mockPurchase.sellerId).doReturn(2L)
        val fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) }, apiTransactionManagerMock!!) as Attachment.DigitalGoodsFeedback
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is DigitalGoodsFeedback)
        assertEquals(mockPurchaseId, attachment.purchaseId)
    }

    @Test
    fun processRequest_incorrectPurchaseWhenOtherBuyerId() {
        val request = QuickMocker.httpServletRequest()

        val mockPurchase = mock<Purchase>()
        val mockAccount = mock<Account>()

        whenever(parameterServiceMock!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)

        whenever(mockAccount.id).doReturn(1L)
        whenever(mockPurchase.buyerId).doReturn(2L)

        assertEquals(INCORRECT_PURCHASE, t!!.processRequest(request))
    }

    @Test
    fun processRequest_goodsNotDeliveredWhenNoEncryptedGoods() {
        val request = QuickMocker.httpServletRequest()

        val mockPurchase = mock<Purchase>()
        val mockAccount = mock<Account>()

        whenever(parameterServiceMock!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)

        whenever(mockAccount.id).doReturn(1L)
        whenever(mockPurchase.buyerId).doReturn(1L)
        whenever(mockPurchase.encryptedGoods).doReturn(null)

        assertEquals(GOODS_NOT_DELIVERED, t!!.processRequest(request))
    }

}
