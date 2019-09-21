package brs.http

import brs.Asset
import brs.BurstException
import brs.Order.Ask
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
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAskOrdersTest : AbstractUnitTest() {

    private var parameterServiceMock: ParameterService? = null
    private var assetExchangeMock: AssetExchange? = null

    private var t: GetAskOrders? = null

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        assetExchangeMock = mock<AssetExchange>()

        t = GetAskOrders(parameterServiceMock!!, assetExchangeMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
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

        whenever(parameterServiceMock!!.getAsset(eq<HttpServletRequest>(request))).doReturn(asset)

        val askOrder1 = mock<Ask>()
        whenever(askOrder1.id).doReturn(3L)
        whenever(askOrder1.assetId).doReturn(assetIndex)
        whenever(askOrder1.quantityQNT).doReturn(56L)
        whenever(askOrder1.priceNQT).doReturn(45L)
        whenever(askOrder1.height).doReturn(32)

        val askOrder2 = mock<Ask>()
        whenever(askOrder1.id).doReturn(4L)

        val askIterator = this.mockCollection<Ask>(askOrder1, askOrder2)

        whenever(assetExchangeMock!!.getSortedAskOrders(eq(assetIndex), eq(firstIndex), eq(lastIndex))).doReturn(askIterator)

        val result = t!!.processRequest(request) as JsonObject
        assertNotNull(result)

        val orders = result.get(ASK_ORDERS_RESPONSE) as JsonArray
        assertNotNull(orders)

        assertEquals(2, orders.size().toLong())

        val askOrder1Result = orders.get(0) as JsonObject

        assertEquals("" + askOrder1.id, JSON.getAsString(askOrder1Result.get(ORDER_RESPONSE)))
        assertEquals("" + askOrder1.assetId, JSON.getAsString(askOrder1Result.get(ASSET_RESPONSE)))
        assertEquals("" + askOrder1.quantityQNT, JSON.getAsString(askOrder1Result.get(QUANTITY_QNT_RESPONSE)))
        assertEquals("" + askOrder1.priceNQT, JSON.getAsString(askOrder1Result.get(PRICE_NQT_RESPONSE)))
        assertEquals(askOrder1.height.toLong(), JSON.getAsInt(askOrder1Result.get(HEIGHT_RESPONSE)).toLong())
    }
}
