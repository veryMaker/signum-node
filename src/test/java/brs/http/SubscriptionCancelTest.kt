package brs.http

import brs.*
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxValues
import brs.http.common.Parameters.SUBSCRIPTION_PARAMETER
import brs.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.services.ParameterService
import brs.services.SubscriptionService
import brs.transaction.TransactionType
import brs.util.JSON
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import javax.servlet.http.HttpServletRequest

@RunWith(JUnit4::class)
class SubscriptionCancelTest : AbstractTransactionTest() {

    private lateinit var t: SubscriptionCancel
    private lateinit var dp: DependencyProvider
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var subscriptionServiceMock: SubscriptionService
    private lateinit var blockchainMock: Blockchain
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        subscriptionServiceMock = mock<SubscriptionService>()
        blockchainMock = mock<Blockchain>()
        apiTransactionManagerMock = mock<APITransactionManager>()
        dp = QuickMocker.dependencyProvider(parameterServiceMock!!, subscriptionServiceMock!!, blockchainMock!!, apiTransactionManagerMock!!)
        t = SubscriptionCancel(dp)
    }

    @Test
    fun processRequest() = runBlocking {
        val subscriptionIdParameter = 123L

        val request = QuickMocker.httpServletRequest(
                MockParam(SUBSCRIPTION_PARAMETER, subscriptionIdParameter)
        )

        val mockSender = mock<Account>()
        whenever(mockSender.id).doReturn(1L)

        val mockSubscription = mock<Subscription>()
        whenever(mockSubscription.id).doReturn(subscriptionIdParameter)
        whenever(mockSubscription.senderId).doReturn(1L)
        whenever(mockSubscription.recipientId).doReturn(2L)

        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSender)
        whenever(subscriptionServiceMock!!.getSubscription(eq<Long>(subscriptionIdParameter))).doReturn(mockSubscription)

        dp.fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) }, apiTransactionManagerMock!!) as Attachment.AdvancedPaymentSubscriptionCancel
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is brs.transaction.advancedPayment.SubscriptionCancel)
        assertEquals(subscriptionIdParameter, attachment.subscriptionId)
    }

    @Test
    fun processRequest_missingSubscriptionParameter() = runBlocking {
        val request = QuickMocker.httpServletRequest()

        val response = t!!.processRequest(request) as JsonObject
        assertNotNull(response)

        assertEquals(3, JSON.getAsInt(response.get(ERROR_CODE_RESPONSE)).toLong())
    }

    @Test
    fun processRequest_failedToParseSubscription() = runBlocking {
        val request = QuickMocker.httpServletRequest(
                MockParam(SUBSCRIPTION_PARAMETER, "notALong")
        )

        val response = t!!.processRequest(request) as JsonObject
        assertNotNull(response)

        assertEquals(4, JSON.getAsInt(response.get(ERROR_CODE_RESPONSE)).toLong())
    }

    @Test
    fun processRequest_subscriptionNotFound() = runBlocking {
        val subscriptionId = 123L

        val request = QuickMocker.httpServletRequest(
                MockParam(SUBSCRIPTION_PARAMETER, subscriptionId)
        )

        whenever(subscriptionServiceMock!!.getSubscription(eq(subscriptionId))).doReturn(null)

        val response = t!!.processRequest(request) as JsonObject
        assertNotNull(response)

        assertEquals(5, JSON.getAsInt(response.get(ERROR_CODE_RESPONSE)).toLong())
    }

    @Test
    fun processRequest_userIsNotSenderOrRecipient() = runBlocking {
        val subscriptionId = 123L

        val request = QuickMocker.httpServletRequest(
                MockParam(SUBSCRIPTION_PARAMETER, subscriptionId)
        )

        val mockSender = mock<Account>()
        whenever(mockSender.id).doReturn(1L)

        val mockSubscription = mock<Subscription>()
        whenever(mockSubscription.senderId).doReturn(2L)
        whenever(mockSubscription.recipientId).doReturn(3L)

        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSender)
        whenever(subscriptionServiceMock!!.getSubscription(eq(subscriptionId))).doReturn(mockSubscription)

        val response = t!!.processRequest(request) as JsonObject
        assertNotNull(response)

        assertEquals(7, JSON.getAsInt(response.get(ERROR_CODE_RESPONSE)).toLong())
    }
}
