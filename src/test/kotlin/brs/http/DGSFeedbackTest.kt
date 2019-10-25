package brs.http

import brs.Account
import brs.Attachment
import brs.Blockchain
import brs.DependencyProvider
import brs.DigitalGoodsStore.Purchase
import brs.common.QuickMocker
import brs.crypto.EncryptedData
import brs.fluxcapacitor.FluxValues
import brs.http.JSONResponses.GOODS_NOT_DELIVERED
import brs.http.JSONResponses.INCORRECT_PURCHASE
import brs.services.AccountService
import brs.services.ParameterService
import brs.transaction.TransactionType
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
    private lateinit var dp: DependencyProvider
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var accountServiceMock: AccountService
    private lateinit var blockchainMock: Blockchain
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        parameterServiceMock = mock()
        accountServiceMock = mock()
        blockchainMock = mock()
        apiTransactionManagerMock = mock()
        dp = QuickMocker.dependencyProvider(
            parameterServiceMock,
            blockchainMock,
            accountServiceMock,
            apiTransactionManagerMock
        )
        t = DGSFeedback(dp)
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

        whenever(parameterServiceMock.getPurchase(eq(request))).doReturn(mockPurchase)
        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(mockAccount)
        whenever(accountServiceMock.getAccount(eq(2L))).doReturn(mockSellerAccount)

        whenever(mockAccount.id).doReturn(1L)
        whenever(mockPurchase.buyerId).doReturn(1L)
        whenever(mockPurchase.encryptedGoods).doReturn(mockEncryptedGoods)
        whenever(mockPurchase.sellerId).doReturn(2L)
        dp.fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.DigitalGoodsFeedback
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is DigitalGoodsFeedback)
        assertEquals(mockPurchaseId, attachment.purchaseId)
    }

    @Test
    fun processRequest_incorrectPurchaseWhenOtherBuyerId() {
        val request = QuickMocker.httpServletRequest()

        val mockPurchase = mock<Purchase>()
        val mockAccount = mock<Account>()

        whenever(parameterServiceMock.getPurchase(eq(request))).doReturn(mockPurchase)
        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(mockAccount)

        whenever(mockAccount.id).doReturn(1L)
        whenever(mockPurchase.buyerId).doReturn(2L)

        assertEquals(INCORRECT_PURCHASE, t.processRequest(request))
    }

    @Test
    fun processRequest_goodsNotDeliveredWhenNoEncryptedGoods() {
        val request = QuickMocker.httpServletRequest()

        val mockPurchase = mock<Purchase>()
        val mockAccount = mock<Account>()

        whenever(parameterServiceMock.getPurchase(eq(request))).doReturn(mockPurchase)
        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(mockAccount)

        whenever(mockAccount.id).doReturn(1L)
        whenever(mockPurchase.buyerId).doReturn(1L)
        whenever(mockPurchase.encryptedGoods).doReturn(null)

        assertEquals(GOODS_NOT_DELIVERED, t.processRequest(request))
    }

}
