package brs.http

import brs.Asset
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

import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.ResultFields.*
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAllAssetsTest : AbstractUnitTest() {

    private var t: GetAllAssets? = null

    private var assetExchange: AssetExchange? = null

    @Before
    fun setUp() {
        assetExchange = mock<AssetExchange>()

        t = GetAllAssets(assetExchange!!)
    }

    @Test
    fun processRequest() {
        val firstIndex = 1
        val lastIndex = 2

        val req = QuickMocker.httpServletRequest(
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex)
        )

        val mockAssetId: Long = 1

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).thenReturn(1L)
        whenever(mockAsset.id).thenReturn(mockAssetId)
        whenever(mockAsset.name).thenReturn("name")
        whenever(mockAsset.description).thenReturn("description")
        whenever(mockAsset.decimals).thenReturn(1.toByte())
        whenever(mockAsset.quantityQNT).thenReturn(2L)

        val mockAssetIterator = mockCollection<Asset>(mockAsset)

        whenever(assetExchange!!.getAllAssets(eq(firstIndex), eq(lastIndex))).thenReturn(mockAssetIterator)
        whenever(assetExchange!!.getAssetAccountsCount(eq(mockAssetId))).thenReturn(1)
        whenever(assetExchange!!.getTransferCount(eq(mockAssetId))).thenReturn(2)
        whenever(assetExchange!!.getTradeCount(eq(mockAssetId))).thenReturn(3)

        val result = t!!.processRequest(req) as JsonObject
        assertNotNull(result)

        val assetsResult = result.get(ASSETS_RESPONSE) as JsonArray
        assertNotNull(assetsResult)
        assertEquals(1, assetsResult.size().toLong())

        val assetResult = assetsResult.get(0) as JsonObject
        assertNotNull(assetResult)

        assertEquals(mockAsset.name, JSON.getAsString(assetResult.get(NAME_RESPONSE)))
        assertEquals(mockAsset.description, JSON.getAsString(assetResult.get(DESCRIPTION_RESPONSE)))
        assertEquals(mockAsset.decimals.toLong(), JSON.getAsByte(assetResult.get(DECIMALS_RESPONSE)).toLong())
        assertEquals("" + mockAsset.quantityQNT, JSON.getAsString(assetResult.get(QUANTITY_QNT_RESPONSE)))
        assertEquals("" + mockAsset.id, JSON.getAsString(assetResult.get(ASSET_RESPONSE)))
        assertEquals(1, JSON.getAsInt(assetResult.get(NUMBER_OF_ACCOUNTS_RESPONSE)).toLong())
        assertEquals(2, JSON.getAsInt(assetResult.get(NUMBER_OF_TRANSFERS_RESPONSE)).toLong())
        assertEquals(3, JSON.getAsInt(assetResult.get(NUMBER_OF_TRADES_RESPONSE)).toLong())
    }

}
