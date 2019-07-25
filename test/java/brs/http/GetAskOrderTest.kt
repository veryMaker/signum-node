package brs.http

import brs.BurstException
import brs.Order.Ask
import brs.assetexchange.AssetExchange
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.UNKNOWN_ORDER
import brs.http.common.Parameters.ORDER_PARAMETER
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAskOrderTest {

    private var t: GetAskOrder? = null

    private var mockAssetExchange: AssetExchange? = null

    @Before
    fun setUp() {
        mockAssetExchange = mock<AssetExchange>()

        t = GetAskOrder(mockAssetExchange!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val orderId = 123L

        val mockOrder = mock<Ask>()

        whenever(mockAssetExchange!!.getAskOrder(eq(orderId))).thenReturn(mockOrder)

        val req = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        val result = t!!.processRequest(req) as JsonObject
        assertNotNull(result)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_unknownOrder() {
        val orderId = 123L

        whenever(mockAssetExchange!!.getAskOrder(eq(orderId))).thenReturn(null)

        val req = QuickMocker.httpServletRequest(
                MockParam(ORDER_PARAMETER, orderId)
        )

        assertEquals(UNKNOWN_ORDER, t!!.processRequest(req))
    }

}
