package brs.api.http

import brs.api.http.common.JSONResponses.GOODS_NOT_DELIVERED
import brs.api.http.common.JSONResponses.INCORRECT_PURCHASE
import brs.common.QuickMocker
import brs.entity.Account
import brs.entity.DependencyProvider
import brs.entity.Purchase
import brs.objects.FluxValues
import brs.services.AccountService
import brs.services.BlockchainService
import brs.services.ParameterService
import brs.transaction.appendix.Attachment
import brs.transaction.type.TransactionType
import brs.transaction.type.digitalGoods.DigitalGoodsFeedback
import burst.kit.entity.BurstEncryptedMessage
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DGSFeedbackTest : AbstractTransactionTest() {

    private lateinit var t: DGSFeedback
    private lateinit var dp: DependencyProvider
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var accountServiceMock: AccountService
    private lateinit var blockchainServiceMock: BlockchainService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        parameterServiceMock = mockk(relaxed = true)
        accountServiceMock = mockk(relaxed = true)
        blockchainServiceMock = mockk(relaxed = true)
        apiTransactionManagerMock = mockk(relaxed = true)
        dp = QuickMocker.dependencyProvider(
            parameterServiceMock,
            blockchainServiceMock,
            accountServiceMock,
            apiTransactionManagerMock
        )
        t = DGSFeedback(dp)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val mockPurchaseId = 123L
        val mockPurchase = mockk<Purchase>(relaxed = true)
        every { mockPurchase.id } returns mockPurchaseId
        val mockAccount = mockk<Account>(relaxed = true)
        val mockSellerAccount = mockk<Account>(relaxed = true)
        val mockEncryptedGoods = mockk<BurstEncryptedMessage>(relaxed = true)

        every { parameterServiceMock.getPurchase(eq(request)) } returns mockPurchase
        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockAccount
        every { accountServiceMock.getAccount(eq(2L)) } returns mockSellerAccount

        every { mockAccount.id } returns 1L
        every { mockPurchase.buyerId } returns 1L
        every { mockPurchase.encryptedGoods } returns mockEncryptedGoods
        every { mockPurchase.sellerId } returns 2L
        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.DigitalGoodsFeedback
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is DigitalGoodsFeedback)
        assertEquals(mockPurchaseId, attachment.purchaseId)
    }

    @Test
    fun processRequest_incorrectPurchaseWhenOtherBuyerId() {
        val request = QuickMocker.httpServletRequest()

        val mockPurchase = mockk<Purchase>(relaxed = true)
        val mockAccount = mockk<Account>(relaxed = true)

        every { parameterServiceMock.getPurchase(eq(request)) } returns mockPurchase
        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockAccount

        every { mockAccount.id } returns 1L
        every { mockPurchase.buyerId } returns 2L

        assertEquals(INCORRECT_PURCHASE, t.processRequest(request))
    }

    @Test
    fun processRequest_goodsNotDeliveredWhenNoEncryptedGoods() {
        val request = QuickMocker.httpServletRequest()

        val mockPurchase = mockk<Purchase>(relaxed = true)
        val mockAccount = mockk<Account>(relaxed = true)

        every { parameterServiceMock.getPurchase(eq(request)) } returns mockPurchase
        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockAccount

        every { mockAccount.id } returns 1L
        every { mockPurchase.buyerId } returns 1L
        every { mockPurchase.encryptedGoods } returns null

        assertEquals(GOODS_NOT_DELIVERED, t.processRequest(request))
    }

}
