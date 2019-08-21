package brs.http

import brs.*
import brs.DigitalGoodsStore.Purchase
import brs.common.QuickMocker
import brs.crypto.EncryptedData
import brs.fluxcapacitor.FluxCapacitor
import brs.fluxcapacitor.FluxValues
import brs.services.AccountService
import brs.services.ParameterService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import javax.servlet.http.HttpServletRequest

import brs.TransactionType.DigitalGoods.FEEDBACK
import brs.http.JSONResponses.GOODS_NOT_DELIVERED
import brs.http.JSONResponses.INCORRECT_PURCHASE
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class DGSFeedbackTest : AbstractTransactionTest() {

    private var t: DGSFeedback? = null

    private var parameterServiceMock: ParameterService? = null
    private var accountServiceMock: AccountService? = null
    private var blockchainMock: Blockchain? = null
    private var apiTransactionManagerMock: APITransactionManager? = null

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        accountServiceMock = mock<AccountService>()
        blockchainMock = mock<Blockchain>()
        apiTransactionManagerMock = mock<APITransactionManager>()

        t = DGSFeedback(parameterServiceMock!!, blockchainMock!!, accountServiceMock!!, apiTransactionManagerMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val req = QuickMocker.httpServletRequest()

        val mockPurchaseId = 123L
        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.id).doReturn(mockPurchaseId)
        val mockAccount = mock<Account>()
        val mockSellerAccount = mock<Account>()
        val mockEncryptedGoods = mock<EncryptedData>()

        whenever(parameterServiceMock!!.getPurchase(eq<HttpServletRequest>(req))).doReturn(mockPurchase)
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(req))).doReturn(mockAccount)
        whenever(accountServiceMock!!.getAccount(eq(2L))).doReturn(mockSellerAccount)

        whenever(mockAccount.getId()).doReturn(1L)
        whenever(mockPurchase.buyerId).doReturn(1L)
        whenever(mockPurchase.encryptedGoods).doReturn(mockEncryptedGoods)
        whenever(mockPurchase.sellerId).doReturn(2L)

        mockkStatic(Burst::class)
        val fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        every { Burst.getFluxCapacitor() } returns fluxCapacitor

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(req) }, apiTransactionManagerMock!!) as Attachment.DigitalGoodsFeedback
        assertNotNull(attachment)

        assertEquals(FEEDBACK, attachment.transactionType)
        assertEquals(mockPurchaseId, attachment.purchaseId)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectPurchaseWhenOtherBuyerId() {
        val req = QuickMocker.httpServletRequest()

        val mockPurchase = mock<Purchase>()
        val mockAccount = mock<Account>()

        whenever(parameterServiceMock!!.getPurchase(eq<HttpServletRequest>(req))).doReturn(mockPurchase)
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(req))).doReturn(mockAccount)

        whenever(mockAccount.getId()).doReturn(1L)
        whenever(mockPurchase.buyerId).doReturn(2L)

        assertEquals(INCORRECT_PURCHASE, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_goodsNotDeliveredWhenNoEncryptedGoods() {
        val req = QuickMocker.httpServletRequest()

        val mockPurchase = mock<Purchase>()
        val mockAccount = mock<Account>()

        whenever(parameterServiceMock!!.getPurchase(eq<HttpServletRequest>(req))).doReturn(mockPurchase)
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(req))).doReturn(mockAccount)

        whenever(mockAccount.getId()).doReturn(1L)
        whenever(mockPurchase.buyerId).doReturn(1L)
        whenever(mockPurchase.encryptedGoods).doReturn(null)

        assertEquals(GOODS_NOT_DELIVERED, t!!.processRequest(req))
    }

}
