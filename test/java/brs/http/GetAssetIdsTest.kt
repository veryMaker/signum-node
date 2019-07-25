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
import brs.http.common.ResultFields.ASSET_IDS_RESPONSE
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAssetIdsTest : AbstractUnitTest() {

    private var t: GetAssetIds? = null

    private var mockAssetExchange: AssetExchange? = null

    @Before
    fun setUp() {
        mockAssetExchange = mock<AssetExchange>()

        t = GetAssetIds(mockAssetExchange!!)
    }

    @Test
    fun processRequest() {
        val firstIndex = 1
        val lastIndex = 2

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).thenReturn(5L)

        val mockAssetIterator = mockCollection<Asset>(mockAsset)

        whenever(mockAssetExchange!!.getAllAssets(eq(firstIndex), eq(lastIndex)))
                .thenReturn(mockAssetIterator)

        val req = QuickMocker.httpServletRequest(
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex)
        )

        val result = t!!.processRequest(req) as JsonObject

        assertNotNull(result)

        val resultAssetIds = result.get(ASSET_IDS_RESPONSE) as JsonArray
        assertNotNull(resultAssetIds)
        assertEquals(1, resultAssetIds.size().toLong())

        val resultAssetId = JSON.getAsString(resultAssetIds.get(0))
        assertEquals("5", resultAssetId)
    }

}
