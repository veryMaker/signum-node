package brs.http

import brs.Order.Ask
import brs.assetexchange.AssetExchange
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.http.JSONResponses.UNKNOWN_ORDER
import brs.http.common.Parameters.ORDER_PARAMETER
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

class GetAskOrderTest {

    private lateinit var t: GetAskOrder

    private lateinit var mockAssetExchange: AssetExchange

    @Before
    fun setUp() {
        mockAssetExchange = mock()

        t = GetAskOrder(mockAssetExchange)
    }

    @Test
    fun processRequest() = runBlocking {
        val orderId = 123L

        val mockOrder = mock<Ask>()

        whenever(mockAssetExchange.getAskOrder(eq(orderId))).doReturn(mockOrder)

        val request = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)
    }

    @Test
    fun processRequest_unknownOrder() = runBlocking {
        val orderId = 123L

        whenever(mockAssetExchange.getAskOrder(eq(orderId))).doReturn(null)

        val request = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        assertEquals(UNKNOWN_ORDER, t.processRequest(request))
    }

}
