package brs.http

import brs.Account
import brs.BurstException
import brs.Subscription
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.services.ParameterService
import brs.services.SubscriptionService
import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.SUBSCRIPTIONS_RESPONSE
import brs.http.common.ResultFields.*
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAccountSubscriptionsTest : AbstractUnitTest() {

    private var parameterServiceMock: ParameterService? = null
    private var subscriptionServiceMock: SubscriptionService? = null

    private var t: GetAccountSubscriptions? = null

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        subscriptionServiceMock = mock<SubscriptionService>()

        t = GetAccountSubscriptions(parameterServiceMock!!, subscriptionServiceMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val userId = 123L

        val req = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, userId)
        )

        val account = mock<Account>()
        whenever(account.getId()).thenReturn(userId)
        whenever(parameterServiceMock!!.getAccount(eq<HttpServletRequest>(req))).thenReturn(account)

        val subscription = mock<Subscription>()
        whenever(subscription.getId()).thenReturn(1L)
        whenever(subscription.getAmountNQT()).thenReturn(2L)
        whenever(subscription.getFrequency()).thenReturn(3)
        whenever(subscription.timeNext).thenReturn(4)

        val subscriptionIterator = this.mockCollection<Subscription>(subscription)
        whenever(subscriptionServiceMock!!.getSubscriptionsByParticipant(eq(userId))).thenReturn(subscriptionIterator)

        val result = t!!.processRequest(req) as JsonObject
        assertNotNull(result)

        val resultSubscriptions = result.get(SUBSCRIPTIONS_RESPONSE) as JsonArray
        assertNotNull(resultSubscriptions)
        assertEquals(1, resultSubscriptions.size().toLong())

        val resultSubscription = resultSubscriptions.get(0) as JsonObject
        assertNotNull(resultSubscription)

        assertEquals("" + subscription.getId()!!, JSON.getAsString(resultSubscription.get(ID_RESPONSE)))
        assertEquals("" + subscription.getAmountNQT()!!, JSON.getAsString(resultSubscription.get(AMOUNT_NQT_RESPONSE)))
        assertEquals(subscription.getFrequency().toLong(), JSON.getAsInt(resultSubscription.get(FREQUENCY_RESPONSE)).toLong())
        assertEquals(subscription.timeNext.toLong(), JSON.getAsInt(resultSubscription.get(TIME_NEXT_RESPONSE)).toLong())
    }

}
