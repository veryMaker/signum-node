package brs.http

import brs.Account
import brs.Attachment
import brs.Blockchain
import brs.Constants.MAX_BALANCE_NQT
import brs.DependencyProvider
import brs.DigitalGoodsStore.Purchase
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.common.TestConstants.TEST_SECRET_PHRASE
import brs.fluxcapacitor.FluxValues
import brs.http.JSONResponses.ALREADY_DELIVERED
import brs.http.JSONResponses.INCORRECT_DGS_DISCOUNT
import brs.http.JSONResponses.INCORRECT_DGS_GOODS
import brs.http.JSONResponses.INCORRECT_PURCHASE
import brs.http.common.Parameters.DISCOUNT_NQT_PARAMETER
import brs.http.common.Parameters.GOODS_TO_ENCRYPT_PARAMETER
import brs.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.services.AccountService
import brs.services.ParameterService
import brs.transaction.TransactionType
import brs.transaction.digitalGoods.DigitalGoodsDelivery
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import javax.servlet.http.HttpServletRequest

class DGSDeliveryTest : AbstractTransactionTest() {

    private lateinit var t: DGSDelivery
    private lateinit var dp: DependencyProvider
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var blockchainMock: Blockchain
    private lateinit var accountServiceMock: AccountService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        blockchainMock = mock<Blockchain>()
        accountServiceMock = mock<AccountService>()
        apiTransactionManagerMock = mock<APITransactionManager>()
        dp = QuickMocker.dependencyProvider(parameterServiceMock!!, blockchainMock!!, accountServiceMock!!, apiTransactionManagerMock!!)
        t = DGSDelivery(dp)
    }

    @Test
    fun processRequest() {
        val discountNQTParameter: Long = 1
        val goodsToEncryptParameter = "beef"

        val request = QuickMocker.httpServletRequest(
                MockParam(DISCOUNT_NQT_PARAMETER, discountNQTParameter),
                MockParam(GOODS_TO_ENCRYPT_PARAMETER, goodsToEncryptParameter),
                MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE)
        )

        val mockSellerAccount = mock<Account>()
        val mockBuyerAccount = mock<Account>()
        val mockPurchase = mock<Purchase>()

        whenever(mockSellerAccount.id).doReturn(1L)
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.buyerId).doReturn(2L)
        whenever(mockPurchase.quantity).doReturn(9)
        whenever(mockPurchase.priceNQT).doReturn(1L)

        whenever(mockPurchase.isPending).doReturn(true)

        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSellerAccount)
        whenever(parameterServiceMock!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)
        whenever(accountServiceMock!!.getAccount(eq(mockPurchase.buyerId))).doReturn(mockBuyerAccount)
        whenever(mockBuyerAccount.encryptTo(any(), any())).doReturn(mock())
        dp.fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) }, apiTransactionManagerMock!!) as Attachment.DigitalGoodsDelivery
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is DigitalGoodsDelivery)
        assertEquals(discountNQTParameter, attachment.discountNQT)
    }

    @Test
    fun processRequest_sellerAccountIdDifferentFromAccountSellerIdIsIncorrectPurchase() {
        val request = QuickMocker.httpServletRequest()

        val mockSellerAccount = mock<Account>()
        val mockPurchase = mock<Purchase>()

        whenever(mockSellerAccount.id).doReturn(1L)
        whenever(mockPurchase.sellerId).doReturn(2L)

        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSellerAccount)
        whenever(parameterServiceMock!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)

        assertEquals(INCORRECT_PURCHASE, t!!.processRequest(request))
    }

    @Test
    fun processRequest_purchaseNotPendingIsAlreadyDelivered() {
        val request = QuickMocker.httpServletRequest()

        val mockSellerAccount = mock<Account>()
        val mockPurchase = mock<Purchase>()

        whenever(mockSellerAccount.id).doReturn(1L)
        whenever(mockPurchase.sellerId).doReturn(1L)

        whenever(mockPurchase.isPending).doReturn(false)

        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSellerAccount)
        whenever(parameterServiceMock!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)

        assertEquals(ALREADY_DELIVERED, t!!.processRequest(request))
    }

    @Test
    fun processRequest_dgsDiscountNotAValidNumberIsIncorrectDGSDiscount() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DISCOUNT_NQT_PARAMETER, "Bob")
        )

        val mockSellerAccount = mock<Account>()
        val mockPurchase = mock<Purchase>()

        whenever(mockSellerAccount.id).doReturn(1L)
        whenever(mockPurchase.sellerId).doReturn(1L)

        whenever(mockPurchase.isPending).doReturn(true)

        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSellerAccount)
        whenever(parameterServiceMock!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)

        assertEquals(INCORRECT_DGS_DISCOUNT, t!!.processRequest(request))
    }

    @Test
    fun processRequest_dgsDiscountNegativeIsIncorrectDGSDiscount() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DISCOUNT_NQT_PARAMETER, "-1")
        )

        val mockSellerAccount = mock<Account>()
        val mockPurchase = mock<Purchase>()

        whenever(mockSellerAccount.id).doReturn(1L)
        whenever(mockPurchase.sellerId).doReturn(1L)

        whenever(mockPurchase.isPending).doReturn(true)

        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSellerAccount)
        whenever(parameterServiceMock!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)

        assertEquals(INCORRECT_DGS_DISCOUNT, t!!.processRequest(request))
    }

    @Test
    fun processRequest_dgsDiscountOverMaxBalanceNQTIsIncorrectDGSDiscount() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DISCOUNT_NQT_PARAMETER, "" + (MAX_BALANCE_NQT + 1))
        )

        val mockSellerAccount = mock<Account>()
        val mockPurchase = mock<Purchase>()

        whenever(mockSellerAccount.id).doReturn(1L)
        whenever(mockPurchase.sellerId).doReturn(1L)

        whenever(mockPurchase.isPending).doReturn(true)

        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSellerAccount)
        whenever(parameterServiceMock!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)

        assertEquals(INCORRECT_DGS_DISCOUNT, t!!.processRequest(request))
    }

    @Test
    fun processRequest_dgsDiscountNegativeIsNotSafeMultiply() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DISCOUNT_NQT_PARAMETER, "99999999999")
        )

        val mockSellerAccount = mock<Account>()
        val mockPurchase = mock<Purchase>()

        whenever(mockSellerAccount.id).doReturn(1L)
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.quantity).doReturn(999999999)
        whenever(mockPurchase.priceNQT).doReturn(1L)

        whenever(mockPurchase.isPending).doReturn(true)

        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSellerAccount)
        whenever(parameterServiceMock!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)

        assertEquals(INCORRECT_DGS_DISCOUNT, t!!.processRequest(request))
    }

    @Test
    fun processRequest_goodsToEncryptIsEmptyIsIncorrectDGSGoods() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DISCOUNT_NQT_PARAMETER, "9"),
                MockParam(GOODS_TO_ENCRYPT_PARAMETER, ""),
                MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE)
        )

        val mockSellerAccount = mock<Account>()
        val mockPurchase = mock<Purchase>()

        whenever(mockSellerAccount.id).doReturn(1L)
        whenever(mockPurchase.sellerId).doReturn(1L)
        whenever(mockPurchase.buyerId).doReturn(2L)
        whenever(mockPurchase.quantity).doReturn(9)
        whenever(mockPurchase.priceNQT).doReturn(1L)

        whenever(mockPurchase.isPending).doReturn(true)

        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSellerAccount)
        whenever(accountServiceMock!!.getAccount(eq(mockPurchase.buyerId))).doReturn(mock())
        whenever(parameterServiceMock!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)

        assertEquals(INCORRECT_DGS_GOODS, t!!.processRequest(request))
    }
}
