package brs.api.http

import brs.api.http.JSONResponses.UNKNOWN_ORDER
import brs.api.http.common.Parameters.ORDER_PARAMETER
import brs.api.http.common.ResultFields.ORDER_RESPONSE
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.entity.Order.Bid
import brs.services.AssetExchangeService
import brs.util.json.safeGetAsString
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetBidOrderTest {

    private lateinit var t: GetBidOrder

    private lateinit var mockAssetExchangeService: AssetExchangeService

    @Before
    fun setUp() {
        mockAssetExchangeService = mockk()
        every { mockAssetExchangeService.getBidOrder(any()) } returns null

        t = GetBidOrder(mockAssetExchangeService)
    }

    @Test
    fun processRequest() {
        val bidOrderId = 123L
        val mockBid = mockk<Bid>(relaxed = true)
        every { mockBid.id } returns bidOrderId

        every { mockAssetExchangeService.getBidOrder(eq(bidOrderId)) } returns mockBid

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
