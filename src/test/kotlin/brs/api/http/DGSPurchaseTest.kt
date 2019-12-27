package brs.api.http

import brs.entity.Account
import brs.transaction.appendix.Attachment
import brs.services.BlockchainService
import brs.entity.DependencyProvider
import brs.entity.Goods
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.objects.FluxValues
import brs.api.http.JSONResponses.INCORRECT_DELIVERY_DEADLINE_TIMESTAMP
import brs.api.http.JSONResponses.INCORRECT_PURCHASE_PRICE
import brs.api.http.JSONResponses.INCORRECT_PURCHASE_QUANTITY
import brs.api.http.JSONResponses.MISSING_DELIVERY_DEADLINE_TIMESTAMP
import brs.api.http.JSONResponses.UNKNOWN_GOODS
import brs.api.http.common.Parameters.DELIVERY_DEADLINE_TIMESTAMP_PARAMETER
import brs.api.http.common.Parameters.PRICE_PLANCK_PARAMETER
import brs.api.http.common.Parameters.QUANTITY_PARAMETER
import brs.services.AccountService
import brs.services.ParameterService
import brs.services.TimeService
import brs.transaction.type.TransactionType
import brs.transaction.type.digitalGoods.DigitalGoodsPurchase
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DGSPurchaseTest : AbstractTransactionTest() {

    private lateinit var t: DGSPurchase
    private lateinit var dp: DependencyProvider
    private lateinit var mockParameterService: ParameterService
    private lateinit var mockBlockchainService: BlockchainService
    private lateinit var mockAccountService: AccountService
    private lateinit var mockTimeService: TimeService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        mockParameterService = mockk()
        every { mockParameterService.getSenderAccount(any()) } returns mockk()
        mockBlockchainService = mockk()
        mockAccountService = mockk()
        mockTimeService = mockk()
        apiTransactionManagerMock = mockk()
        dp = QuickMocker.dependencyProvider(
            mockParameterService,
            mockBlockchainService,
            mockAccountService,
            mockTimeService,
            apiTransactionManagerMock
        )
        t = DGSPurchase(dp)
    }

    @Test
    fun processRequest() {
        val goodsQuantity = 5
        val goodsPrice = 10L
        val deliveryDeadlineTimestamp: Long = 100

        val request = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_PLANCK_PARAMETER, goodsPrice),
                MockParam(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER, deliveryDeadlineTimestamp)
        )

        val mockSellerId = 123L
        val mockGoodsId = 123L
        val mockGoods = mockk<Goods>()
        every { mockGoods.id } returns mockGoodsId
        every { mockGoods.isDelisted } returns false
        every { mockGoods.quantity } returns 10
        every { mockGoods.pricePlanck } returns 10L
        every { mockGoods.sellerId } returns mockSellerId

        val mockSellerAccount = mockk<Account>()

        every { mockParameterService.getGoods(eq(request)) } returns mockGoods
        every { mockTimeService.epochTime } returns 10

        every { mockAccountService.getAccount(eq(mockSellerId)) } returns mockSellerAccount
        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.DigitalGoodsPurchase
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is DigitalGoodsPurchase)
        assertEquals(goodsQuantity.toLong(), attachment.quantity.toLong())
        assertEquals(goodsPrice, attachment.pricePlanck)
        assertEquals(deliveryDeadlineTimestamp, attachment.deliveryDeadlineTimestamp.toLong())
        assertEquals(mockGoodsId, attachment.goodsId)
    }

    @Test
    fun processRequest_unknownGoods() {
        val request = QuickMocker.httpServletRequest()

        val mockGoods = mockk<Goods>()
        every { mockGoods.isDelisted } returns true

        every { mockParameterService.getGoods(eq(request)) } returns mockGoods

        assertEquals(UNKNOWN_GOODS, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectPurchaseQuantity() {
        val goodsQuantity = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity)
        )

        val mockGoods = mockk<Goods>()
        every { mockGoods.isDelisted } returns false
        every { mockGoods.quantity } returns 4

        every { mockParameterService.getGoods(eq(request)) } returns mockGoods

        assertEquals(INCORRECT_PURCHASE_QUANTITY, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectPurchasePrice() {
        val goodsQuantity = 5
        val goodsPrice = 5L

        val request = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_PLANCK_PARAMETER, goodsPrice)
        )

        val mockGoods = mockk<Goods>()
        every { mockGoods.isDelisted } returns false
        every { mockGoods.quantity } returns 10
        every { mockGoods.pricePlanck } returns 10L

        every { mockParameterService.getGoods(eq(request)) } returns mockGoods

        assertEquals(INCORRECT_PURCHASE_PRICE, t.processRequest(request))
    }


    @Test
    fun processRequest_missingDeliveryDeadlineTimestamp() {
        val goodsQuantity = 5
        val goodsPrice = 10L

        val request = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_PLANCK_PARAMETER, goodsPrice)
        )

        val mockGoods = mockk<Goods>()
        every { mockGoods.isDelisted } returns false
        every { mockGoods.quantity } returns 10
        every { mockGoods.pricePlanck } returns 10L

        every { mockParameterService.getGoods(eq(request)) } returns mockGoods

        assertEquals(MISSING_DELIVERY_DEADLINE_TIMESTAMP, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectDeliveryDeadlineTimestamp_unParsable() {
        val goodsQuantity = 5
        val goodsPrice = 10L

        val request = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_PLANCK_PARAMETER, goodsPrice),
                MockParam(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER, "unParsable")
        )

        val mockGoods = mockk<Goods>()
        every { mockGoods.isDelisted } returns false
        every { mockGoods.quantity } returns 10
        every { mockGoods.pricePlanck } returns 10L

        every { mockParameterService.getGoods(eq(request)) } returns mockGoods

        assertEquals(INCORRECT_DELIVERY_DEADLINE_TIMESTAMP, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectDeliveryDeadlineTimestamp_beforeCurrentTime() {
        val goodsQuantity = 5
        val goodsPrice = 10L
        val deliveryDeadlineTimestamp: Long = 100

        val request = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_PLANCK_PARAMETER, goodsPrice),
                MockParam(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER, deliveryDeadlineTimestamp)
        )

        val mockGoods = mockk<Goods>()
        every { mockGoods.isDelisted } returns false
        every { mockGoods.quantity } returns 10
        every { mockGoods.pricePlanck } returns 10L

        every { mockParameterService.getGoods(eq(request)) } returns mockGoods
        every { mockTimeService.epochTime } returns 1000

        assertEquals(INCORRECT_DELIVERY_DEADLINE_TIMESTAMP, t.processRequest(request))
    }
}
