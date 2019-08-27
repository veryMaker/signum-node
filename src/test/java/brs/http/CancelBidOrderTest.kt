package brs.http

import brs.*
import brs.Order.Bid
import brs.assetexchange.AssetExchange
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxCapacitor
import brs.fluxcapacitor.FluxValues
import brs.services.ParameterService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import javax.servlet.http.HttpServletRequest

import brs.TransactionType.ColoredCoins.BID_ORDER_CANCELLATION
import brs.http.JSONResponses.UNKNOWN_ORDER
import brs.http.common.Parameters.ORDER_PARAMETER
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CancelBidOrderTest : AbstractTransactionTest() {

    private var t: CancelBidOrder? = null

    private var parameterServiceMock: ParameterService? = null
    private var blockchainMock: Blockchain? = null
    private var assetExchangeMock: AssetExchange? = null
    private var apiTransactionManagerMock: APITransactionManager? = null

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        blockchainMock = mock<Blockchain>()
        assetExchangeMock = mock<AssetExchange>()
        apiTransactionManagerMock = mock<APITransactionManager>()

        t = CancelBidOrder(parameterServiceMock!!, blockchainMock!!, assetExchangeMock!!, apiTransactionManagerMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val orderId = 123
        val orderAccountId: Long = 1

        val req = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        val mockBidOrder = mock<Bid>()
        whenever(mockBidOrder.accountId).doReturn(orderAccountId)
        whenever(assetExchangeMock!!.getBidOrder(eq(123L))).doReturn(mockBidOrder)

        val mockAccount = mock<Account>()
        whenever(mockAccount.getId()).doReturn(orderAccountId)
        whenever(parameterServiceMock!!.getSenderAccount(eq(req))).doReturn(mockAccount)

        mockkStatic(Burst::class)
        val fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        every { Burst.fluxCapacitor } returns fluxCapacitor

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(req) }, apiTransactionManagerMock!!) as Attachment.ColoredCoinsBidOrderCancellation
        assertNotNull(attachment)

        assertEquals(BID_ORDER_CANCELLATION, attachment.transactionType)
        assertEquals(orderId.toLong(), attachment.orderId)
    }

    @Test(expected = ParameterException::class)
    @Throws(BurstException::class)
    fun processRequest_orderParameterMissing() {
        t!!.processRequest(QuickMocker.httpServletRequest())
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_orderDataMissingUnkownOrder() {
        val orderId = 123
        val req = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        whenever(assetExchangeMock!!.getBidOrder(eq(123L))).doReturn(null)

        assertEquals(UNKNOWN_ORDER, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_accountIdNotSameAsOrder() {
        val orderId = 123
        val orderAccountId: Long = 1
        val senderAccountId: Long = 2

        val req = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        val mockBidOrder = mock<Bid>()
        whenever(mockBidOrder.accountId).doReturn(orderAccountId)
        whenever(assetExchangeMock!!.getBidOrder(eq(123L))).doReturn(mockBidOrder)

        val mockAccount = mock<Account>()
        whenever(mockAccount.getId()).doReturn(senderAccountId)

        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(req))).doReturn(mockAccount)

        assertEquals(UNKNOWN_ORDER, t!!.processRequest(req))
    }

}
