package brs.http

import brs.Account
import brs.Blockchain
import brs.DependencyProvider
import brs.Order.Ask
import brs.assetexchange.AssetExchange
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxValues
import brs.http.JSONResponses.UNKNOWN_ORDER
import brs.http.common.Parameters.ORDER_PARAMETER
import brs.services.ParameterService
import brs.transaction.TransactionType
import brs.transaction.coloredCoins.AskOrderCancellation
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import javax.servlet.http.HttpServletRequest

class CancelAskOrderTest : AbstractTransactionTest() {

    private lateinit var t: CancelAskOrder
    private lateinit var dp: DependencyProvider
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
        dp = QuickMocker.dependencyProvider(parameterServiceMock!!, blockchainMock!!, assetExchangeMock!!, apiTransactionManagerMock!!)
        t = CancelAskOrder(dp)
    }

    @Test
    fun processRequest() = runBlocking {
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
        dp.fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) },
                apiTransactionManagerMock!!) as brs.Attachment.ColoredCoinsAskOrderCancellation
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is AskOrderCancellation)
        assertEquals(orderId, attachment.orderId)
    }

    @Test
    fun processRequest_orderDataNotFound() = runBlocking {
        val orderId = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        whenever(assetExchangeMock!!.getAskOrder(eq(orderId).toLong())).doReturn(null)

        assertEquals(UNKNOWN_ORDER, t!!.processRequest(request))
    }

    @Test
    fun processRequest_orderOtherAccount() = runBlocking {
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
