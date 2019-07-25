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
import brs.http.common.ResultFields.*
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

        val req = QuickMocker.httpServletRequest()
        whenever<Array<String>>(req.getParameterValues(eq(ASSETS_PARAMETER))).thenReturn(arrayOf("" + assetId, ""))

        val mockTradeCount = 1
        val mockTransferCount = 2
        val mockAccountsCount = 3

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).thenReturn(assetId)

        whenever(mockAssetExchange!!.getAsset(eq(assetId))).thenReturn(mockAsset)

        whenever(mockAssetExchange!!.getTradeCount(eq(assetId))).thenReturn(mockTradeCount)
        whenever(mockAssetExchange!!.getTransferCount(eq(assetId))).thenReturn(mockTransferCount)
        whenever(mockAssetExchange!!.getAssetAccountsCount(eq(assetId))).thenReturn(mockAccountsCount)

        val response = t!!.processRequest(req) as JsonObject
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

        val req = QuickMocker.httpServletRequest()
        whenever<Array<String>>(req.getParameterValues(eq(ASSETS_PARAMETER))).thenReturn(arrayOf("" + assetId))

        whenever(mockAssetExchange!!.getAsset(eq(assetId))).thenReturn(null)

        assertEquals(UNKNOWN_ASSET, t!!.processRequest(req))
    }

    @Test
    fun processRequest_incorrectAsset() {
        val req = QuickMocker.httpServletRequest()

        whenever<Array<String>>(req.getParameterValues(eq(ASSETS_PARAMETER))).thenReturn(arrayOf("unParsable"))

        assertEquals(INCORRECT_ASSET, t!!.processRequest(req))
    }

}
