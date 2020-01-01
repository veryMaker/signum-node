package brs.api.http

import brs.entity.Account
import brs.transaction.appendix.Attachment
import brs.services.BlockchainService
import brs.entity.DependencyProvider
import brs.entity.Goods
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.objects.FluxValues
import brs.api.http.common.JSONResponses.UNKNOWN_GOODS
import brs.api.http.common.Parameters.PRICE_PLANCK_PARAMETER
import brs.services.ParameterService
import brs.transaction.type.TransactionType
import brs.transaction.type.digitalGoods.DigitalGoodsPriceChange
import io.mockk.mockk
import io.mockk.every
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
    private lateinit var blockchainServiceMock: BlockchainService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        parameterServiceMock = mockk(relaxed = true)
        blockchainServiceMock = mockk(relaxed = true)
        apiTransactionManagerMock = mockk(relaxed = true)
        dp = QuickMocker.dependencyProvider(parameterServiceMock, blockchainServiceMock, apiTransactionManagerMock)
        t = DGSPriceChange(dp)
    }

    @Test
    fun processRequest() {
        val pricePlanckParameter = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_PLANCK_PARAMETER, pricePlanckParameter)
        )

        val mockAccount = mockk<Account>(relaxed = true)
        every { mockAccount.id } returns 1L

        val mockGoodsId: Long = 123
        val mockGoods = mockk<Goods>(relaxed = true)
        every { mockGoods.id } returns mockGoodsId
        every { mockGoods.sellerId } returns 1L
        every { mockGoods.isDelisted } returns false

        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockAccount
        every { parameterServiceMock.getGoods(eq(request)) } returns mockGoods

        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
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

        val mockAccount = mockk<Account>(relaxed = true)

        val mockGoods = mockk<Goods>(relaxed = true)
        every { mockGoods.isDelisted } returns true

        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockAccount
        every { parameterServiceMock.getGoods(eq(request)) } returns mockGoods

        assertEquals(UNKNOWN_GOODS, t.processRequest(request))
    }

    @Test
    fun processRequest_goodsWrongSellerIdUnknownGoods() {
        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_PLANCK_PARAMETER, 123L)
        )

        val mockAccount = mockk<Account>(relaxed = true)
        every { mockAccount.id } returns 1L

        val mockGoods = mockk<Goods>(relaxed = true)
        every { mockGoods.sellerId } returns 2L
        every { mockGoods.isDelisted } returns false

        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockAccount
        every { parameterServiceMock.getGoods(eq(request)) } returns mockGoods

        assertEquals(UNKNOWN_GOODS, t.processRequest(request))
    }
}
