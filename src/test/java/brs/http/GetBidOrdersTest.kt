package brs.http

import brs.Asset
import brs.BurstException
import brs.Order.Bid
import brs.assetexchange.AssetExchange
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.services.ParameterService
import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.ResultFields.BID_ORDERS_RESPONSE
import brs.http.common.ResultFields.ORDER_RESPONSE
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetBidOrdersTest : AbstractUnitTest() {

    private var t: GetBidOrders? = null

    private var mockParameterService: ParameterService? = null
    private var mockAssetExchange: AssetExchange? = null

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        mockAssetExchange = mock<AssetExchange>()

        t = GetBidOrders(mockParameterService!!, mockAssetExchange!!)
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

        val mockBidIterator = mockCollection<Bid>(mockBid)

        whenever(mockParameterService!!.getAsset(request)).doReturn(mockAsset)
        whenever(mockAssetExchange!!.getSortedBidOrders(eq(assetId), eq(firstIndex), eq(lastIndex))).doReturn(mockBidIterator)

        val result = t!!.processRequest(request) as JsonObject
        assertNotNull(result)

        val resultBidOrdersList = result.get(BID_ORDERS_RESPONSE) as JsonArray
        assertNotNull(resultBidOrdersList)
        assertEquals(1, resultBidOrdersList.size().toLong())

        val resultBidOrder = resultBidOrdersList.get(0) as JsonObject
        assertNotNull(resultBidOrder)

        assertEquals("" + mockOrderId, JSON.getAsString(resultBidOrder.get(ORDER_RESPONSE)))
    }

}
