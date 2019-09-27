package brs.http

import brs.Account
import brs.Attachment
import brs.Blockchain
import brs.DependencyProvider
import brs.DigitalGoodsStore.Goods
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxValues
import brs.http.JSONResponses.INCORRECT_DELIVERY_DEADLINE_TIMESTAMP
import brs.http.JSONResponses.INCORRECT_PURCHASE_PRICE
import brs.http.JSONResponses.INCORRECT_PURCHASE_QUANTITY
import brs.http.JSONResponses.MISSING_DELIVERY_DEADLINE_TIMESTAMP
import brs.http.JSONResponses.UNKNOWN_GOODS
import brs.http.common.Parameters.DELIVERY_DEADLINE_TIMESTAMP_PARAMETER
import brs.http.common.Parameters.PRICE_NQT_PARAMETER
import brs.http.common.Parameters.QUANTITY_PARAMETER
import brs.services.AccountService
import brs.services.ParameterService
import brs.services.TimeService
import brs.transaction.TransactionType
import brs.transaction.digitalGoods.DigitalGoodsPurchase
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import javax.servlet.http.HttpServletRequest

class DGSPurchaseTest : AbstractTransactionTest() {

    private lateinit var t: DGSPurchase
    private lateinit var dp: DependencyProvider
    private lateinit var mockParameterService: ParameterService
    private lateinit var mockBlockchain: Blockchain
    private lateinit var mockAccountService: AccountService
    private lateinit var mockTimeService: TimeService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        whenever(mockParameterService!!.getSenderAccount(any())).doReturn(mock())
        mockBlockchain = mock<Blockchain>()
        mockAccountService = mock<AccountService>()
        mockTimeService = mock<TimeService>()
        apiTransactionManagerMock = mock<APITransactionManager>()
        dp = QuickMocker.dependencyProvider(mockParameterService!!, mockBlockchain!!, mockAccountService!!, mockTimeService!!, apiTransactionManagerMock!!)
        t = DGSPurchase(dp)
    }

    @Test
    fun processRequest() {
        val goodsQuantity = 5
        val goodsPrice = 10L
        val deliveryDeadlineTimestamp: Long = 100

        val request = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_NQT_PARAMETER, goodsPrice),
                MockParam(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER, deliveryDeadlineTimestamp)
        )

        val mockSellerId = 123L
        val mockGoodsId = 123L
        val mockGoods = mock<Goods>()
        whenever(mockGoods.id).doReturn(mockGoodsId)
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.quantity).doReturn(10)
        whenever(mockGoods.priceNQT).doReturn(10L)
        whenever(mockGoods.sellerId).doReturn(mockSellerId)

        val mockSellerAccount = mock<Account>()

        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)
        whenever(mockTimeService!!.epochTime).doReturn(10)

        whenever(mockAccountService!!.getAccount(eq(mockSellerId))).doReturn(mockSellerAccount)
        dp.fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) }, apiTransactionManagerMock!!) as Attachment.DigitalGoodsPurchase
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is DigitalGoodsPurchase)
        assertEquals(goodsQuantity.toLong(), attachment.quantity.toLong())
        assertEquals(goodsPrice, attachment.priceNQT)
        assertEquals(deliveryDeadlineTimestamp, attachment.deliveryDeadlineTimestamp.toLong())
        assertEquals(mockGoodsId, attachment.goodsId)
    }

    @Test
    fun processRequest_unknownGoods() {
        val request = QuickMocker.httpServletRequest()

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(true)

        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        assertEquals(UNKNOWN_GOODS, t!!.processRequest(request))
    }

    @Test
    fun processRequest_incorrectPurchaseQuantity() {
        val goodsQuantity = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity)
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.quantity).doReturn(4)

        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        assertEquals(INCORRECT_PURCHASE_QUANTITY, t!!.processRequest(request))
    }

    @Test
    fun processRequest_incorrectPurchasePrice() {
        val goodsQuantity = 5
        val goodsPrice = 5L

        val request = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_NQT_PARAMETER, goodsPrice)
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.quantity).doReturn(10)
        whenever(mockGoods.priceNQT).doReturn(10L)

        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        assertEquals(INCORRECT_PURCHASE_PRICE, t!!.processRequest(request))
    }


    @Test
    fun processRequest_missingDeliveryDeadlineTimestamp() {
        val goodsQuantity = 5
        val goodsPrice = 10L

        val request = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_NQT_PARAMETER, goodsPrice)
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.quantity).doReturn(10)
        whenever(mockGoods.priceNQT).doReturn(10L)

        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        assertEquals(MISSING_DELIVERY_DEADLINE_TIMESTAMP, t!!.processRequest(request))
    }

    @Test
    fun processRequest_incorrectDeliveryDeadlineTimestamp_unParsable() {
        val goodsQuantity = 5
        val goodsPrice = 10L

        val request = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_NQT_PARAMETER, goodsPrice),
                MockParam(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER, "unParsable")
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.quantity).doReturn(10)
        whenever(mockGoods.priceNQT).doReturn(10L)

        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        assertEquals(INCORRECT_DELIVERY_DEADLINE_TIMESTAMP, t!!.processRequest(request))
    }

    @Test
    fun processRequest_incorrectDeliveryDeadlineTimestamp_beforeCurrentTime() {
        val goodsQuantity = 5
        val goodsPrice = 10L
        val deliveryDeadlineTimestamp: Long = 100

        val request = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_NQT_PARAMETER, goodsPrice),
                MockParam(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER, deliveryDeadlineTimestamp)
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.quantity).doReturn(10)
        whenever(mockGoods.priceNQT).doReturn(10L)

        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)
        whenever(mockTimeService!!.epochTime).doReturn(1000)

        assertEquals(INCORRECT_DELIVERY_DEADLINE_TIMESTAMP, t!!.processRequest(request))
    }
}
