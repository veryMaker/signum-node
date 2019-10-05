package brs.http

import brs.Account
import brs.Subscription
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.SUBSCRIPTIONS_RESPONSE
import brs.http.common.ResultFields.AMOUNT_NQT_RESPONSE
import brs.http.common.ResultFields.FREQUENCY_RESPONSE
import brs.http.common.ResultFields.ID_RESPONSE
import brs.http.common.ResultFields.TIME_NEXT_RESPONSE
import brs.services.ParameterService
import brs.services.SubscriptionService
import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import javax.servlet.http.HttpServletRequest

class GetAccountSubscriptionsTest : AbstractUnitTest() {

    private lateinit var parameterServiceMock: ParameterService
    private lateinit var subscriptionServiceMock: SubscriptionService

    private lateinit var t: GetAccountSubscriptions

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        subscriptionServiceMock = mock<SubscriptionService>()

        t = GetAccountSubscriptions(parameterServiceMock!!, subscriptionServiceMock!!)
    }

    @Test
    fun processRequest() = runBlocking {
        val userId = 123L

        val request = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, userId)
        )

        val account = mock<Account>()
        whenever(account.id).doReturn(userId)
        whenever(parameterServiceMock!!.getAccount(eq<HttpServletRequest>(request))).doReturn(account)

        val subscription = mock<Subscription>()
        whenever(subscription.id).doReturn(1L)
        whenever(subscription.amountNQT).doReturn(2L)
        whenever(subscription.frequency).doReturn(3)
        whenever(subscription.timeNext).doReturn(4)

        val subscriptionIterator = mockCollection(subscription)
        whenever(subscriptionServiceMock!!.getSubscriptionsByParticipant(eq(userId))).doReturn(subscriptionIterator)

        val result = t!!.processRequest(request) as JsonObject
        assertNotNull(result)

        val resultSubscriptions = result.get(SUBSCRIPTIONS_RESPONSE) as JsonArray
        assertNotNull(resultSubscriptions)
        assertEquals(1, resultSubscriptions.size().toLong())

        val resultSubscription = resultSubscriptions.get(0) as JsonObject
        assertNotNull(resultSubscription)

        assertEquals("" + subscription.id!!, JSON.getAsString(resultSubscription.get(ID_RESPONSE)))
        assertEquals("" + subscription.amountNQT!!, JSON.getAsString(resultSubscription.get(AMOUNT_NQT_RESPONSE)))
        assertEquals(subscription.frequency.toLong(), JSON.getAsInt(resultSubscription.get(FREQUENCY_RESPONSE)).toLong())
        assertEquals(subscription.timeNext.toLong(), JSON.getAsInt(resultSubscription.get(TIME_NEXT_RESPONSE)).toLong())
    }

}
