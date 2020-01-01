package brs.api.http

import brs.entity.Order.Ask
import brs.services.AssetExchangeService
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.api.http.common.JSONResponses.UNKNOWN_ORDER
import brs.api.http.common.Parameters.ORDER_PARAMETER
import com.google.gson.JsonObject
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAskOrderTest {
    private lateinit var t: GetAskOrder

    private lateinit var mockAssetExchangeService: AssetExchangeService

    @Before
    fun setUp() {
        mockAssetExchangeService = mockk(relaxed = true)

        t = GetAskOrder(mockAssetExchangeService)
    }

    @Test
    fun processRequest() {
        val orderId = 123L

        val mockOrder = mockk<Ask>(relaxed = true)

        every { mockAssetExchangeService.getAskOrder(eq(orderId)) } returns mockOrder

        val request = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)
    }

    @Test
    fun processRequest_unknownOrder() {
        val orderId = 123L

        every { mockAssetExchangeService.getAskOrder(eq(orderId)) } returns null

        val request = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        assertEquals(UNKNOWN_ORDER, t.processRequest(request))
    }
}
