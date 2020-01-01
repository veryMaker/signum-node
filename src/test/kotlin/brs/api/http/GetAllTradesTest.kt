package brs.api.http

import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.INCLUDE_ASSET_INFO_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.api.http.common.ResultFields.ASSET_RESPONSE
import brs.api.http.common.ResultFields.NAME_RESPONSE
import brs.api.http.common.ResultFields.PRICE_PLANCK_RESPONSE
import brs.api.http.common.ResultFields.TRADES_RESPONSE
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.entity.Asset
import brs.entity.Trade
import brs.services.AssetExchangeService
import brs.util.json.getMemberAsString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAllTradesTest : AbstractUnitTest() {

    private lateinit var t: GetAllTrades

    private lateinit var mockAssetExchangeService: AssetExchangeService

    @Before
    fun setUp() {
        mockAssetExchangeService = mockk(relaxed = true)

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
        val mockAsset = mockk<Asset>(relaxed = true)
        every { mockAsset.id } returns mockAssetId
        every { mockAsset.name } returns mockAssetName

        val pricePlanck = 123L
        val mockTrade = mockk<Trade>(relaxed = true)
        every { mockTrade.pricePlanck } returns pricePlanck
        every { mockTrade.timestamp } returns 2
        every { mockTrade.assetId } returns mockAssetId

        val mockTradeIterator = mockCollection(mockTrade)

        every { mockAssetExchangeService.getAllTrades(eq(0), eq(-1)) } returns mockTradeIterator
        every { mockAssetExchangeService.getAsset(eq(mockAssetId)) } returns mockAsset

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val tradesResult = result.get(TRADES_RESPONSE) as JsonArray
        assertNotNull(tradesResult)
        assertEquals(1, tradesResult.size().toLong())

        val tradeAssetInfoResult = tradesResult.get(0) as JsonObject
        assertNotNull(tradeAssetInfoResult)

        assertEquals(pricePlanck.toString(), tradeAssetInfoResult.getMemberAsString(PRICE_PLANCK_RESPONSE))
        assertEquals(mockAssetId.toString(), tradeAssetInfoResult.getMemberAsString(ASSET_RESPONSE))
        assertEquals(mockAssetName, tradeAssetInfoResult.getMemberAsString(NAME_RESPONSE))
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
        val mockTrade = mockk<Trade>(relaxed = true)
        every { mockTrade.pricePlanck } returns pricePlanck
        every { mockTrade.timestamp } returns 2
        every { mockTrade.assetId } returns mockAssetId

        val mockTradeIterator = mockCollection(mockTrade)

        every { mockAssetExchangeService.getAllTrades(eq(0), eq(-1)) } returns mockTradeIterator

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val tradesResult = result.get(TRADES_RESPONSE) as JsonArray
        assertNotNull(tradesResult)
        assertEquals(1, tradesResult.size().toLong())

        val tradeAssetInfoResult = tradesResult.get(0) as JsonObject
        assertNotNull(tradeAssetInfoResult)

        assertEquals(pricePlanck.toString(), tradeAssetInfoResult.getMemberAsString(PRICE_PLANCK_RESPONSE))
        assertEquals(mockAssetId.toString(), tradeAssetInfoResult.getMemberAsString(ASSET_RESPONSE))
        assertEquals(null, tradeAssetInfoResult.getMemberAsString(NAME_RESPONSE))

        verify(exactly = 0) { mockAssetExchangeService.getAsset(eq(mockAssetId)) }
    }
}
