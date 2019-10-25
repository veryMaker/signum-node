package brs.api.http

import brs.entity.Order.Bid
import brs.services.AssetExchangeService
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.ASSET_RESPONSE
import brs.api.http.common.ResultFields.HEIGHT_RESPONSE
import brs.api.http.common.ResultFields.OPEN_ORDERS_RESPONSE
import brs.api.http.common.ResultFields.ORDER_RESPONSE
import brs.api.http.common.ResultFields.PRICE_PLANCK_RESPONSE
import brs.api.http.common.ResultFields.QUANTITY_QNT_RESPONSE
import brs.util.mustGetAsJsonObject
import brs.util.safeGetAsLong
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

class GetAllOpenBidOrdersTest : AbstractUnitTest() {

    private lateinit var t: GetAllOpenBidOrders

    private lateinit var mockAssetExchangeService: AssetExchangeService

    @Before
    fun setUp() {
        mockAssetExchangeService = mock()

        t = GetAllOpenBidOrders(mockAssetExchangeService)
    }

    @Test
    fun processRequest() {
        val mockBidOrder = mock<Bid>()
        whenever(mockBidOrder.id).doReturn(1L)
        whenever(mockBidOrder.assetId).doReturn(2L)
        whenever(mockBidOrder.quantity).doReturn(3L)
        whenever(mockBidOrder.pricePlanck).doReturn(4L)
        whenever(mockBidOrder.height).doReturn(5)

        val firstIndex = 1
        val lastIndex = 2

        val mockIterator = mockCollection(mockBidOrder)
        whenever(mockAssetExchangeService.getAllBidOrders(eq(firstIndex), eq(lastIndex)))
                .doReturn(mockIterator)

        val result = t.processRequest(QuickMocker.httpServletRequest(
                MockParam(FIRST_INDEX_PARAMETER, firstIndex.toString()),
                MockParam(LAST_INDEX_PARAMETER, lastIndex.toString())
        )) as JsonObject

        assertNotNull(result)
        val openOrdersResult = result.get(OPEN_ORDERS_RESPONSE) as JsonArray

        assertNotNull(openOrdersResult)
        assertEquals(1, openOrdersResult.size().toLong())

        val openOrderResult = openOrdersResult.get(0).mustGetAsJsonObject("openOrderResult")
        assertEquals(mockBidOrder.id.toString(), openOrderResult.get(ORDER_RESPONSE).safeGetAsString())
        assertEquals(mockBidOrder.assetId.toString(), openOrderResult.get(ASSET_RESPONSE).safeGetAsString())
        assertEquals(mockBidOrder.quantity.toString(), openOrderResult.get(QUANTITY_QNT_RESPONSE).safeGetAsString())
        assertEquals(mockBidOrder.pricePlanck.toString(), openOrderResult.get(PRICE_PLANCK_RESPONSE).safeGetAsString())
        assertEquals(mockBidOrder.height.toLong(), openOrderResult.get(HEIGHT_RESPONSE).safeGetAsLong())
    }
}
