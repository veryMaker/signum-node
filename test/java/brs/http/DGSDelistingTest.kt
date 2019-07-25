package brs.http

import brs.*
import brs.DigitalGoodsStore.Goods
import brs.common.QuickMocker
import brs.fluxcapacitor.FluxCapacitor
import brs.fluxcapacitor.FluxValues
import brs.services.ParameterService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import javax.servlet.http.HttpServletRequest

import brs.TransactionType.DigitalGoods.DELISTING
import brs.http.JSONResponses.UNKNOWN_GOODS
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
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
    @Throws(BurstException::class)
    fun processRequest() {
        val req = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        val mockGoods = mock<Goods>()

        whenever(mockGoods.isDelisted).thenReturn(false)
        whenever(mockGoods.sellerId).thenReturn(1L)
        whenever(mockAccount.getId()).thenReturn(1L)

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(req))).thenReturn(mockAccount)
        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(req))).thenReturn(mockGoods)

        QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(req) }, apiTransactionManagerMock!!) as Attachment.DigitalGoodsDelisting
        assertNotNull(attachment)

        assertEquals(DELISTING, attachment.transactionType)
        assertEquals(mockGoods.id, attachment.goodsId)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_goodsDelistedUnknownGoods() {
        val req = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        val mockGoods = mock<Goods>()

        whenever(mockGoods.isDelisted).thenReturn(true)

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(req))).thenReturn(mockAccount)
        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(req))).thenReturn(mockGoods)

        assertEquals(UNKNOWN_GOODS, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_otherSellerIdUnknownGoods() {
        val req = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        val mockGoods = mock<Goods>()

        whenever(mockGoods.isDelisted).thenReturn(false)
        whenever(mockGoods.sellerId).thenReturn(1L)
        whenever(mockAccount.getId()).thenReturn(2L)

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(req))).thenReturn(mockAccount)
        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(req))).thenReturn(mockGoods)

        assertEquals(UNKNOWN_GOODS, t!!.processRequest(req))
    }

}
