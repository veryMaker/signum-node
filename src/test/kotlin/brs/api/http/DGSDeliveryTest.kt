package brs.api.http

import brs.api.http.common.JSONResponses.ALREADY_DELIVERED
import brs.api.http.common.JSONResponses.INCORRECT_DGS_DISCOUNT
import brs.api.http.common.JSONResponses.INCORRECT_DGS_GOODS
import brs.api.http.common.JSONResponses.INCORRECT_PURCHASE
import brs.api.http.common.Parameters.DISCOUNT_PLANCK_PARAMETER
import brs.api.http.common.Parameters.GOODS_TO_ENCRYPT_PARAMETER
import brs.api.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.common.TestConstants.TEST_SECRET_PHRASE
import brs.entity.Account
import brs.entity.DependencyProvider
import brs.entity.Purchase
import brs.objects.Constants.MAX_BALANCE_PLANCK
import brs.objects.FluxValues
import brs.services.AccountService
import brs.services.BlockchainService
import brs.services.ParameterService
import brs.transaction.appendix.Attachment
import brs.transaction.type.TransactionType
import brs.transaction.type.digitalGoods.DigitalGoodsDelivery
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DGSDeliveryTest : AbstractTransactionTest() {

    private lateinit var t: DGSDelivery
    private lateinit var dp: DependencyProvider
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var blockchainServiceMock: BlockchainService
    private lateinit var accountServiceMock: AccountService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        parameterServiceMock = mockk(relaxed = true)
        blockchainServiceMock = mockk(relaxed = true)
        accountServiceMock = mockk(relaxed = true)
        apiTransactionManagerMock = mockk(relaxed = true)
        dp = QuickMocker.dependencyProvider(
            parameterServiceMock,
            blockchainServiceMock,
            accountServiceMock,
            apiTransactionManagerMock
        )
        t = DGSDelivery(dp)
    }

    @Test
    fun processRequest() {
        val discountPlanckParameter: Long = 1
        val goodsToEncryptParameter = "beef"

        val request = QuickMocker.httpServletRequest(
                MockParam(DISCOUNT_PLANCK_PARAMETER, discountPlanckParameter),
                MockParam(GOODS_TO_ENCRYPT_PARAMETER, goodsToEncryptParameter),
                MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE)
        )

        val mockSellerAccount = mockk<Account>(relaxed = true)
        val mockBuyerAccount = mockk<Account>(relaxed = true)
        val mockPurchase = mockk<Purchase>(relaxed = true)

        every { mockSellerAccount.id } returns 1L
        every { mockPurchase.sellerId } returns 1L
        every { mockPurchase.buyerId } returns 2L
        every { mockPurchase.quantity } returns 9
        every { mockPurchase.pricePlanck } returns 1L

        every { mockPurchase.isPending } returns true

        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockSellerAccount
        every { parameterServiceMock.getPurchase(eq(request)) } returns mockPurchase
        every { accountServiceMock.getAccount(eq(mockPurchase.buyerId)) } returns mockBuyerAccount
        every { mockBuyerAccount.encryptTo(any(), any(), any()) } returns mockk(relaxed = true)
        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.DigitalGoodsDelivery
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is DigitalGoodsDelivery)
        assertEquals(discountPlanckParameter, attachment.discountPlanck)
    }

    @Test
    fun processRequest_sellerAccountIdDifferentFromAccountSellerIdIsIncorrectPurchase() {
        val request = QuickMocker.httpServletRequest()

        val mockSellerAccount = mockk<Account>(relaxed = true)
        val mockPurchase = mockk<Purchase>(relaxed = true)

        every { mockSellerAccount.id } returns 1L
        every { mockPurchase.sellerId } returns 2L

        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockSellerAccount
        every { parameterServiceMock.getPurchase(eq(request)) } returns mockPurchase

        assertEquals(INCORRECT_PURCHASE, t.processRequest(request))
    }

    @Test
    fun processRequest_purchaseNotPendingIsAlreadyDelivered() {
        val request = QuickMocker.httpServletRequest()

        val mockSellerAccount = mockk<Account>(relaxed = true)
        val mockPurchase = mockk<Purchase>(relaxed = true)

        every { mockSellerAccount.id } returns 1L
        every { mockPurchase.sellerId } returns 1L

        every { mockPurchase.isPending } returns false

        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockSellerAccount
        every { parameterServiceMock.getPurchase(eq(request)) } returns mockPurchase

        assertEquals(ALREADY_DELIVERED, t.processRequest(request))
    }

    @Test
    fun processRequest_dgsDiscountNotAValidNumberIsIncorrectDGSDiscount() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DISCOUNT_PLANCK_PARAMETER, "Bob")
        )

        val mockSellerAccount = mockk<Account>(relaxed = true)
        val mockPurchase = mockk<Purchase>(relaxed = true)

        every { mockSellerAccount.id } returns 1L
        every { mockPurchase.sellerId } returns 1L

        every { mockPurchase.isPending } returns true

        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockSellerAccount
        every { parameterServiceMock.getPurchase(eq(request)) } returns mockPurchase

        assertEquals(INCORRECT_DGS_DISCOUNT, t.processRequest(request))
    }

    @Test
    fun processRequest_dgsDiscountNegativeIsIncorrectDGSDiscount() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DISCOUNT_PLANCK_PARAMETER, "-1")
        )

        val mockSellerAccount = mockk<Account>(relaxed = true)
        val mockPurchase = mockk<Purchase>(relaxed = true)

        every { mockSellerAccount.id } returns 1L
        every { mockPurchase.sellerId } returns 1L

        every { mockPurchase.isPending } returns true

        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockSellerAccount
        every { parameterServiceMock.getPurchase(eq(request)) } returns mockPurchase

        assertEquals(INCORRECT_DGS_DISCOUNT, t.processRequest(request))
    }

    @Test
    fun processRequest_dgsDiscountOverMaxBalancePlanckIsIncorrectDGSDiscount() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DISCOUNT_PLANCK_PARAMETER, (MAX_BALANCE_PLANCK + 1).toString())
        )

        val mockSellerAccount = mockk<Account>(relaxed = true)
        val mockPurchase = mockk<Purchase>(relaxed = true)

        every { mockSellerAccount.id } returns 1L
        every { mockPurchase.sellerId } returns 1L

        every { mockPurchase.isPending } returns true

        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockSellerAccount
        every { parameterServiceMock.getPurchase(eq(request)) } returns mockPurchase

        assertEquals(INCORRECT_DGS_DISCOUNT, t.processRequest(request))
    }

    @Test
    fun processRequest_dgsDiscountNegativeIsNotSafeMultiply() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DISCOUNT_PLANCK_PARAMETER, "99999999999")
        )

        val mockSellerAccount = mockk<Account>(relaxed = true)
        val mockPurchase = mockk<Purchase>(relaxed = true)

        every { mockSellerAccount.id } returns 1L
        every { mockPurchase.sellerId } returns 1L
        every { mockPurchase.quantity } returns 999999999
        every { mockPurchase.pricePlanck } returns 1L

        every { mockPurchase.isPending } returns true

        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockSellerAccount
        every { parameterServiceMock.getPurchase(eq(request)) } returns mockPurchase

        assertEquals(INCORRECT_DGS_DISCOUNT, t.processRequest(request))
    }

    @Test
    fun processRequest_goodsToEncryptIsEmptyIsIncorrectDGSGoods() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DISCOUNT_PLANCK_PARAMETER, "9"),
                MockParam(GOODS_TO_ENCRYPT_PARAMETER, ""),
                MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE)
        )

        val mockSellerAccount = mockk<Account>(relaxed = true)
        val mockPurchase = mockk<Purchase>(relaxed = true)

        every { mockSellerAccount.id } returns 1L
        every { mockPurchase.sellerId } returns 1L
        every { mockPurchase.buyerId } returns 2L
        every { mockPurchase.quantity } returns 9
        every { mockPurchase.pricePlanck } returns 1L

        every { mockPurchase.isPending } returns true

        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockSellerAccount
        every { accountServiceMock.getAccount(eq(mockPurchase.buyerId)) } returns mockk(relaxed = true)
        every { parameterServiceMock.getPurchase(eq(request)) } returns mockPurchase

        assertEquals(INCORRECT_DGS_GOODS, t.processRequest(request))
    }
}
