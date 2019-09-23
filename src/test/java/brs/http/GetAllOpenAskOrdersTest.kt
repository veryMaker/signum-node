package brs.http

import brs.Order.Ask
import brs.assetexchange.AssetExchange
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.ResultFields.ASSET_RESPONSE
import brs.http.common.ResultFields.HEIGHT_RESPONSE
import brs.http.common.ResultFields.OPEN_ORDERS_RESPONSE
import brs.http.common.ResultFields.ORDER_RESPONSE
import brs.http.common.ResultFields.PRICE_NQT_RESPONSE
import brs.http.common.ResultFields.QUANTITY_QNT_RESPONSE
import brs.util.JSON
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

class GetAllOpenAskOrdersTest : AbstractUnitTest() {

    private var t: GetAllOpenAskOrders? = null

    private var mockAssetExchange: AssetExchange? = null

    @Before
    fun setUp() {
        mockAssetExchange = mock<AssetExchange>()

        t = GetAllOpenAskOrders(mockAssetExchange!!)
    }

    @Test
    fun processRequest() {
        val mockAskOrder = mock<Ask>()
        whenever(mockAskOrder.id).doReturn(1L)
        whenever(mockAskOrder.assetId).doReturn(2L)
        whenever(mockAskOrder.quantityQNT).doReturn(3L)
        whenever(mockAskOrder.priceNQT).doReturn(4L)
        whenever(mockAskOrder.height).doReturn(5)

        val firstIndex = 1
        val lastIndex = 2

        val mockIterator = mockCollection<Ask>(mockAskOrder)
        whenever(mockAssetExchange!!.getAllAskOrders(eq(firstIndex), eq(lastIndex)))
                .doReturn(mockIterator)

        val result = t!!.processRequest(QuickMocker.httpServletRequest(
                MockParam(FIRST_INDEX_PARAMETER, "" + firstIndex),
                MockParam(LAST_INDEX_PARAMETER, "" + lastIndex)
        )) as JsonObject

        assertNotNull(result)
        val openOrdersResult = result.get(OPEN_ORDERS_RESPONSE) as JsonArray

        assertNotNull(openOrdersResult)
        assertEquals(1, openOrdersResult.size().toLong())

        val openOrderResult = openOrdersResult.get(0) as JsonObject
        assertEquals("" + mockAskOrder.id, JSON.getAsString(openOrderResult.get(ORDER_RESPONSE)))
        assertEquals("" + mockAskOrder.assetId, JSON.getAsString(openOrderResult.get(ASSET_RESPONSE)))
        assertEquals("" + mockAskOrder.quantityQNT, JSON.getAsString(openOrderResult.get(QUANTITY_QNT_RESPONSE)))
        assertEquals("" + mockAskOrder.priceNQT, JSON.getAsString(openOrderResult.get(PRICE_NQT_RESPONSE)))
        assertEquals(mockAskOrder.height.toLong(), JSON.getAsInt(openOrderResult.get(HEIGHT_RESPONSE)).toLong())
    }
}
