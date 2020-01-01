package brs.api.http

import brs.api.http.common.JSONResponses.UNKNOWN_GOODS
import brs.common.QuickMocker
import brs.entity.Account
import brs.entity.DependencyProvider
import brs.entity.Goods
import brs.objects.FluxValues
import brs.services.BlockchainService
import brs.services.ParameterService
import brs.transaction.appendix.Attachment
import brs.transaction.type.TransactionType
import brs.transaction.type.digitalGoods.DigitalGoodsDelisting
import io.mockk.every
import io.mockk.mockk
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
        mockParameterService = mockk(relaxed = true)
        mockBlockchainService = mockk(relaxed = true)
        apiTransactionManagerMock = mockk(relaxed = true)
        dp = QuickMocker.dependencyProvider(mockParameterService, mockBlockchainService, apiTransactionManagerMock)
        t = DGSDelisting(dp)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mockk<Account>(relaxed = true)
        val mockGoods = mockk<Goods>(relaxed = true)

        every { mockGoods.id } returns 1L
        every { mockGoods.isDelisted } returns false
        every { mockGoods.sellerId } returns 1L
        every { mockAccount.id } returns 1L

        every { mockParameterService.getSenderAccount(eq(request)) } returns mockAccount
        every { mockParameterService.getGoods(eq(request)) } returns mockGoods
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

        val mockAccount = mockk<Account>(relaxed = true)
        val mockGoods = mockk<Goods>(relaxed = true)

        every { mockGoods.isDelisted } returns true

        every { mockParameterService.getSenderAccount(eq(request)) } returns mockAccount
        every { mockParameterService.getGoods(eq(request)) } returns mockGoods

        assertEquals(UNKNOWN_GOODS, t.processRequest(request))
    }

    @Test
    fun processRequest_otherSellerIdUnknownGoods() {
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mockk<Account>(relaxed = true)
        val mockGoods = mockk<Goods>(relaxed = true)

        every { mockGoods.isDelisted } returns false
        every { mockGoods.sellerId } returns 1L
        every { mockAccount.id } returns 2L

        every { mockParameterService.getSenderAccount(eq(request)) } returns mockAccount
        every { mockParameterService.getGoods(eq(request)) } returns mockGoods

        assertEquals(UNKNOWN_GOODS, t.processRequest(request))
    }
}
