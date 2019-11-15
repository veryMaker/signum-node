package brs.api.http

import brs.entity.Order.Bid
import brs.services.AssetExchangeService
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.api.http.JSONResponses.UNKNOWN_ORDER
import brs.api.http.common.Parameters.ORDER_PARAMETER
import brs.api.http.common.ResultFields.ORDER_RESPONSE
import brs.util.json.safeGetAsString
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetBidOrderTest {

    private lateinit var t: GetBidOrder

    private lateinit var mockAssetExchangeService: AssetExchangeService

    @Before
    fun setUp() {
        mockAssetExchangeService = mock()

        t = GetBidOrder(mockAssetExchangeService)
    }

    @Test
    fun processRequest() {
        val bidOrderId = 123L
        val mockBid = mock<Bid>()
        whenever(mockBid.id).doReturn(bidOrderId)

        whenever(mockAssetExchangeService.getBidOrder(eq(bidOrderId))).doReturn(mockBid)

        val request = QuickMocker.httpServletRequest(MockParam(ORDER_PARAMETER, bidOrderId))

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)
        assertEquals(bidOrderId.toString(), result.get(ORDER_RESPONSE).safeGetAsString())
    }

    @Test
    fun processRequest_orderNotFoundUnknownOrder() {
        val bidOrderId = 123L

        val request = QuickMocker.httpServletRequest(MockParam(ORDER_PARAMETER, bidOrderId))

        assertEquals(UNKNOWN_ORDER, t.processRequest(request))
    }

}
