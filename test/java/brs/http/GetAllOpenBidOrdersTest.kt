package brs.http

import brs.Order.Bid
import brs.assetexchange.AssetExchange
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.ResultFields.*
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAllOpenBidOrdersTest : AbstractUnitTest() {

    private var t: GetAllOpenBidOrders? = null

    private var mockAssetExchange: AssetExchange? = null

    @Before
    fun setUp() {
        mockAssetExchange = mock<AssetExchange>()

        t = GetAllOpenBidOrders(mockAssetExchange!!)
    }

    @Test
    fun processRequest() {
        val mockBidOrder = mock<Bid>()
        whenever(mockBidOrder.id).thenReturn(1L)
        whenever(mockBidOrder.assetId).thenReturn(2L)
        whenever(mockBidOrder.quantityQNT).thenReturn(3L)
        whenever(mockBidOrder.priceNQT).thenReturn(4L)
        whenever(mockBidOrder.height).thenReturn(5)

        val firstIndex = 1
        val lastIndex = 2

        val mockIterator = mockCollection<Bid>(mockBidOrder)
        whenever(mockAssetExchange!!.getAllBidOrders(eq(firstIndex), eq(lastIndex)))
                .thenReturn(mockIterator)

        val result = t!!.processRequest(QuickMocker.httpServletRequest(
                MockParam(FIRST_INDEX_PARAMETER, "" + firstIndex),
                MockParam(LAST_INDEX_PARAMETER, "" + lastIndex)
        )) as JsonObject

        assertNotNull(result)
        val openOrdersResult = result.get(OPEN_ORDERS_RESPONSE) as JsonArray

        assertNotNull(openOrdersResult)
        assertEquals(1, openOrdersResult.size().toLong())

        val openOrderResult = JSON.getAsJsonObject(openOrdersResult.get(0))
        assertEquals("" + mockBidOrder.id, JSON.getAsString(openOrderResult.get(ORDER_RESPONSE)))
        assertEquals("" + mockBidOrder.assetId, JSON.getAsString(openOrderResult.get(ASSET_RESPONSE)))
        assertEquals("" + mockBidOrder.quantityQNT, JSON.getAsString(openOrderResult.get(QUANTITY_QNT_RESPONSE)))
        assertEquals("" + mockBidOrder.priceNQT, JSON.getAsString(openOrderResult.get(PRICE_NQT_RESPONSE)))
        assertEquals(mockBidOrder.height.toLong(), JSON.getAsInt(openOrderResult.get(HEIGHT_RESPONSE)).toLong())
    }
}
