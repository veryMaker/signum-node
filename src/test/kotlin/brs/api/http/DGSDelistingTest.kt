package brs.api.http

import brs.entity.Account
import brs.transaction.appendix.Attachment
import brs.services.BlockchainService
import brs.entity.DependencyProvider
import brs.entity.Goods
import brs.common.QuickMocker
import brs.objects.FluxValues
import brs.api.http.JSONResponses.UNKNOWN_GOODS
import brs.services.ParameterService
import brs.transaction.type.TransactionType
import brs.transaction.type.digitalGoods.DigitalGoodsDelisting
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DGSDelistingTest : AbstractTransactionTest() {

    private lateinit var t: DGSDelisting
    private lateinit var dp: DependencyProvider
    private lateinit var mockParameterService: ParameterService
    private lateinit var mockBlockchainService: BlockchainService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        mockParameterService = mock()
        mockBlockchainService = mock()
        apiTransactionManagerMock = mock()
        dp = QuickMocker.dependencyProvider(mockParameterService, mockBlockchainService, apiTransactionManagerMock)
        t = DGSDelisting(dp)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        val mockGoods = mock<Goods>()

        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.sellerId).doReturn(1L)
        whenever(mockAccount.id).doReturn(1L)

        whenever(mockParameterService.getSenderAccount(eq(request))).doReturn(mockAccount)
        whenever(mockParameterService.getGoods(eq(request))).doReturn(mockGoods)
        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.DigitalGoodsDelisting
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

        whenever(mockParameterService.getSenderAccount(eq(request))).doReturn(mockAccount)
        whenever(mockParameterService.getGoods(eq(request))).doReturn(mockGoods)

        assertEquals(UNKNOWN_GOODS, t.processRequest(request))
    }

    @Test
    fun processRequest_otherSellerIdUnknownGoods() {
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        val mockGoods = mock<Goods>()

        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.sellerId).doReturn(1L)
        whenever(mockAccount.id).doReturn(2L)

        whenever(mockParameterService.getSenderAccount(eq(request))).doReturn(mockAccount)
        whenever(mockParameterService.getGoods(eq(request))).doReturn(mockGoods)

        assertEquals(UNKNOWN_GOODS, t.processRequest(request))
    }

}
