package brs.http

import brs.*
import brs.DigitalGoodsStore.Goods
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxCapacitor
import brs.fluxcapacitor.FluxValues
import brs.services.AccountService
import brs.services.ParameterService
import brs.services.TimeService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import javax.servlet.http.HttpServletRequest

import brs.TransactionType.DigitalGoods.PURCHASE
import brs.http.JSONResponses.INCORRECT_DELIVERY_DEADLINE_TIMESTAMP
import brs.http.JSONResponses.INCORRECT_PURCHASE_PRICE
import brs.http.JSONResponses.INCORRECT_PURCHASE_QUANTITY
import brs.http.JSONResponses.MISSING_DELIVERY_DEADLINE_TIMESTAMP
import brs.http.JSONResponses.UNKNOWN_GOODS
import brs.http.common.Parameters.*
import com.nhaarman.mockitokotlin2.*
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class DGSPurchaseTest : AbstractTransactionTest() {

    private var t: DGSPurchase? = null

    private var mockParameterService: ParameterService? = null
    private var mockBlockchain: Blockchain? = null
    private var mockAccountService: AccountService? = null
    private var mockTimeService: TimeService? = null
    private var apiTransactionManagerMock: APITransactionManager? = null

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        whenever(mockParameterService!!.getSenderAccount(any())).doReturn(mock())
        mockBlockchain = mock<Blockchain>()
        mockAccountService = mock<AccountService>()
        mockTimeService = mock<TimeService>()
        apiTransactionManagerMock = mock<APITransactionManager>()

        t = DGSPurchase(mockParameterService!!, mockBlockchain!!, mockAccountService!!, mockTimeService!!, apiTransactionManagerMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val goodsQuantity = 5
        val goodsPrice = 10L
        val deliveryDeadlineTimestamp: Long = 100

        val req = QuickMocker.httpServletRequest(
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

        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(req))).doReturn(mockGoods)
        whenever(mockTimeService!!.epochTime).doReturn(10)

        whenever(mockAccountService!!.getAccount(eq(mockSellerId))).doReturn(mockSellerAccount)

        mockkStatic(Burst::class)
        val fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        every { Burst.getFluxCapacitor() } returns fluxCapacitor

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(req) }, apiTransactionManagerMock!!) as Attachment.DigitalGoodsPurchase
        assertNotNull(attachment)

        assertEquals(PURCHASE, attachment.transactionType)
        assertEquals(goodsQuantity.toLong(), attachment.quantity.toLong())
        assertEquals(goodsPrice, attachment.priceNQT)
        assertEquals(deliveryDeadlineTimestamp, attachment.deliveryDeadlineTimestamp.toLong())
        assertEquals(mockGoodsId, attachment.goodsId)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_unknownGoods() {
        val req = QuickMocker.httpServletRequest()

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(true)

        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(req))).doReturn(mockGoods)

        assertEquals(UNKNOWN_GOODS, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectPurchaseQuantity() {
        val goodsQuantity = 5

        val req = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity)
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.quantity).doReturn(4)

        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(req))).doReturn(mockGoods)

        assertEquals(INCORRECT_PURCHASE_QUANTITY, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectPurchasePrice() {
        val goodsQuantity = 5
        val goodsPrice = 5L

        val req = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_NQT_PARAMETER, goodsPrice)
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.quantity).doReturn(10)
        whenever(mockGoods.priceNQT).doReturn(10L)

        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(req))).doReturn(mockGoods)

        assertEquals(INCORRECT_PURCHASE_PRICE, t!!.processRequest(req))
    }


    @Test
    @Throws(BurstException::class)
    fun processRequest_missingDeliveryDeadlineTimestamp() {
        val goodsQuantity = 5
        val goodsPrice = 10L

        val req = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_NQT_PARAMETER, goodsPrice)
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.quantity).doReturn(10)
        whenever(mockGoods.priceNQT).doReturn(10L)

        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(req))).doReturn(mockGoods)

        assertEquals(MISSING_DELIVERY_DEADLINE_TIMESTAMP, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectDeliveryDeadlineTimestamp_unParsable() {
        val goodsQuantity = 5
        val goodsPrice = 10L

        val req = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_NQT_PARAMETER, goodsPrice),
                MockParam(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER, "unParsable")
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.quantity).doReturn(10)
        whenever(mockGoods.priceNQT).doReturn(10L)

        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(req))).doReturn(mockGoods)

        assertEquals(INCORRECT_DELIVERY_DEADLINE_TIMESTAMP, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectDeliveryDeadlineTimestamp_beforeCurrentTime() {
        val goodsQuantity = 5
        val goodsPrice = 10L
        val deliveryDeadlineTimestamp: Long = 100

        val req = QuickMocker.httpServletRequest(
                MockParam(QUANTITY_PARAMETER, goodsQuantity),
                MockParam(PRICE_NQT_PARAMETER, goodsPrice),
                MockParam(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER, deliveryDeadlineTimestamp)
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.quantity).doReturn(10)
        whenever(mockGoods.priceNQT).doReturn(10L)

        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(req))).doReturn(mockGoods)
        whenever(mockTimeService!!.epochTime).doReturn(1000)

        assertEquals(INCORRECT_DELIVERY_DEADLINE_TIMESTAMP, t!!.processRequest(req))
    }
}
