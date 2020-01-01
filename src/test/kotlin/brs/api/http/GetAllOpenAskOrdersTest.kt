package brs.api.http

import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.ASSET_RESPONSE
import brs.api.http.common.ResultFields.HEIGHT_RESPONSE
import brs.api.http.common.ResultFields.OPEN_ORDERS_RESPONSE
import brs.api.http.common.ResultFields.ORDER_RESPONSE
import brs.api.http.common.ResultFields.PRICE_PLANCK_RESPONSE
import brs.api.http.common.ResultFields.QUANTITY_QNT_RESPONSE
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.entity.Order.Ask
import brs.services.AssetExchangeService
import brs.util.json.getMemberAsLong
import brs.util.json.getMemberAsString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAllOpenAskOrdersTest : AbstractUnitTest() {
    private lateinit var t: GetAllOpenAskOrders

    private lateinit var mockAssetExchangeService: AssetExchangeService

    @Before
    fun setUp() {
        mockAssetExchangeService = mockk(relaxed = true)

        t = GetAllOpenAskOrders(mockAssetExchangeService)
    }

    @Test
    fun processRequest() {
        val mockAskOrder = mockk<Ask>(relaxed = true)
        every { mockAskOrder.id } returns 1L
        every { mockAskOrder.assetId } returns 2L
        every { mockAskOrder.quantity } returns 3L
        every { mockAskOrder.pricePlanck } returns 4L
        every { mockAskOrder.height } returns 5

        val firstIndex = 1
        val lastIndex = 2

        val mockIterator = mockCollection(mockAskOrder)
        every { mockAssetExchangeService.getAllAskOrders(eq(firstIndex), eq(lastIndex)) } returns mockIterator

        val result = t.processRequest(QuickMocker.httpServletRequest(
                MockParam(FIRST_INDEX_PARAMETER, firstIndex.toString()),
                MockParam(LAST_INDEX_PARAMETER, lastIndex.toString())
        )) as JsonObject

        assertNotNull(result)
        val openOrdersResult = result.get(OPEN_ORDERS_RESPONSE) as JsonArray

        assertNotNull(openOrdersResult)
        assertEquals(1, openOrdersResult.size().toLong())

        val openOrderResult = openOrdersResult.get(0) as JsonObject
        assertEquals(mockAskOrder.id.toString(), openOrderResult.getMemberAsString(ORDER_RESPONSE))
        assertEquals(mockAskOrder.assetId.toString(), openOrderResult.getMemberAsString(ASSET_RESPONSE))
        assertEquals(mockAskOrder.quantity.toString(), openOrderResult.getMemberAsString(QUANTITY_QNT_RESPONSE))
        assertEquals(mockAskOrder.pricePlanck.toString(), openOrderResult.getMemberAsString(PRICE_PLANCK_RESPONSE))
        assertEquals(mockAskOrder.height.toLong(), openOrderResult.getMemberAsLong(HEIGHT_RESPONSE))
    }
}
