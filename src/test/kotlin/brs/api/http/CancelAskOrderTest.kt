package brs.api.http

import brs.entity.Account
import brs.services.BlockchainService
import brs.DependencyProvider
import brs.entity.Order.Ask
import brs.services.AssetExchangeService
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.objects.FluxValues
import brs.api.http.JSONResponses.UNKNOWN_ORDER
import brs.api.http.common.Parameters.ORDER_PARAMETER
import brs.services.ParameterService
import brs.transaction.appendix.Attachment
import brs.transaction.type.TransactionType
import brs.transaction.type.coloredCoins.AskOrderCancellation
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CancelAskOrderTest : AbstractTransactionTest() {

    private lateinit var t: CancelAskOrder
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
        t = CancelAskOrder(dp)
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

        whenever(assetExchangeServiceMock.getAskOrder(eq(orderId))).doReturn(order)
        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(sellerAccount)
        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) },
            apiTransactionManagerMock
        ) as Attachment.ColoredCoinsAskOrderCancellation
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

        whenever(assetExchangeServiceMock.getAskOrder(eq(orderId).toLong())).doReturn(null)

        assertEquals(UNKNOWN_ORDER, t.processRequest(request))
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

        whenever(assetExchangeServiceMock.getAskOrder(eq(orderId))).doReturn(order)
        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(sellerAccount)

        assertEquals(UNKNOWN_ORDER, t.processRequest(request))
    }
}
