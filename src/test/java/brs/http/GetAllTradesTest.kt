package brs.http

import brs.Asset
import brs.BurstException
import brs.Trade
import brs.assetexchange.AssetExchange
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.*

class GetAllTradesTest : AbstractUnitTest() {

    private var t: GetAllTrades? = null

    private var mockAssetExchange: AssetExchange? = null

    @Before
    fun setUp() {
        mockAssetExchange = mock<AssetExchange>()

        t = GetAllTrades(mockAssetExchange!!)
    }

    @Test
    @Throws(BurstException::class)
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
    @Throws(BurstException::class)
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
        assertNull(JSON.getAsString(tradeAssetInfoResult.get(NAME_RESPONSE)))

        verify(mockAssetExchange!!, never()).getAsset(eq(mockAssetId))
    }

}
