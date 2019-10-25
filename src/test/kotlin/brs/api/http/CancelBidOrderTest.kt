package brs.api.http

import brs.entity.Account
import brs.transaction.appendix.Attachment
import brs.services.BlockchainService
import brs.DependencyProvider
import brs.entity.Order.Bid
import brs.services.AssetExchangeService
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.objects.FluxValues
import brs.api.http.JSONResponses.UNKNOWN_ORDER
import brs.api.http.common.Parameters.ORDER_PARAMETER
import brs.services.ParameterService
import brs.transaction.type.TransactionType
import brs.transaction.type.coloredCoins.BidOrderCancellation
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
class CancelBidOrderTest : AbstractTransactionTest() {

    private lateinit var t: CancelBidOrder
    private lateinit var dp: DependencyProvider
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var blockchainServiceMock: BlockchainService
    private lateinit var assetExchangeServiceMock: AssetExchangeService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        parameterServiceMock = mock()
        blockchainServiceMock = mock()
        assetExchangeServiceMock = mock()
        apiTransactionManagerMock = mock()
        dp = QuickMocker.dependencyProvider(
            parameterServiceMock,
            blockchainServiceMock,
            assetExchangeServiceMock,
            apiTransactionManagerMock
        )
        t = CancelBidOrder(dp)
    }

    @Test
    fun processRequest() {
        val orderId = 123
        val orderAccountId: Long = 1

        val request = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        val mockBidOrder = mock<Bid>()
        whenever(mockBidOrder.accountId).doReturn(orderAccountId)
        whenever(assetExchangeServiceMock.getBidOrder(eq(123L))).doReturn(mockBidOrder)

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(orderAccountId)
        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(mockAccount)
        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.ColoredCoinsBidOrderCancellation
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is BidOrderCancellation)
        assertEquals(orderId.toLong(), attachment.orderId)
    }

    @Test(expected = ParameterException::class)
    fun processRequest_orderParameterMissing() {
        t.processRequest(QuickMocker.httpServletRequest())
    }

    @Test
    fun processRequest_orderDataMissingUnkownOrder() {
        val orderId = 123
        val request = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        whenever(assetExchangeServiceMock.getBidOrder(eq(123L))).doReturn(null)

        assertEquals(UNKNOWN_ORDER, t.processRequest(request))
    }

    @Test
    fun processRequest_accountIdNotSameAsOrder() {
        val orderId = 123
        val orderAccountId: Long = 1
        val senderAccountId: Long = 2

        val request = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        val mockBidOrder = mock<Bid>()
        whenever(mockBidOrder.accountId).doReturn(orderAccountId)
        whenever(assetExchangeServiceMock.getBidOrder(eq(123L))).doReturn(mockBidOrder)

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(senderAccountId)

        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(mockAccount)

        assertEquals(UNKNOWN_ORDER, t.processRequest(request))
    }
}
