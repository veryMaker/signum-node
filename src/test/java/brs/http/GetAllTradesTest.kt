package brs.http

import brs.Asset
import brs.Trade
import brs.assetexchange.AssetExchange
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.INCLUDE_ASSET_INFO_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.http.common.ResultFields.ASSET_RESPONSE
import brs.http.common.ResultFields.NAME_RESPONSE
import brs.http.common.ResultFields.PRICE_NQT_RESPONSE
import brs.http.common.ResultFields.TRADES_RESPONSE
import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAllTradesTest : AbstractUnitTest() {

    private lateinit var t: GetAllTrades

    private lateinit var mockAssetExchange: AssetExchange

    @Before
    fun setUp() {
        mockAssetExchange = mock<AssetExchange>()

        t = GetAllTrades(mockAssetExchange!!)
    }

    @Test
    fun processRequest_withAssetsInformation() = runBlocking {
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

        val priceNQT = 123L
        val mockTrade = mock<Trade>()
        whenever(mockTrade.priceNQT).doReturn(priceNQT)
        whenever(mockTrade.timestamp).doReturn(2)
        whenever(mockTrade.assetId).doReturn(mockAssetId)

        val mockTradeIterator = mockCollection<Trade>(mockTrade)

        whenever(mockAssetExchange!!.getAllTrades(eq(0), eq(-1))).doReturn(mockTradeIterator)
        whenever(mockAssetExchange!!.getAsset(eq(mockAssetId))).doReturn(mockAsset)

        val result = t!!.processRequest(request) as JsonObject
        assertNotNull(result)

        val tradesResult = result.get(TRADES_RESPONSE) as JsonArray
        assertNotNull(tradesResult)
        assertEquals(1, tradesResult.size().toLong())

        val tradeAssetInfoResult = tradesResult.get(0) as JsonObject
        assertNotNull(tradeAssetInfoResult)

        assertEquals("" + priceNQT, JSON.getAsString(tradeAssetInfoResult.get(PRICE_NQT_RESPONSE)))
        assertEquals("" + mockAssetId, JSON.getAsString(tradeAssetInfoResult.get(ASSET_RESPONSE)))
        assertEquals(mockAssetName, JSON.getAsString(tradeAssetInfoResult.get(NAME_RESPONSE)))
    }

    @Test
    fun processRequest_withoutAssetsInformation() = runBlocking<Unit> {
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
        val priceNQT = 123L
        val mockTrade = mock<Trade>()
        whenever(mockTrade.priceNQT).doReturn(priceNQT)
        whenever(mockTrade.timestamp).doReturn(2)
        whenever(mockTrade.assetId).doReturn(mockAssetId)

        val mockTradeIterator = mockCollection<Trade>(mockTrade)

        whenever(mockAssetExchange!!.getAllTrades(eq(0), eq(-1))).doReturn(mockTradeIterator)

        val result = t!!.processRequest(request) as JsonObject
        assertNotNull(result)

        val tradesResult = result.get(TRADES_RESPONSE) as JsonArray
        assertNotNull(tradesResult)
        assertEquals(1, tradesResult.size().toLong())

        val tradeAssetInfoResult = tradesResult.get(0) as JsonObject
        assertNotNull(tradeAssetInfoResult)

        assertEquals("" + priceNQT, JSON.getAsString(tradeAssetInfoResult.get(PRICE_NQT_RESPONSE)))
        assertEquals("" + mockAssetId, JSON.getAsString(tradeAssetInfoResult.get(ASSET_RESPONSE)))
        assertEquals("", JSON.getAsString(tradeAssetInfoResult.get(NAME_RESPONSE)))

        verify(mockAssetExchange!!, never()).getAsset(eq(mockAssetId))
    }

}
