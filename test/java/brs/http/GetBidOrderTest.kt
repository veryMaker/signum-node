package brs.http

import brs.BurstException
import brs.Order.Bid
import brs.assetexchange.AssetExchange
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.util.JSON
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.UNKNOWN_ORDER
import brs.http.common.Parameters.ORDER_PARAMETER
import brs.http.common.ResultFields.ORDER_RESPONSE
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetBidOrderTest {

    private var t: GetBidOrder? = null

    private var mockAssetExchange: AssetExchange? = null

    @Before
    fun setUp() {
        mockAssetExchange = mock<AssetExchange>()

        t = GetBidOrder(mockAssetExchange!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val bidOrderId = 123L
        val mockBid = mock<Bid>()
        whenever(mockBid.id).doReturn(bidOrderId)

        whenever(mockAssetExchange!!.getBidOrder(eq(bidOrderId))).doReturn(mockBid)

        val req = QuickMocker.httpServletRequest(MockParam(ORDER_PARAMETER, bidOrderId))

        val result = t!!.processRequest(req) as JsonObject
        assertNotNull(result)
        assertEquals("" + bidOrderId, JSON.getAsString(result.get(ORDER_RESPONSE)))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_orderNotFoundUnknownOrder() {
        val bidOrderId = 123L

        val req = QuickMocker.httpServletRequest(MockParam(ORDER_PARAMETER, bidOrderId))

        assertEquals(UNKNOWN_ORDER, t!!.processRequest(req))
    }

}
