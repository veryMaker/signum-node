package brs.http

import brs.*
import brs.DigitalGoodsStore.Goods
import brs.common.QuickMocker
import brs.fluxcapacitor.FluxValues
import brs.services.ParameterService
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.UNKNOWN_GOODS
import brs.transaction.digitalGoods.DigitalGoodsDelisting
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*

class DGSDelistingTest : AbstractTransactionTest() {

    private lateinit var t: DGSDelisting

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockBlockchain: Blockchain
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        mockBlockchain = mock<Blockchain>()
        apiTransactionManagerMock = mock<APITransactionManager>()

        t = DGSDelisting(QuickMocker.dependencyProvider(mockParameterService!!, mockBlockchain!!, apiTransactionManagerMock!!))
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        val mockGoods = mock<Goods>()

        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.sellerId).doReturn(1L)
        whenever(mockAccount.id).doReturn(1L)

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)
        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) }, apiTransactionManagerMock!!) as Attachment.DigitalGoodsDelisting
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is DigitalGoodsDelisting)
        assertEquals(mockGoods.id, attachment.goodsId)
    }

    @Test
    fun processRequest_goodsDelistedUnknownGoods() {
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        val mockGoods = mock<Goods>()

        whenever(mockGoods.isDelisted).doReturn(true)

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)
        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        assertEquals(UNKNOWN_GOODS, t!!.processRequest(request))
    }

    @Test
    fun processRequest_otherSellerIdUnknownGoods() {
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        val mockGoods = mock<Goods>()

        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.sellerId).doReturn(1L)
        whenever(mockAccount.id).doReturn(2L)

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)
        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        assertEquals(UNKNOWN_GOODS, t!!.processRequest(request))
    }

}
