package brs.http

import brs.Asset
import brs.Order.Ask
import brs.assetexchange.AssetExchange
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.http.common.Parameters.ASSET_PARAMETER
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.ResultFields.ASK_ORDERS_RESPONSE
import brs.http.common.ResultFields.ASSET_RESPONSE
import brs.http.common.ResultFields.HEIGHT_RESPONSE
import brs.http.common.ResultFields.ORDER_RESPONSE
import brs.http.common.ResultFields.PRICE_NQT_RESPONSE
import brs.http.common.ResultFields.QUANTITY_QNT_RESPONSE
import brs.services.ParameterService
import brs.util.JSON
import brs.util.safeGetAsLong
import brs.util.safeGetAsString
import com.google.gson.JsonArray
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
import javax.servlet.http.HttpServletRequest

class GetAskOrdersTest : AbstractUnitTest() {

    private lateinit var parameterServiceMock: ParameterService
    private lateinit var assetExchangeMock: AssetExchange

    private lateinit var t: GetAskOrders

    @Before
    fun setUp() {
        parameterServiceMock = mock()
        assetExchangeMock = mock()

        t = GetAskOrders(parameterServiceMock, assetExchangeMock)
    }

    @Test
    fun processRequest() = runBlocking {
        val assetIndex: Long = 5
        val firstIndex = 1
        val lastIndex = 3

        val request = QuickMocker.httpServletRequest(
                MockParam(ASSET_PARAMETER, assetIndex),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex)
        )

        val asset = mock<Asset>()
        whenever(asset.id).doReturn(assetIndex)

        whenever(parameterServiceMock.getAsset(eq(request))).doReturn(asset)

        val askOrder1 = mock<Ask>()
        whenever(askOrder1.id).doReturn(3L)
        whenever(askOrder1.assetId).doReturn(assetIndex)
        whenever(askOrder1.quantityQNT).doReturn(56L)
        whenever(askOrder1.priceNQT).doReturn(45L)
        whenever(askOrder1.height).doReturn(32)

        val askOrder2 = mock<Ask>()
        whenever(askOrder1.id).doReturn(4L)

        val askIterator = mockCollection(askOrder1, askOrder2)

        whenever(assetExchangeMock.getSortedAskOrders(eq(assetIndex), eq(firstIndex), eq(lastIndex))).doReturn(askIterator)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val orders = result.get(ASK_ORDERS_RESPONSE) as JsonArray
        assertNotNull(orders)

        assertEquals(2, orders.size().toLong())

        val askOrder1Result = orders.get(0) as JsonObject

        assertEquals("" + askOrder1.id, askOrder1Result.get(ORDER_RESPONSE).safeGetAsString())
        assertEquals("" + askOrder1.assetId, askOrder1Result.get(ASSET_RESPONSE).safeGetAsString())
        assertEquals("" + askOrder1.quantityQNT, askOrder1Result.get(QUANTITY_QNT_RESPONSE).safeGetAsString())
        assertEquals("" + askOrder1.priceNQT, askOrder1Result.get(PRICE_NQT_RESPONSE).safeGetAsString())
        assertEquals(askOrder1.height.toLong(), askOrder1Result.get(HEIGHT_RESPONSE).safeGetAsLong())
    }
}
