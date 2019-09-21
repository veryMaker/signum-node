package brs.http

import brs.*
import brs.DigitalGoodsStore.Goods
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxCapacitor
import brs.fluxcapacitor.FluxValues
import brs.http.JSONResponses.INCORRECT_DELTA_QUANTITY
import brs.http.JSONResponses.MISSING_DELTA_QUANTITY
import brs.http.JSONResponses.UNKNOWN_GOODS
import brs.services.ParameterService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import javax.servlet.http.HttpServletRequest
import brs.http.common.Parameters.DELTA_QUANTITY_PARAMETER
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DGSQuantityChangeTest : AbstractTransactionTest() {

    private var t: DGSQuantityChange? = null

    private var mockParameterService: ParameterService? = null
    private var mockBlockchain: Blockchain? = null
    private var apiTransactionManagerMock: APITransactionManager? = null

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        mockBlockchain = mock<Blockchain>()
        apiTransactionManagerMock = mock<APITransactionManager>()

        t = DGSQuantityChange(mockParameterService!!, mockBlockchain!!, apiTransactionManagerMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val deltaQualityParameter = 5
        val request = QuickMocker.httpServletRequest(
                MockParam(DELTA_QUANTITY_PARAMETER, deltaQualityParameter)
        )

        val mockGoodsID = 123L
        val mockGoods = mock<Goods>()
        whenever(mockGoods.id).doReturn(mockGoodsID)
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.sellerId).doReturn(1L)

        val mockSenderAccount = mock<Account>()
        whenever(mockSenderAccount.id).doReturn(1L)

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSenderAccount)
        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) }, apiTransactionManagerMock!!) as Attachment.DigitalGoodsQuantityChange
        assertNotNull(attachment)

        attachment.transactionType
        assertEquals(mockGoodsID, attachment.goodsId)
        assertEquals(deltaQualityParameter.toLong(), attachment.deltaQuantity.toLong())
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_unknownGoodsBecauseDelisted() {
        val request = QuickMocker.httpServletRequest()

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(true)

        val mockSenderAccount = mock<Account>()

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSenderAccount)
        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        assertEquals(UNKNOWN_GOODS, t!!.processRequest(request))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_unknownGoodsBecauseWrongSellerId() {
        val request = QuickMocker.httpServletRequest()

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.sellerId).doReturn(1L)

        val mockSenderAccount = mock<Account>()
        whenever(mockSenderAccount.id).doReturn(2L)

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSenderAccount)
        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        assertEquals(UNKNOWN_GOODS, t!!.processRequest(request))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_missingDeltaQuantity() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DELTA_QUANTITY_PARAMETER, null as String?)
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.sellerId).doReturn(1L)

        val mockSenderAccount = mock<Account>()
        whenever(mockSenderAccount.id).doReturn(1L)

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSenderAccount)
        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        assertEquals(MISSING_DELTA_QUANTITY, t!!.processRequest(request))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_deltaQuantityWrongFormat() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DELTA_QUANTITY_PARAMETER, "Bob")
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.sellerId).doReturn(1L)

        val mockSenderAccount = mock<Account>()
        whenever(mockSenderAccount.id).doReturn(1L)

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSenderAccount)
        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        assertEquals(INCORRECT_DELTA_QUANTITY, t!!.processRequest(request))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_deltaQuantityOverMaxIncorrectDeltaQuantity() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DELTA_QUANTITY_PARAMETER, Integer.MIN_VALUE)
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.sellerId).doReturn(1L)

        val mockSenderAccount = mock<Account>()
        whenever(mockSenderAccount.id).doReturn(1L)

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSenderAccount)
        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        assertEquals(INCORRECT_DELTA_QUANTITY, t!!.processRequest(request))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_deltaQuantityLowerThanNegativeMaxIncorrectDeltaQuantity() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DELTA_QUANTITY_PARAMETER, Integer.MAX_VALUE)
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.sellerId).doReturn(1L)

        val mockSenderAccount = mock<Account>()
        whenever(mockSenderAccount.id).doReturn(1L)

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSenderAccount)
        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        assertEquals(INCORRECT_DELTA_QUANTITY, t!!.processRequest(request))
    }

}
