package brs.api.http

import brs.entity.Asset
import brs.entity.Order.Ask
import brs.services.AssetExchangeService
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.api.http.common.Parameters.ASSET_PARAMETER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.ASK_ORDER_IDS_RESPONSE
import brs.services.ParameterService
import com.google.gson.JsonArray
import brs.util.jetty.get
import com.google.gson.JsonObject
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAskOrderIdsTest : AbstractUnitTest() {

    private lateinit var parameterServiceMock: ParameterService
    private lateinit var assetExchangeServiceMock: AssetExchangeService

    private lateinit var t: GetAskOrderIds

    @Before
    fun setUp() {
        parameterServiceMock = mockk(relaxed = true)
        assetExchangeServiceMock = mockk(relaxed = true)

        t = GetAskOrderIds(parameterServiceMock, assetExchangeServiceMock)
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
        every { askOrder1.id } returns 5L
        val askOrder2 = mockk<Ask>(relaxed = true)
        every { askOrder1.id } returns 6L

        val askIterator = mockCollection(askOrder1, askOrder2)

        every { assetExchangeServiceMock.getSortedAskOrders(eq(assetIndex), eq(firstIndex), eq(lastIndex)) } returns askIterator

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val ids = result.get(ASK_ORDER_IDS_RESPONSE) as JsonArray
        assertNotNull(ids)

        assertEquals(2, ids.size().toLong())
    }
}
