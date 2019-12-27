package brs.api.http

import brs.api.http.common.Parameters.SUBSCRIPTION_PARAMETER
import brs.api.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.entity.Account
import brs.entity.DependencyProvider
import brs.entity.Subscription
import brs.objects.FluxValues
import brs.services.BlockchainService
import brs.services.ParameterService
import brs.services.SubscriptionService
import brs.transaction.appendix.Attachment
import brs.transaction.type.TransactionType
import brs.util.json.safeGetAsLong
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SubscriptionCancelTest : AbstractTransactionTest() {

    private lateinit var t: SubscriptionCancel
    private lateinit var dp: DependencyProvider
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var subscriptionServiceMock: SubscriptionService
    private lateinit var blockchainServiceMock: BlockchainService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        parameterServiceMock = mockk()
        subscriptionServiceMock = mockk()
        blockchainServiceMock = mockk()
        apiTransactionManagerMock = mockk()
        dp = QuickMocker.dependencyProvider(
            parameterServiceMock,
            subscriptionServiceMock,
            blockchainServiceMock,
            apiTransactionManagerMock
        )
        t = SubscriptionCancel(dp)
    }

    @Test
    fun processRequest() {
        val subscriptionIdParameter = 123L

        val request = QuickMocker.httpServletRequest(
                MockParam(SUBSCRIPTION_PARAMETER, subscriptionIdParameter)
        )

        val mockSender = mockk<Account>()
        every { mockSender.id } returns 1L

        val mockSubscription = mockk<Subscription>()
        every { mockSubscription.id } returns subscriptionIdParameter
        every { mockSubscription.senderId } returns 1L
        every { mockSubscription.recipientId } returns 2L

        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockSender
        every { subscriptionServiceMock.getSubscription(eq(subscriptionIdParameter)) } returns mockSubscription

        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.AdvancedPaymentSubscriptionCancel
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is brs.transaction.type.advancedPayment.SubscriptionCancel)
        assertEquals(subscriptionIdParameter, attachment.subscriptionId)
    }

    @Test
    fun processRequest_missingSubscriptionParameter() {
        val request = QuickMocker.httpServletRequest()

        val response = t.processRequest(request) as JsonObject
        assertNotNull(response)

        assertEquals(3L, response.get(ERROR_CODE_RESPONSE).safeGetAsLong())
    }

    @Test
    fun processRequest_failedToParseSubscription() {
        val request = QuickMocker.httpServletRequest(
                MockParam(SUBSCRIPTION_PARAMETER, "notALong")
        )

        val response = t.processRequest(request) as JsonObject
        assertNotNull(response)

        assertEquals(4L, response.get(ERROR_CODE_RESPONSE).safeGetAsLong())
    }

    @Test
    fun processRequest_subscriptionNotFound() {
        val subscriptionId = 123L

        val request = QuickMocker.httpServletRequest(
                MockParam(SUBSCRIPTION_PARAMETER, subscriptionId)
        )

        every { subscriptionServiceMock.getSubscription(eq(subscriptionId)) } returns null

        val response = t.processRequest(request) as JsonObject
        assertNotNull(response)

        assertEquals(5L, response.get(ERROR_CODE_RESPONSE).safeGetAsLong())
    }

    @Test
    fun processRequest_userIsNotSenderOrRecipient() {
        val subscriptionId = 123L

        val request = QuickMocker.httpServletRequest(
                MockParam(SUBSCRIPTION_PARAMETER, subscriptionId)
        )

        val mockSender = mockk<Account>()
        every { mockSender.id } returns 1L

        val mockSubscription = mockk<Subscription>()
        every { mockSubscription.senderId } returns 2L
        every { mockSubscription.recipientId } returns 3L

        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockSender
        every { subscriptionServiceMock.getSubscription(eq(subscriptionId)) } returns mockSubscription

        val response = t.processRequest(request) as JsonObject
        assertNotNull(response)

        assertEquals(7L, response.get(ERROR_CODE_RESPONSE).safeGetAsLong())
    }
}
