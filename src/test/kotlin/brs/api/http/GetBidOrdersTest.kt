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
import brs.util.safeGetAsString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
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
        mockParameterService = mock()
        mockAssetExchangeService = mock()

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

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).doReturn(assetId)

        val mockOrderId = 345L
        val mockBid = mock<Bid>()
        whenever(mockBid.id).doReturn(mockOrderId)

        val mockBidIterator = mockCollection(mockBid)

        whenever(mockParameterService.getAsset(request)).doReturn(mockAsset)
        whenever(mockAssetExchangeService.getSortedBidOrders(eq(assetId), eq(firstIndex), eq(lastIndex))).doReturn(mockBidIterator)

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
