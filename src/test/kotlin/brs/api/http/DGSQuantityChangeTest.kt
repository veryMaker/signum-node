package brs.api.http

import brs.api.http.common.JSONResponses.INCORRECT_DELTA_QUANTITY
import brs.api.http.common.JSONResponses.MISSING_DELTA_QUANTITY
import brs.api.http.common.JSONResponses.UNKNOWN_GOODS
import brs.api.http.common.Parameters.DELTA_QUANTITY_PARAMETER
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.entity.Account
import brs.entity.DependencyProvider
import brs.entity.Goods
import brs.objects.FluxValues
import brs.services.BlockchainService
import brs.services.ParameterService
import brs.transaction.appendix.Attachment
import brs.transaction.type.TransactionType
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DGSQuantityChangeTest : AbstractTransactionTest() {

    private lateinit var t: DGSQuantityChange
    private lateinit var dp: DependencyProvider
    private lateinit var mockParameterService: ParameterService
    private lateinit var mockBlockchainService: BlockchainService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        mockParameterService = mockk(relaxed = true)
        mockBlockchainService = mockk(relaxed = true)
        apiTransactionManagerMock = mockk(relaxed = true)
        every { mockBlockchainService.height } returns 0
        dp = QuickMocker.dependencyProvider(mockParameterService, mockBlockchainService, apiTransactionManagerMock)
        t = DGSQuantityChange(dp)
    }

    @Test
    fun processRequest() {
        val deltaQualityParameter = 5
        val request = QuickMocker.httpServletRequest(
                MockParam(DELTA_QUANTITY_PARAMETER, deltaQualityParameter)
        )

        val mockGoodsID = 123L
        val mockGoods = mockk<Goods>(relaxed = true)
        every { mockGoods.id } returns mockGoodsID
        every { mockGoods.isDelisted } returns false
        every { mockGoods.sellerId } returns 1L

        val mockSenderAccount = mockk<Account>(relaxed = true)
        every { mockSenderAccount.id } returns 1L

        every { mockParameterService.getSenderAccount(eq(request)) } returns mockSenderAccount
        every { mockParameterService.getGoods(eq(request)) } returns mockGoods
        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.DigitalGoodsQuantityChange
        assertNotNull(attachment)

        attachment.transactionType
        assertEquals(mockGoodsID, attachment.goodsId)
        assertEquals(deltaQualityParameter.toLong(), attachment.deltaQuantity.toLong())
    }

    @Test
    fun processRequest_unknownGoodsBecauseDelisted() {
        val request = QuickMocker.httpServletRequest()

        val mockGoods = mockk<Goods>(relaxed = true)
        every { mockGoods.isDelisted } returns true

        val mockSenderAccount = mockk<Account>(relaxed = true)

        every { mockParameterService.getSenderAccount(eq(request)) } returns mockSenderAccount
        every { mockParameterService.getGoods(eq(request)) } returns mockGoods

        assertEquals(UNKNOWN_GOODS, t.processRequest(request))
    }

    @Test
    fun processRequest_unknownGoodsBecauseWrongSellerId() {
        val request = QuickMocker.httpServletRequest()

        val mockGoods = mockk<Goods>(relaxed = true)
        every { mockGoods.isDelisted } returns false
        every { mockGoods.sellerId } returns 1L

        val mockSenderAccount = mockk<Account>(relaxed = true)
        every { mockSenderAccount.id } returns 2L

        every { mockParameterService.getSenderAccount(eq(request)) } returns mockSenderAccount
        every { mockParameterService.getGoods(eq(request)) } returns mockGoods

        assertEquals(UNKNOWN_GOODS, t.processRequest(request))
    }

    @Test
    fun processRequest_missingDeltaQuantity() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DELTA_QUANTITY_PARAMETER, null as String?)
        )

        val mockGoods = mockk<Goods>(relaxed = true)
        every { mockGoods.isDelisted } returns false
        every { mockGoods.sellerId } returns 1L

        val mockSenderAccount = mockk<Account>(relaxed = true)
        every { mockSenderAccount.id } returns 1L

        every { mockParameterService.getSenderAccount(eq(request)) } returns mockSenderAccount
        every { mockParameterService.getGoods(eq(request)) } returns mockGoods

        assertEquals(MISSING_DELTA_QUANTITY, t.processRequest(request))
    }

    @Test
    fun processRequest_deltaQuantityWrongFormat() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DELTA_QUANTITY_PARAMETER, "Bob")
        )

        val mockGoods = mockk<Goods>(relaxed = true)
        every { mockGoods.isDelisted } returns false
        every { mockGoods.sellerId } returns 1L

        val mockSenderAccount = mockk<Account>(relaxed = true)
        every { mockSenderAccount.id } returns 1L

        every { mockParameterService.getSenderAccount(eq(request)) } returns mockSenderAccount
        every { mockParameterService.getGoods(eq(request)) } returns mockGoods

        assertEquals(INCORRECT_DELTA_QUANTITY, t.processRequest(request))
    }

    @Test
    fun processRequest_deltaQuantityOverMaxIncorrectDeltaQuantity() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DELTA_QUANTITY_PARAMETER, Integer.MIN_VALUE)
        )

        val mockGoods = mockk<Goods>(relaxed = true)
        every { mockGoods.isDelisted } returns false
        every { mockGoods.sellerId } returns 1L

        val mockSenderAccount = mockk<Account>(relaxed = true)
        every { mockSenderAccount.id } returns 1L

        every { mockParameterService.getSenderAccount(eq(request)) } returns mockSenderAccount
        every { mockParameterService.getGoods(eq(request)) } returns mockGoods

        assertEquals(INCORRECT_DELTA_QUANTITY, t.processRequest(request))
    }

    @Test
    fun processRequest_deltaQuantityLowerThanNegativeMaxIncorrectDeltaQuantity() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DELTA_QUANTITY_PARAMETER, Integer.MAX_VALUE)
        )

        val mockGoods = mockk<Goods>(relaxed = true)
        every { mockGoods.isDelisted } returns false
        every { mockGoods.sellerId } returns 1L

        val mockSenderAccount = mockk<Account>(relaxed = true)
        every { mockSenderAccount.id } returns 1L

        every { mockParameterService.getSenderAccount(eq(request)) } returns mockSenderAccount
        every { mockParameterService.getGoods(eq(request)) } returns mockGoods

        assertEquals(INCORRECT_DELTA_QUANTITY, t.processRequest(request))
    }

}
