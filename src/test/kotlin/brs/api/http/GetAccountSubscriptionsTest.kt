package brs.api.http

import brs.entity.Account
import brs.entity.Subscription
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.SUBSCRIPTIONS_RESPONSE
import brs.api.http.common.ResultFields.AMOUNT_PLANCK_RESPONSE
import brs.api.http.common.ResultFields.FREQUENCY_RESPONSE
import brs.api.http.common.ResultFields.ID_RESPONSE
import brs.api.http.common.ResultFields.TIME_NEXT_RESPONSE
import brs.services.ParameterService
import brs.services.SubscriptionService
import com.google.gson.JsonArray
import brs.util.json.getMemberAsLong
import brs.util.json.getMemberAsString
import com.google.gson.JsonObject
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAccountSubscriptionsTest : AbstractUnitTest() {
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var subscriptionServiceMock: SubscriptionService

    private lateinit var t: GetAccountSubscriptions

    @Before
    fun setUp() {
        parameterServiceMock = mockk(relaxed = true)
        subscriptionServiceMock = mockk(relaxed = true)

        t = GetAccountSubscriptions(parameterServiceMock, subscriptionServiceMock)
    }

    @Test
    fun processRequest() {
        val userId = 123L

        val request = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, userId)
        )

        val account = mockk<Account>(relaxed = true)
        every { account.id } returns userId
        every { parameterServiceMock.getAccount(eq(request)) } returns account

        val subscription = mockk<Subscription>(relaxed = true)
        every { subscription.id } returns 1L
        every { subscription.amountPlanck } returns 2L
        every { subscription.frequency } returns 3
        every { subscription.timeNext } returns 4

        val subscriptionIterator = mockCollection(subscription)
        every { subscriptionServiceMock.getSubscriptionsByParticipant(eq(userId)) } returns subscriptionIterator

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val resultSubscriptions = result.get(SUBSCRIPTIONS_RESPONSE) as JsonArray
        assertNotNull(resultSubscriptions)
        assertEquals(1, resultSubscriptions.size().toLong())

        val resultSubscription = resultSubscriptions.get(0) as JsonObject
        assertNotNull(resultSubscription)

        assertEquals(subscription.id.toString(), resultSubscription.getMemberAsString(ID_RESPONSE))
        assertEquals(subscription.amountPlanck.toString(), resultSubscription.getMemberAsString(AMOUNT_PLANCK_RESPONSE))
        assertEquals(subscription.frequency.toLong(), resultSubscription.getMemberAsLong(FREQUENCY_RESPONSE))
        assertEquals(subscription.timeNext.toLong(), resultSubscription.getMemberAsLong(TIME_NEXT_RESPONSE))
    }
}
