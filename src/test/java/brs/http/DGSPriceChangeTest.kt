package brs.http

import brs.*
import brs.DigitalGoodsStore.Goods
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxValues
import brs.services.ParameterService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import javax.servlet.http.HttpServletRequest

import brs.transaction.TransactionType.DigitalGoods.PRICE_CHANGE
import brs.http.JSONResponses.UNKNOWN_GOODS
import brs.http.common.Parameters.PRICE_NQT_PARAMETER
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DGSPriceChangeTest : AbstractTransactionTest() {

    private var t: DGSPriceChange? = null

    private var parameterServiceMock: ParameterService? = null
    private var blockchainMock: Blockchain? = null
    private var apiTransactionManagerMock: APITransactionManager? = null

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        blockchainMock = mock<Blockchain>()
        apiTransactionManagerMock = mock<APITransactionManager>()

        t = DGSPriceChange(parameterServiceMock!!, blockchainMock!!, apiTransactionManagerMock!!)
    }

    @Test
    fun processRequest() {
        val priceNQTParameter = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, priceNQTParameter)
        )

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(1L)

        val mockGoodsId: Long = 123
        val mockGoods = mock<Goods>()
        whenever(mockGoods.id).doReturn(mockGoodsId)
        whenever(mockGoods.sellerId).doReturn(1L)
        whenever(mockGoods.isDelisted).doReturn(false)

        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)
        whenever(parameterServiceMock!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) }, apiTransactionManagerMock!!) as Attachment.DigitalGoodsPriceChange
        assertNotNull(attachment)

        assertEquals(PRICE_CHANGE, attachment.transactionType)
        assertEquals(mockGoodsId, attachment.goodsId)
        assertEquals(priceNQTParameter.toLong(), attachment.priceNQT)
    }

    @Test
    fun processRequest_goodsDelistedUnknownGoods() {
        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, 123L)
        )

        val mockAccount = mock<Account>()

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(true)

        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)
        whenever(parameterServiceMock!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        assertEquals(UNKNOWN_GOODS, t!!.processRequest(request))
    }

    @Test
    fun processRequest_goodsWrongSellerIdUnknownGoods() {
        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, 123L)
        )

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(1L)

        val mockGoods = mock<Goods>()
        whenever(mockGoods.sellerId).doReturn(2L)
        whenever(mockGoods.isDelisted).doReturn(false)

        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)
        whenever(parameterServiceMock!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        assertEquals(UNKNOWN_GOODS, t!!.processRequest(request))
    }

}
