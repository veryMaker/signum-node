package brs.api.http

import brs.api.http.JSONResponses.UNKNOWN_ORDER
import brs.api.http.common.Parameters.ORDER_PARAMETER
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.entity.Account
import brs.entity.DependencyProvider
import brs.entity.Order.Ask
import brs.objects.FluxValues
import brs.services.AssetExchangeService
import brs.services.BlockchainService
import brs.services.ParameterService
import brs.transaction.appendix.Attachment
import brs.transaction.type.TransactionType
import brs.transaction.type.coloredCoins.AskOrderCancellation
import io.mockk.every
import io.mockk.mockk
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
        parameterServiceMock = mockk()
        blockchainServiceMock = mockk()
        assetExchangeServiceMock = mockk()
        apiTransactionManagerMock = mockk()
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

        val sellerAccount = mockk<Account>()
        every { sellerAccount.id } returns sellerId

        val order = mockk<Ask>()
        every { order.accountId } returns sellerId

        every { assetExchangeServiceMock.getAskOrder(eq(orderId)) } returns order
        every { parameterServiceMock.getSenderAccount(eq(request)) } returns sellerAccount
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

        every { assetExchangeServiceMock.getAskOrder(eq(orderId.toLong())) } returns null

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

        val sellerAccount = mockk<Account>()
        every { sellerAccount.id } returns accountId

        val order = mockk<Ask>()
        every { order.accountId } returns otherAccountId

        every { assetExchangeServiceMock.getAskOrder(eq(orderId)) } returns order
        every { parameterServiceMock.getSenderAccount(eq(request)) } returns sellerAccount

        assertEquals(UNKNOWN_ORDER, t.processRequest(request))
    }
}
