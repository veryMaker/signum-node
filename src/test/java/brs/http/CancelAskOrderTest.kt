package brs.http

import brs.*
import brs.Order.Ask
import brs.assetexchange.AssetExchange
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxValues
import brs.services.ParameterService
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.UNKNOWN_ORDER
import brs.http.common.Parameters.ORDER_PARAMETER
import brs.transaction.coloredCoins.AskOrderCancellation
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.*

class CancelAskOrderTest : AbstractTransactionTest() {

    private lateinit var t: CancelAskOrder

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

        t = CancelAskOrder(QuickMocker.dependencyProvider(parameterServiceMock!!, blockchainMock!!, assetExchangeMock!!, apiTransactionManagerMock!!))
    }

    @Test
    fun processRequest() {
        val orderId: Long = 5
        val sellerId: Long = 6

        val request = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        val sellerAccount = mock<Account>()
        whenever(sellerAccount.id).doReturn(sellerId)

        val order = mock<Ask>()
        whenever(order.accountId).doReturn(sellerId)

        whenever(assetExchangeMock!!.getAskOrder(eq(orderId))).doReturn(order)
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(sellerAccount)
        val fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) },
                apiTransactionManagerMock!!) as brs.Attachment.ColoredCoinsAskOrderCancellation
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is AskOrderCancellation)
        assertEquals(orderId, attachment.orderId)
    }

    @Test
    fun processRequest_orderDataNotFound() {
        val orderId = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        whenever(assetExchangeMock!!.getAskOrder(eq(orderId).toLong())).doReturn(null)

        assertEquals(UNKNOWN_ORDER, t!!.processRequest(request))
    }

    @Test
    fun processRequest_orderOtherAccount() {
        val orderId: Long = 5
        val accountId: Long = 6
        val otherAccountId: Long = 7

        val request = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        val sellerAccount = mock<Account>()
        whenever(sellerAccount.id).doReturn(accountId)

        val order = mock<Ask>()
        whenever(order.accountId).doReturn(otherAccountId)

        whenever(assetExchangeMock!!.getAskOrder(eq(orderId))).doReturn(order)
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(sellerAccount)

        assertEquals(UNKNOWN_ORDER, t!!.processRequest(request))
    }
}
