package brs.api.http

import brs.entity.Asset
import brs.entity.Order.Bid
import brs.services.AssetExchangeService
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.BID_ORDERS_RESPONSE
import brs.api.http.common.ResultFields.ORDER_RESPONSE
import brs.services.ParameterService
import brs.util.json.safeGetAsString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetBidOrdersTest : AbstractUnitTest() {

    private lateinit var t: GetBidOrders

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockAssetExchangeService: AssetExchangeService

    @Before
    fun setUp() {
        mockParameterService = mockk()
        mockAssetExchangeService = mockk()

        t = GetBidOrders(mockParameterService, mockAssetExchangeService)
    }

    @Test
    fun processRequest() {
        val assetId = 123L
        val firstIndex = 0
        val lastIndex = 1

        val request = QuickMocker.httpServletRequest(
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex)
        )

        val mockAsset = mockk<Asset>()
        every { mockAsset.id } returns assetId

        val mockOrderId = 345L
        val mockBid = mockk<Bid>()
        every { mockBid.id } returns mockOrderId

        val mockBidIterator = mockCollection(mockBid)

        every { mockParameterService.getAsset(request) } returns mockAsset
        every { mockAssetExchangeService.getSortedBidOrders(eq(assetId), eq(firstIndex), eq(lastIndex)) } returns mockBidIterator

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val resultBidOrdersList = result.get(BID_ORDERS_RESPONSE) as JsonArray
        assertNotNull(resultBidOrdersList)
        assertEquals(1, resultBidOrdersList.size().toLong())

        val resultBidOrder = resultBidOrdersList.get(0) as JsonObject
        assertNotNull(resultBidOrder)

        assertEquals(mockOrderId.toString(), resultBidOrder.get(ORDER_RESPONSE).safeGetAsString())
    }

}
