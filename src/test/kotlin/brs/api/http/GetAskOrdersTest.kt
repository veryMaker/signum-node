package brs.api.http

import brs.api.http.common.Parameters.ASSET_PARAMETER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.ASK_ORDERS_RESPONSE
import brs.api.http.common.ResultFields.ASSET_RESPONSE
import brs.api.http.common.ResultFields.HEIGHT_RESPONSE
import brs.api.http.common.ResultFields.ORDER_RESPONSE
import brs.api.http.common.ResultFields.PRICE_PLANCK_RESPONSE
import brs.api.http.common.ResultFields.QUANTITY_QNT_RESPONSE
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.entity.Asset
import brs.entity.Order.Ask
import brs.services.AssetExchangeService
import brs.services.ParameterService
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

class GetAskOrdersTest : AbstractUnitTest() {

    private lateinit var parameterServiceMock: ParameterService
    private lateinit var assetExchangeServiceMock: AssetExchangeService

    private lateinit var t: GetAskOrders

    @Before
    fun setUp() {
        parameterServiceMock = mockk(relaxed = true)
        assetExchangeServiceMock = mockk(relaxed = true)

        t = GetAskOrders(parameterServiceMock, assetExchangeServiceMock)
    }

    @Test
    fun processRequest() {
        val assetIndex: Long = 5
        val firstIndex = 1
        val lastIndex = 3

        val request = QuickMocker.httpServletRequest(
                MockParam(ASSET_PARAMETER, assetIndex),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex)
        )

        val asset = mockk<Asset>(relaxed = true)
        every { asset.id } returns assetIndex

        every { parameterServiceMock.getAsset(eq(request)) } returns asset

        val askOrder1 = mockk<Ask>(relaxed = true)
        every { askOrder1.id } returns 3L
        every { askOrder1.assetId } returns assetIndex
        every { askOrder1.quantity } returns 56L
        every { askOrder1.pricePlanck } returns 45L
        every { askOrder1.height } returns 32

        val askOrder2 = mockk<Ask>(relaxed = true)
        every { askOrder1.id } returns 4L

        val askIterator = mockCollection(askOrder1, askOrder2)

        every { assetExchangeServiceMock.getSortedAskOrders(eq(assetIndex), eq(firstIndex), eq(lastIndex)) } returns askIterator

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val orders = result.get(ASK_ORDERS_RESPONSE) as JsonArray
        assertNotNull(orders)

        assertEquals(2, orders.size().toLong())

        val askOrder1Result = orders.get(0) as JsonObject

        assertEquals(askOrder1.id.toString(), askOrder1Result.getMemberAsString(ORDER_RESPONSE))
        assertEquals(askOrder1.assetId.toString(), askOrder1Result.getMemberAsString(ASSET_RESPONSE))
        assertEquals(askOrder1.quantity.toString(), askOrder1Result.getMemberAsString(QUANTITY_QNT_RESPONSE))
        assertEquals(askOrder1.pricePlanck.toString(), askOrder1Result.getMemberAsString(PRICE_PLANCK_RESPONSE))
        assertEquals(askOrder1.height.toLong(), askOrder1Result.getMemberAsLong(HEIGHT_RESPONSE))
    }
}
