package brs.api.http

import brs.entity.Account
import brs.transaction.appendix.Attachment
import brs.services.BlockchainService
import brs.entity.DependencyProvider
import brs.entity.Order.Bid
import brs.services.AssetExchangeService
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.objects.FluxValues
import brs.api.http.common.JSONResponses.UNKNOWN_ORDER
import brs.api.http.common.Parameters.ORDER_PARAMETER
import brs.services.ParameterService
import brs.transaction.type.TransactionType
import brs.transaction.type.coloredCoins.BidOrderCancellation
import io.mockk.mockk
import io.mockk.every
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
        parameterServiceMock = mockk(relaxed = true)
        blockchainServiceMock = mockk(relaxed = true)
        assetExchangeServiceMock = mockk(relaxed = true)
        apiTransactionManagerMock = mockk(relaxed = true)
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

        val mockBidOrder = mockk<Bid>(relaxed = true)
        every { mockBidOrder.accountId } returns orderAccountId
        every { assetExchangeServiceMock.getBidOrder(eq(123L)) } returns mockBidOrder

        val mockAccount = mockk<Account>(relaxed = true)
        every { mockAccount.id } returns orderAccountId
        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockAccount
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

        every { assetExchangeServiceMock.getBidOrder(eq(123L)) } returns null

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

        val mockBidOrder = mockk<Bid>(relaxed = true)
        every { mockBidOrder.accountId } returns orderAccountId
        every { assetExchangeServiceMock.getBidOrder(eq(123L)) } returns mockBidOrder

        val mockAccount = mockk<Account>(relaxed = true)
        every { mockAccount.id } returns senderAccountId

        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockAccount

        assertEquals(UNKNOWN_ORDER, t.processRequest(request))
    }
}
