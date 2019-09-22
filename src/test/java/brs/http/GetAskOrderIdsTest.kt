package brs.http

import brs.Asset
import brs.BurstException
import brs.Order.Ask
import brs.assetexchange.AssetExchange
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest
import brs.http.common.ResultFields.ASK_ORDER_IDS_RESPONSE
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAskOrderIdsTest : AbstractUnitTest() {

    private var parameterServiceMock: ParameterService? = null
    private var assetExchangeMock: AssetExchange? = null

    private var t: GetAskOrderIds? = null

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        assetExchangeMock = mock<AssetExchange>()

        t = GetAskOrderIds(parameterServiceMock!!, assetExchangeMock!!)
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

        val asset = mock<Asset>()
        whenever(asset.id).doReturn(assetIndex)

        whenever(parameterServiceMock!!.getAsset(eq<HttpServletRequest>(request))).doReturn(asset)

        val askOrder1 = mock<Ask>()
        whenever(askOrder1.id).doReturn(5L)
        val askOrder2 = mock<Ask>()
        whenever(askOrder1.id).doReturn(6L)

        val askIterator = this.mockCollection<Ask>(askOrder1, askOrder2)

        whenever(assetExchangeMock!!.getSortedAskOrders(eq(assetIndex), eq(firstIndex), eq(lastIndex))).doReturn(askIterator)

        val result = t!!.processRequest(request) as JsonObject
        assertNotNull(result)

        val ids = result.get(ASK_ORDER_IDS_RESPONSE) as JsonArray
        assertNotNull(ids)

        assertEquals(2, ids.size().toLong())
    }
}
