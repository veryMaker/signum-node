package brs.http

import brs.Asset
import brs.assetexchange.AssetExchange
import brs.common.QuickMocker
import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.INCORRECT_ASSET
import brs.http.JSONResponses.UNKNOWN_ASSET
import brs.http.common.Parameters.ASSETS_PARAMETER
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAssetsTest {

    private var t: GetAssets? = null

    private var mockAssetExchange: AssetExchange? = null

    @Before
    fun setUp() {
        mockAssetExchange = mock<AssetExchange>()

        t = GetAssets(mockAssetExchange!!)
    }

    @Test
    fun processRequest() {
        val assetId = 123L

        val request = QuickMocker.httpServletRequest()
        whenever(request.getParameterValues(eq(ASSETS_PARAMETER))).doReturn(arrayOf("" + assetId, ""))

        val mockTradeCount = 1
        val mockTransferCount = 2
        val mockAccountsCount = 3

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).doReturn(assetId)

        whenever(mockAssetExchange!!.getAsset(eq(assetId))).doReturn(mockAsset)

        whenever(mockAssetExchange!!.getTradeCount(eq(assetId))).doReturn(mockTradeCount)
        whenever(mockAssetExchange!!.getTransferCount(eq(assetId))).doReturn(mockTransferCount)
        whenever(mockAssetExchange!!.getAssetAccountsCount(eq(assetId))).doReturn(mockAccountsCount)

        val response = t!!.processRequest(request) as JsonObject
        assertNotNull(response)

        val responseList = response.get(ASSETS_RESPONSE) as JsonArray
        assertNotNull(responseList)
        assertEquals(1, responseList.size().toLong())

        val assetResponse = responseList.get(0) as JsonObject
        assertNotNull(assetResponse)
        assertEquals(mockTradeCount.toLong(), JSON.getAsInt(assetResponse.get(NUMBER_OF_TRADES_RESPONSE)).toLong())
        assertEquals(mockTransferCount.toLong(), JSON.getAsInt(assetResponse.get(NUMBER_OF_TRANSFERS_RESPONSE)).toLong())
        assertEquals(mockAccountsCount.toLong(), JSON.getAsInt(assetResponse.get(NUMBER_OF_ACCOUNTS_RESPONSE)).toLong())
    }

    @Test
    fun processRequest_unknownAsset() {
        val assetId = 123L

        val request = QuickMocker.httpServletRequest()
        whenever(request.getParameterValues(eq(ASSETS_PARAMETER))).doReturn(arrayOf(assetId.toString()))

        whenever(mockAssetExchange!!.getAsset(eq(assetId))).doReturn(null)

        assertEquals(UNKNOWN_ASSET, t!!.processRequest(request))
    }

    @Test
    fun processRequest_incorrectAsset() {
        val request = QuickMocker.httpServletRequest()

        whenever(request.getParameterValues(eq(ASSETS_PARAMETER))).doReturn(arrayOf("unParsable"))

        assertEquals(INCORRECT_ASSET, t!!.processRequest(request))
    }

}
