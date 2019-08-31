package brs.http

import brs.*
import brs.Order.Ask
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

import brs.TransactionType.ColoredCoins.ASK_ORDER_CANCELLATION
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

class CancelAskOrderTest : AbstractTransactionTest() {

    private var t: CancelAskOrder? = null

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

        t = CancelAskOrder(parameterServiceMock!!, blockchainMock!!, assetExchangeMock!!, apiTransactionManagerMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val orderId: Long = 5
        val sellerId: Long = 6

        val req = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        val sellerAccount = mock<Account>()
        whenever(sellerAccount.id).doReturn(sellerId)

        val order = mock<Ask>()
        whenever(order.accountId).doReturn(sellerId)

        whenever(assetExchangeMock!!.getAskOrder(eq(orderId))).doReturn(order)
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(req))).doReturn(sellerAccount)

        mockkStatic(Burst::class)
        val fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        every { Burst.fluxCapacitor } returns fluxCapacitor

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(req) },
                apiTransactionManagerMock!!) as brs.Attachment.ColoredCoinsAskOrderCancellation
        assertNotNull(attachment)

        assertEquals(ASK_ORDER_CANCELLATION, attachment.transactionType)
        assertEquals(orderId, attachment.orderId)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_orderDataNotFound() {
        val orderId = 5

        val req = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        whenever(assetExchangeMock!!.getAskOrder(eq(orderId).toLong())).doReturn(null)

        assertEquals(UNKNOWN_ORDER, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_orderOtherAccount() {
        val orderId: Long = 5
        val accountId: Long = 6
        val otherAccountId: Long = 7

        val req = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        val sellerAccount = mock<Account>()
        whenever(sellerAccount.id).doReturn(accountId)

        val order = mock<Ask>()
        whenever(order.accountId).doReturn(otherAccountId)

        whenever(assetExchangeMock!!.getAskOrder(eq(orderId))).doReturn(order)
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(req))).doReturn(sellerAccount)

        assertEquals(UNKNOWN_ORDER, t!!.processRequest(req))
    }
}
