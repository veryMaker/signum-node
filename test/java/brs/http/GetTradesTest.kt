package brs.http

import brs.Account
import brs.Asset
import brs.BurstException
import brs.Trade
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

import brs.http.common.Parameters.*
import brs.http.common.ResultFields.TRADES_RESPONSE
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class GetTradesTest : AbstractUnitTest() {

    private var t: GetTrades? = null

    private var mockParameterService: ParameterService? = null
    private var mockAssetExchange: AssetExchange? = null

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        mockAssetExchange = mock<AssetExchange>()

        t = GetTrades(mockParameterService!!, mockAssetExchange!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_withAssetId() {
        val assetId = 123L
        val firstIndex = 0
        val lastIndex = 1
        val includeAssetInfo = true

        val req = QuickMocker.httpServletRequest(
                MockParam(ASSET_PARAMETER, assetId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex),
                MockParam(INCLUDE_ASSET_INFO_PARAMETER, includeAssetInfo)
        )

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).doReturn(assetId)

        val mockTrade = mock<Trade>()
        val mockTradesIterator = mockCollection<Trade>(mockTrade)

        whenever(mockParameterService!!.getAsset(eq<HttpServletRequest>(req))).doReturn(mockAsset)
        whenever(mockAssetExchange!!.getTrades(eq(assetId), eq(firstIndex), eq(lastIndex))).doReturn(mockTradesIterator)

        val result = t!!.processRequest(req) as JsonObject
        assertNotNull(result)

        val trades = result.get(TRADES_RESPONSE) as JsonArray
        assertNotNull(trades)
        assertEquals(1, trades.size().toLong())

        val tradeResult = trades.get(0) as JsonObject
        assertNotNull(tradeResult)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_withAccountId() {
        val accountId = 321L
        val firstIndex = 0
        val lastIndex = 1
        val includeAssetInfo = true

        val req = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, accountId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex),
                MockParam(INCLUDE_ASSET_INFO_PARAMETER, includeAssetInfo)
        )

        val mockAccount = mock<Account>()
        whenever(mockAccount.getId()).doReturn(accountId)

        val mockTrade = mock<Trade>()
        val mockTradesIterator = mockCollection<Trade>(mockTrade)

        whenever(mockParameterService!!.getAccount(eq<HttpServletRequest>(req))).doReturn(mockAccount)
        whenever(mockAssetExchange!!.getAccountTrades(eq(accountId), eq(firstIndex), eq(lastIndex))).doReturn(mockTradesIterator)

        val result = t!!.processRequest(req) as JsonObject
        assertNotNull(result)

        val trades = result.get(TRADES_RESPONSE) as JsonArray
        assertNotNull(trades)
        assertEquals(1, trades.size().toLong())

        val tradeResult = trades.get(0) as JsonObject
        assertNotNull(tradeResult)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_withAssetIdAndAccountId() {
        val assetId = 123L
        val accountId = 321L
        val firstIndex = 0
        val lastIndex = 1
        val includeAssetInfo = true

        val req = QuickMocker.httpServletRequest(
                MockParam(ASSET_PARAMETER, assetId),
                MockParam(ACCOUNT_PARAMETER, accountId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex),
                MockParam(INCLUDE_ASSET_INFO_PARAMETER, includeAssetInfo)
        )

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).doReturn(assetId)

        val mockAccount = mock<Account>()
        whenever(mockAccount.getId()).doReturn(accountId)

        val mockTrade = mock<Trade>()
        val mockTradesIterator = mockCollection<Trade>(mockTrade)

        whenever(mockParameterService!!.getAsset(eq<HttpServletRequest>(req))).doReturn(mockAsset)
        whenever(mockParameterService!!.getAccount(eq<HttpServletRequest>(req))).doReturn(mockAccount)
        whenever(mockAssetExchange!!.getAccountAssetTrades(eq(accountId), eq(assetId), eq(firstIndex), eq(lastIndex))).doReturn(mockTradesIterator)

        val result = t!!.processRequest(req) as JsonObject
        assertNotNull(result)

        val trades = result.get(TRADES_RESPONSE) as JsonArray
        assertNotNull(trades)
        assertEquals(1, trades.size().toLong())

        val tradeResult = trades.get(0) as JsonObject
        assertNotNull(tradeResult)
    }
}
