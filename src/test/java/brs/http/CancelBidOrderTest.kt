package brs.http

import brs.*
import brs.Order.Bid
import brs.assetexchange.AssetExchange
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxValues
import brs.services.ParameterService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.UNKNOWN_ORDER
import brs.http.common.Parameters.ORDER_PARAMETER
import brs.transaction.coloredCoins.BidOrderCancellation
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.*
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CancelBidOrderTest : AbstractTransactionTest() {

    private lateinit var t: CancelBidOrder

    private lateinit var parameterServiceMock: ParameterService
    private lateinit var blockchainMock: Blockchain
    private lateinit var assetExchangeMock: AssetExchange
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        blockchainMock = mock<Blockchain>()
        assetExchangeMock = mock<AssetExchange>()
        apiTransactionManagerMock = mock<APITransactionManager>()

        t = CancelBidOrder(QuickMocker.dependencyProvider(parameterServiceMock!!, blockchainMock!!, assetExchangeMock!!, apiTransactionManagerMock!!))
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
        whenever(assetExchangeMock!!.getBidOrder(eq(123L))).doReturn(mockBidOrder)

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(orderAccountId)
        whenever(parameterServiceMock!!.getSenderAccount(eq(request))).doReturn(mockAccount)
        val fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) }, apiTransactionManagerMock!!) as Attachment.ColoredCoinsBidOrderCancellation
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is BidOrderCancellation)
        assertEquals(orderId.toLong(), attachment.orderId)
    }

    @Test(expected = ParameterException::class)
    fun processRequest_orderParameterMissing() {
        t!!.processRequest(QuickMocker.httpServletRequest())
    }

    @Test
    fun processRequest_orderDataMissingUnkownOrder() {
        val orderId = 123
        val request = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        whenever(assetExchangeMock!!.getBidOrder(eq(123L))).doReturn(null)

        assertEquals(UNKNOWN_ORDER, t!!.processRequest(request))
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
        whenever(assetExchangeMock!!.getBidOrder(eq(123L))).doReturn(mockBidOrder)

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(senderAccountId)

        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)

        assertEquals(UNKNOWN_ORDER, t!!.processRequest(request))
    }
}
