package brs.api.http

import brs.entity.Order.Ask
import brs.services.AssetExchangeService
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.api.http.JSONResponses.UNKNOWN_ORDER
import brs.api.http.common.Parameters.ORDER_PARAMETER
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAskOrderTest {

    private lateinit var t: GetAskOrder

    private lateinit var mockAssetExchangeService: AssetExchangeService

    @Before
    fun setUp() {
        mockAssetExchangeService = mock()

        t = GetAskOrder(mockAssetExchangeService)
    }

    @Test
    fun processRequest() {
        val orderId = 123L

        val mockOrder = mock<Ask>()

        whenever(mockAssetExchangeService.getAskOrder(eq(orderId))).doReturn(mockOrder)

        val request = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)
    }

    @Test
    fun processRequest_unknownOrder() {
        val orderId = 123L

        whenever(mockAssetExchangeService.getAskOrder(eq(orderId))).doReturn(null)

        val request = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        assertEquals(UNKNOWN_ORDER, t.processRequest(request))
    }

}
