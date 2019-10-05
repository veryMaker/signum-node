package brs.http

import brs.Account
import brs.Attachment
import brs.Blockchain
import brs.DependencyProvider
import brs.DigitalGoodsStore.Goods
import brs.common.QuickMocker
import brs.fluxcapacitor.FluxValues
import brs.http.JSONResponses.UNKNOWN_GOODS
import brs.services.ParameterService
import brs.transaction.TransactionType
import brs.transaction.digitalGoods.DigitalGoodsDelisting
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import javax.servlet.http.HttpServletRequest

class DGSDelistingTest : AbstractTransactionTest() {

    private lateinit var t: DGSDelisting
    private lateinit var dp: DependencyProvider
    private lateinit var mockParameterService: ParameterService
    private lateinit var mockBlockchain: Blockchain
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        mockBlockchain = mock<Blockchain>()
        apiTransactionManagerMock = mock<APITransactionManager>()
        dp = QuickMocker.dependencyProvider(mockParameterService!!, mockBlockchain!!, apiTransactionManagerMock!!)
        t = DGSDelisting(dp)
    }

    @Test
    fun processRequest() = runBlocking {
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        val mockGoods = mock<Goods>()

        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.sellerId).doReturn(1L)
        whenever(mockAccount.id).doReturn(1L)

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)
        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)
        dp.fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) }, apiTransactionManagerMock!!) as Attachment.DigitalGoodsDelisting
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is DigitalGoodsDelisting)
        assertEquals(mockGoods.id, attachment.goodsId)
    }

    @Test
    fun processRequest_goodsDelistedUnknownGoods() = runBlocking {
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        val mockGoods = mock<Goods>()

        whenever(mockGoods.isDelisted).doReturn(true)

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)
        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        assertEquals(UNKNOWN_GOODS, t!!.processRequest(request))
    }

    @Test
    fun processRequest_otherSellerIdUnknownGoods() = runBlocking {
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
