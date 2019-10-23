package brs.http

import brs.Order.Bid
import brs.assetexchange.AssetExchange
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.http.JSONResponses.UNKNOWN_ORDER
import brs.http.common.Parameters.ORDER_PARAMETER
import brs.http.common.ResultFields.ORDER_RESPONSE
import brs.util.safeGetAsString
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

class GetBidOrderTest {

    private lateinit var t: GetBidOrder

    private lateinit var mockAssetExchange: AssetExchange

    @Before
    fun setUp() {
        mockAssetExchange = mock()

        t = GetBidOrder(mockAssetExchange)
    }

    @Test
    fun processRequest() = runBlocking {
        val bidOrderId = 123L
        val mockBid = mock<Bid>()
        whenever(mockBid.id).doReturn(bidOrderId)

        whenever(mockAssetExchange.getBidOrder(eq(bidOrderId))).doReturn(mockBid)

        val request = QuickMocker.httpServletRequest(MockParam(ORDER_PARAMETER, bidOrderId))

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)
        assertEquals("" + bidOrderId, result.get(ORDER_RESPONSE).safeGetAsString())
    }

    @Test
    fun processRequest_orderNotFoundUnknownOrder() = runBlocking {
        val bidOrderId = 123L

        val request = QuickMocker.httpServletRequest(MockParam(ORDER_PARAMETER, bidOrderId))

        assertEquals(UNKNOWN_ORDER, t.processRequest(request))
    }

}
