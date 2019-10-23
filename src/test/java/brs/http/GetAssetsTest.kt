package brs.http

import brs.Asset
import brs.assetexchange.AssetExchange
import brs.common.QuickMocker
import brs.http.JSONResponses.INCORRECT_ASSET
import brs.http.JSONResponses.UNKNOWN_ASSET
import brs.http.common.Parameters.ASSETS_PARAMETER
import brs.http.common.ResultFields.ASSETS_RESPONSE
import brs.http.common.ResultFields.NUMBER_OF_ACCOUNTS_RESPONSE
import brs.http.common.ResultFields.NUMBER_OF_TRADES_RESPONSE
import brs.http.common.ResultFields.NUMBER_OF_TRANSFERS_RESPONSE
import brs.util.safeGetAsLong
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

class GetAssetsTest {

    private lateinit var t: GetAssets

    private lateinit var mockAssetExchange: AssetExchange

    @Before
    fun setUp() {
        mockAssetExchange = mock()

        t = GetAssets(mockAssetExchange)
    }

    @Test
    fun processRequest() = runBlocking {
        val assetId = 123L

        val request = QuickMocker.httpServletRequest()
        whenever(request.getParameterValues(eq(ASSETS_PARAMETER))).doReturn(arrayOf("" + assetId, ""))

        val mockTradeCount = 1
        val mockTransferCount = 2
        val mockAccountsCount = 3

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).doReturn(assetId)

        whenever(mockAssetExchange.getAsset(eq(assetId))).doReturn(mockAsset)

        whenever(mockAssetExchange.getTradeCount(eq(assetId))).doReturn(mockTradeCount)
        whenever(mockAssetExchange.getTransferCount(eq(assetId))).doReturn(mockTransferCount)
        whenever(mockAssetExchange.getAssetAccountsCount(eq(assetId))).doReturn(mockAccountsCount)

        val response = t.processRequest(request) as JsonObject
        assertNotNull(response)

        val responseList = response.get(ASSETS_RESPONSE) as JsonArray
        assertNotNull(responseList)
        assertEquals(1, responseList.size().toLong())

        val assetResponse = responseList.get(0) as JsonObject
        assertNotNull(assetResponse)
        assertEquals(mockTradeCount.toLong(), assetResponse.get(NUMBER_OF_TRADES_RESPONSE).safeGetAsLong())
        assertEquals(mockTransferCount.toLong(), assetResponse.get(NUMBER_OF_TRANSFERS_RESPONSE).safeGetAsLong())
        assertEquals(mockAccountsCount.toLong(), assetResponse.get(NUMBER_OF_ACCOUNTS_RESPONSE).safeGetAsLong())
    }

    @Test
    fun processRequest_unknownAsset() = runBlocking {
        val assetId = 123L

        val request = QuickMocker.httpServletRequest()
        whenever(request.getParameterValues(eq(ASSETS_PARAMETER))).doReturn(arrayOf(assetId.toString()))

        whenever(mockAssetExchange.getAsset(eq(assetId))).doReturn(null)

        assertEquals(UNKNOWN_ASSET, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectAsset() = runBlocking {
        val request = QuickMocker.httpServletRequest()

        whenever(request.getParameterValues(eq(ASSETS_PARAMETER))).doReturn(arrayOf("unParsable"))

        assertEquals(INCORRECT_ASSET, t.processRequest(request))
    }

}
