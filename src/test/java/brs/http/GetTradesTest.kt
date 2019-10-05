package brs.http

import brs.Account
import brs.Asset
import brs.Trade
import brs.assetexchange.AssetExchange
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.ASSET_PARAMETER
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.INCLUDE_ASSET_INFO_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.ResultFields.TRADES_RESPONSE
import brs.services.ParameterService
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

class GetTradesTest : AbstractUnitTest() {

    private lateinit var t: GetTrades

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockAssetExchange: AssetExchange

    @Before
    fun setUp() {
        mockParameterService = mock()
        mockAssetExchange = mock()

        t = GetTrades(mockParameterService, mockAssetExchange)
    }

    @Test
    fun processRequest_withAssetId() = runBlocking {
        val assetId = 123L
        val firstIndex = 0
        val lastIndex = 1
        val includeAssetInfo = true

        val request = QuickMocker.httpServletRequest(
                MockParam(ASSET_PARAMETER, assetId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex),
                MockParam(INCLUDE_ASSET_INFO_PARAMETER, includeAssetInfo)
        )

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).doReturn(assetId)

        val mockTrade = mock<Trade>()
        val mockTradesIterator = mockCollection(mockTrade)

        whenever(mockParameterService.getAsset(eq(request))).doReturn(mockAsset)
        whenever(mockAssetExchange.getTrades(eq(assetId), eq(firstIndex), eq(lastIndex))).doReturn(mockTradesIterator)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val trades = result.get(TRADES_RESPONSE) as JsonArray
        assertNotNull(trades)
        assertEquals(1, trades.size().toLong())

        val tradeResult = trades.get(0) as JsonObject
        assertNotNull(tradeResult)
    }

    @Test
    fun processRequest_withAccountId() = runBlocking {
        val accountId = 321L
        val firstIndex = 0
        val lastIndex = 1
        val includeAssetInfo = true

        val request = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, accountId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex),
                MockParam(INCLUDE_ASSET_INFO_PARAMETER, includeAssetInfo)
        )

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(accountId)

        val mockTrade = mock<Trade>()
        val mockTradesIterator = mockCollection(mockTrade)

        whenever(mockParameterService.getAccount(eq(request))).doReturn(mockAccount)
        whenever(mockAssetExchange.getAccountTrades(eq(accountId), eq(firstIndex), eq(lastIndex))).doReturn(mockTradesIterator)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val trades = result.get(TRADES_RESPONSE) as JsonArray
        assertNotNull(trades)
        assertEquals(1, trades.size().toLong())

        val tradeResult = trades.get(0) as JsonObject
        assertNotNull(tradeResult)
    }

    @Test
    fun processRequest_withAssetIdAndAccountId() = runBlocking {
        val assetId = 123L
        val accountId = 321L
        val firstIndex = 0
        val lastIndex = 1
        val includeAssetInfo = true

        val request = QuickMocker.httpServletRequest(
                MockParam(ASSET_PARAMETER, assetId),
                MockParam(ACCOUNT_PARAMETER, accountId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex),
                MockParam(INCLUDE_ASSET_INFO_PARAMETER, includeAssetInfo)
        )

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).doReturn(assetId)

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(accountId)

        val mockTrade = mock<Trade>()
        val mockTradesIterator = mockCollection(mockTrade)

        whenever(mockParameterService.getAsset(eq(request))).doReturn(mockAsset)
        whenever(mockParameterService.getAccount(eq(request))).doReturn(mockAccount)
        whenever(mockAssetExchange.getAccountAssetTrades(eq(accountId), eq(assetId), eq(firstIndex), eq(lastIndex))).doReturn(mockTradesIterator)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val trades = result.get(TRADES_RESPONSE) as JsonArray
        assertNotNull(trades)
        assertEquals(1, trades.size().toLong())

        val tradeResult = trades.get(0) as JsonObject
        assertNotNull(tradeResult)
    }
}
