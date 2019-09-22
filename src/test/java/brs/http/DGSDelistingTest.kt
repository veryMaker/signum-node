package brs.http

import brs.*
import brs.DigitalGoodsStore.Goods
import brs.common.QuickMocker
import brs.fluxcapacitor.FluxValues
import brs.services.ParameterService
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.transaction.TransactionType.DigitalGoods.DELISTING
import brs.http.JSONResponses.UNKNOWN_GOODS
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class DGSDelistingTest : AbstractTransactionTest() {

    private var t: DGSDelisting? = null

    private var mockParameterService: ParameterService? = null
    private var mockBlockchain: Blockchain? = null
    private var apiTransactionManagerMock: APITransactionManager? = null

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        mockBlockchain = mock<Blockchain>()
        apiTransactionManagerMock = mock<APITransactionManager>()

        t = DGSDelisting(mockParameterService!!, mockBlockchain!!, apiTransactionManagerMock!!)
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

        assertEquals(DELISTING, attachment.transactionType)
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
