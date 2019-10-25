package brs.api.http

import brs.entity.Asset
import brs.entity.Trade
import brs.services.AssetExchangeService
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.INCLUDE_ASSET_INFO_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.api.http.common.ResultFields.ASSET_RESPONSE
import brs.api.http.common.ResultFields.NAME_RESPONSE
import brs.api.http.common.ResultFields.PRICE_PLANCK_RESPONSE
import brs.api.http.common.ResultFields.TRADES_RESPONSE
import brs.util.json.safeGetAsString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAllTradesTest : AbstractUnitTest() {

    private lateinit var t: GetAllTrades

    private lateinit var mockAssetExchangeService: AssetExchangeService

    @Before
    fun setUp() {
        mockAssetExchangeService = mock()

        t = GetAllTrades(mockAssetExchangeService)
    }

    @Test
    fun processRequest_withAssetsInformation() {
        val timestamp = 1
        val firstIndex = 0
        val lastIndex = 1

        val request = QuickMocker.httpServletRequest(
                MockParam(TIMESTAMP_PARAMETER, timestamp),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex),
                MockParam(INCLUDE_ASSET_INFO_PARAMETER, true)
        )

        val mockAssetId = 123L
        val mockAssetName = "mockAssetName"
        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).doReturn(mockAssetId)
        whenever(mockAsset.name).doReturn(mockAssetName)

        val pricePlanck = 123L
        val mockTrade = mock<Trade>()
        whenever(mockTrade.pricePlanck).doReturn(pricePlanck)
        whenever(mockTrade.timestamp).doReturn(2)
        whenever(mockTrade.assetId).doReturn(mockAssetId)

        val mockTradeIterator = mockCollection(mockTrade)

        whenever(mockAssetExchangeService.getAllTrades(eq(0), eq(-1))).doReturn(mockTradeIterator)
        whenever(mockAssetExchangeService.getAsset(eq(mockAssetId))).doReturn(mockAsset)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val tradesResult = result.get(TRADES_RESPONSE) as JsonArray
        assertNotNull(tradesResult)
        assertEquals(1, tradesResult.size().toLong())

        val tradeAssetInfoResult = tradesResult.get(0) as JsonObject
        assertNotNull(tradeAssetInfoResult)

        assertEquals(pricePlanck.toString(), tradeAssetInfoResult.get(PRICE_PLANCK_RESPONSE).safeGetAsString())
        assertEquals(mockAssetId.toString(), tradeAssetInfoResult.get(ASSET_RESPONSE).safeGetAsString())
        assertEquals(mockAssetName, tradeAssetInfoResult.get(NAME_RESPONSE).safeGetAsString())
    }

    @Test
    fun processRequest_withoutAssetsInformation() {
        val timestamp = 1
        val firstIndex = 0
        val lastIndex = 1

        val request = QuickMocker.httpServletRequest(
                MockParam(TIMESTAMP_PARAMETER, timestamp),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex),
                MockParam(INCLUDE_ASSET_INFO_PARAMETER, false)
        )

        val mockAssetId = 123L
        val pricePlanck = 123L
        val mockTrade = mock<Trade>()
        whenever(mockTrade.pricePlanck).doReturn(pricePlanck)
        whenever(mockTrade.timestamp).doReturn(2)
        whenever(mockTrade.assetId).doReturn(mockAssetId)

        val mockTradeIterator = mockCollection(mockTrade)

        whenever(mockAssetExchangeService.getAllTrades(eq(0), eq(-1))).doReturn(mockTradeIterator)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val tradesResult = result.get(TRADES_RESPONSE) as JsonArray
        assertNotNull(tradesResult)
        assertEquals(1, tradesResult.size().toLong())

        val tradeAssetInfoResult = tradesResult.get(0) as JsonObject
        assertNotNull(tradeAssetInfoResult)

        assertEquals(pricePlanck.toString(), tradeAssetInfoResult.get(PRICE_PLANCK_RESPONSE).safeGetAsString())
        assertEquals(mockAssetId.toString(), tradeAssetInfoResult.get(ASSET_RESPONSE).safeGetAsString())
        assertEquals(null, tradeAssetInfoResult.get(NAME_RESPONSE).safeGetAsString())

        verify(mockAssetExchangeService, never()).getAsset(eq(mockAssetId))
    }

}
