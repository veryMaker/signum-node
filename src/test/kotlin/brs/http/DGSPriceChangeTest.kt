package brs.http

import brs.Account
import brs.Attachment
import brs.Blockchain
import brs.DependencyProvider
import brs.DigitalGoodsStore.Goods
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxValues
import brs.http.JSONResponses.UNKNOWN_GOODS
import brs.http.common.Parameters.PRICE_PLANCK_PARAMETER
import brs.services.ParameterService
import brs.transaction.TransactionType
import brs.transaction.digitalGoods.DigitalGoodsPriceChange
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DGSPriceChangeTest : AbstractTransactionTest() {

    private lateinit var t: DGSPriceChange
    private lateinit var dp: DependencyProvider
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var blockchainMock: Blockchain
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        parameterServiceMock = mock()
        blockchainMock = mock()
        apiTransactionManagerMock = mock()
        dp = QuickMocker.dependencyProvider(parameterServiceMock, blockchainMock, apiTransactionManagerMock)
        t = DGSPriceChange(dp)
    }

    @Test
    fun processRequest() {
        val pricePlanckParameter = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_PLANCK_PARAMETER, pricePlanckParameter)
        )

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(1L)

        val mockGoodsId: Long = 123
        val mockGoods = mock<Goods>()
        whenever(mockGoods.id).doReturn(mockGoodsId)
        whenever(mockGoods.sellerId).doReturn(1L)
        whenever(mockGoods.isDelisted).doReturn(false)

        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(mockAccount)
        whenever(parameterServiceMock.getGoods(eq(request))).doReturn(mockGoods)

        dp.fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.DigitalGoodsPriceChange
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is DigitalGoodsPriceChange)
        assertEquals(mockGoodsId, attachment.goodsId)
        assertEquals(pricePlanckParameter.toLong(), attachment.pricePlanck)
    }

    @Test
    fun processRequest_goodsDelistedUnknownGoods() {
        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_PLANCK_PARAMETER, 123L)
        )

        val mockAccount = mock<Account>()

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(true)

        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(mockAccount)
        whenever(parameterServiceMock.getGoods(eq(request))).doReturn(mockGoods)

        assertEquals(UNKNOWN_GOODS, t.processRequest(request))
    }

    @Test
    fun processRequest_goodsWrongSellerIdUnknownGoods() {
        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_PLANCK_PARAMETER, 123L)
        )

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(1L)

        val mockGoods = mock<Goods>()
        whenever(mockGoods.sellerId).doReturn(2L)
        whenever(mockGoods.isDelisted).doReturn(false)

        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(mockAccount)
        whenever(parameterServiceMock.getGoods(eq(request))).doReturn(mockGoods)

        assertEquals(UNKNOWN_GOODS, t.processRequest(request))
    }

}
