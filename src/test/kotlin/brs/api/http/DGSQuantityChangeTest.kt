package brs.api.http

import brs.entity.Account
import brs.transaction.appendix.Attachment
import brs.services.BlockchainService
import brs.entity.DependencyProvider
import brs.entity.Goods
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.objects.FluxValues
import brs.api.http.JSONResponses.INCORRECT_DELTA_QUANTITY
import brs.api.http.JSONResponses.MISSING_DELTA_QUANTITY
import brs.api.http.JSONResponses.UNKNOWN_GOODS
import brs.api.http.common.Parameters.DELTA_QUANTITY_PARAMETER
import brs.services.ParameterService
import brs.transaction.type.TransactionType
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
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
        mockParameterService = mock()
        mockBlockchainService = mock()
        apiTransactionManagerMock = mock()
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
        val mockGoods = mock<Goods>()
        whenever(mockGoods.id).doReturn(mockGoodsID)
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.sellerId).doReturn(1L)

        val mockSenderAccount = mock<Account>()
        whenever(mockSenderAccount.id).doReturn(1L)

        whenever(mockParameterService.getSenderAccount(eq(request))).doReturn(mockSenderAccount)
        whenever(mockParameterService.getGoods(eq(request))).doReturn(mockGoods)
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

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(true)

        val mockSenderAccount = mock<Account>()

        whenever(mockParameterService.getSenderAccount(eq(request))).doReturn(mockSenderAccount)
        whenever(mockParameterService.getGoods(eq(request))).doReturn(mockGoods)

        assertEquals(UNKNOWN_GOODS, t.processRequest(request))
    }

    @Test
    fun processRequest_unknownGoodsBecauseWrongSellerId() {
        val request = QuickMocker.httpServletRequest()

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.sellerId).doReturn(1L)

        val mockSenderAccount = mock<Account>()
        whenever(mockSenderAccount.id).doReturn(2L)

        whenever(mockParameterService.getSenderAccount(eq(request))).doReturn(mockSenderAccount)
        whenever(mockParameterService.getGoods(eq(request))).doReturn(mockGoods)

        assertEquals(UNKNOWN_GOODS, t.processRequest(request))
    }

    @Test
    fun processRequest_missingDeltaQuantity() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DELTA_QUANTITY_PARAMETER, null as String?)
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.sellerId).doReturn(1L)

        val mockSenderAccount = mock<Account>()
        whenever(mockSenderAccount.id).doReturn(1L)

        whenever(mockParameterService.getSenderAccount(eq(request))).doReturn(mockSenderAccount)
        whenever(mockParameterService.getGoods(eq(request))).doReturn(mockGoods)

        assertEquals(MISSING_DELTA_QUANTITY, t.processRequest(request))
    }

    @Test
    fun processRequest_deltaQuantityWrongFormat() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DELTA_QUANTITY_PARAMETER, "Bob")
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.sellerId).doReturn(1L)

        val mockSenderAccount = mock<Account>()
        whenever(mockSenderAccount.id).doReturn(1L)

        whenever(mockParameterService.getSenderAccount(eq(request))).doReturn(mockSenderAccount)
        whenever(mockParameterService.getGoods(eq(request))).doReturn(mockGoods)

        assertEquals(INCORRECT_DELTA_QUANTITY, t.processRequest(request))
    }

    @Test
    fun processRequest_deltaQuantityOverMaxIncorrectDeltaQuantity() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DELTA_QUANTITY_PARAMETER, Integer.MIN_VALUE)
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.sellerId).doReturn(1L)

        val mockSenderAccount = mock<Account>()
        whenever(mockSenderAccount.id).doReturn(1L)

        whenever(mockParameterService.getSenderAccount(eq(request))).doReturn(mockSenderAccount)
        whenever(mockParameterService.getGoods(eq(request))).doReturn(mockGoods)

        assertEquals(INCORRECT_DELTA_QUANTITY, t.processRequest(request))
    }

    @Test
    fun processRequest_deltaQuantityLowerThanNegativeMaxIncorrectDeltaQuantity() {
        val request = QuickMocker.httpServletRequest(
                MockParam(DELTA_QUANTITY_PARAMETER, Integer.MAX_VALUE)
        )

        val mockGoods = mock<Goods>()
        whenever(mockGoods.isDelisted).doReturn(false)
        whenever(mockGoods.sellerId).doReturn(1L)

        val mockSenderAccount = mock<Account>()
        whenever(mockSenderAccount.id).doReturn(1L)

        whenever(mockParameterService.getSenderAccount(eq(request))).doReturn(mockSenderAccount)
        whenever(mockParameterService.getGoods(eq(request))).doReturn(mockGoods)

        assertEquals(INCORRECT_DELTA_QUANTITY, t.processRequest(request))
    }

}
