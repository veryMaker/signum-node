package brs.api.http

import brs.entity.Asset
import brs.services.AssetExchangeService
import brs.common.QuickMocker
import brs.api.http.JSONResponses.INCORRECT_ASSET
import brs.api.http.JSONResponses.UNKNOWN_ASSET
import brs.api.http.common.Parameters.ASSETS_PARAMETER
import brs.api.http.common.ResultFields.ASSETS_RESPONSE
import brs.api.http.common.ResultFields.NUMBER_OF_ACCOUNTS_RESPONSE
import brs.api.http.common.ResultFields.NUMBER_OF_TRADES_RESPONSE
import brs.api.http.common.ResultFields.NUMBER_OF_TRANSFERS_RESPONSE
import brs.util.json.safeGetAsLong
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAssetsTest {

    private lateinit var t: GetAssets

    private lateinit var mockAssetExchangeService: AssetExchangeService

    @Before
    fun setUp() {
        mockAssetExchangeService = mock()

        t = GetAssets(mockAssetExchangeService)
    }

    @Test
    fun processRequest() {
        val assetId = 123L

        val request = QuickMocker.httpServletRequest()
        whenever(request.getParameterValues(eq(ASSETS_PARAMETER))).doReturn(arrayOf(assetId.toString(), ""))

        val mockTradeCount = 1
        val mockTransferCount = 2
        val mockAccountsCount = 3

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).doReturn(assetId)

        whenever(mockAssetExchangeService.getAsset(eq(assetId))).doReturn(mockAsset)

        whenever(mockAssetExchangeService.getTradeCount(eq(assetId))).doReturn(mockTradeCount)
        whenever(mockAssetExchangeService.getTransferCount(eq(assetId))).doReturn(mockTransferCount)
        whenever(mockAssetExchangeService.getAssetAccountsCount(eq(assetId))).doReturn(mockAccountsCount)

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
    fun processRequest_unknownAsset() {
        val assetId = 123L

        val request = QuickMocker.httpServletRequest()
        whenever(request.getParameterValues(eq(ASSETS_PARAMETER))).doReturn(arrayOf(assetId.toString()))

        whenever(mockAssetExchangeService.getAsset(eq(assetId))).doReturn(null)

        assertEquals(UNKNOWN_ASSET, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectAsset() {
        val request = QuickMocker.httpServletRequest()

        whenever(request.getParameterValues(eq(ASSETS_PARAMETER))).doReturn(arrayOf("unParsable"))

        assertEquals(INCORRECT_ASSET, t.processRequest(request))
    }

}
