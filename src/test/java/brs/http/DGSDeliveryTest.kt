package brs.http

import brs.*
import brs.DigitalGoodsStore.Purchase
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxValues
import brs.services.AccountService
import brs.services.ParameterService
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.Constants.MAX_BALANCE_NQT
import brs.transaction.TransactionType.DigitalGoods.DELIVERY
import brs.common.TestConstants.TEST_SECRET_PHRASE
import brs.http.JSONResponses.ALREADY_DELIVERED
import brs.http.JSONResponses.INCORRECT_DGS_DISCOUNT
import brs.http.JSONResponses.INCORRECT_DGS_GOODS
import brs.http.JSONResponses.INCORRECT_PURCHASE
import com.nhaarman.mockitokotlin2.*
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class DGSDeliveryTest : AbstractTransactionTest() {

    private var t: DGSDelivery? = null

    private var parameterServiceMock: ParameterService? = null
    private var blockchainMock: Blockchain? = null
    private var accountServiceMock: AccountService? = null
    private var apiTransactionManagerMock: APITransactionManager? = null

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        blockchainMock = mock<Blockchain>()
        accountServiceMock = mock<AccountService>()
        apiTransactionManagerMock = mock<APITransactionManager>()

        t = DGSDelivery(parameterServiceMock!!, blockchainMock!!, accountServiceMock!!, apiTransactionManagerMock!!)
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

        mockkStatic(Burst::class)
        val fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        every { Burst.fluxCapacitor } returns fluxCapacitor

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) }, apiTransactionManagerMock!!) as Attachment.DigitalGoodsDelivery
        assertNotNull(attachment)

        assertEquals(DELIVERY, attachment.transactionType)
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
        whenever(mockPurchase.quantity).doReturn(9)
        whenever(mockPurchase.priceNQT).doReturn(1L)

        whenever(mockPurchase.isPending).doReturn(true)

        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSellerAccount)
        whenever(parameterServiceMock!!.getPurchase(eq<HttpServletRequest>(request))).doReturn(mockPurchase)

        assertEquals(INCORRECT_DGS_GOODS, t!!.processRequest(request))
    }

}
