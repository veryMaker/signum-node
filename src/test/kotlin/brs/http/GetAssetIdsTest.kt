package brs.http

import brs.Asset
import brs.assetexchange.AssetExchange
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.ResultFields.ASSET_IDS_RESPONSE
import brs.util.safeGetAsString
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

class GetAssetIdsTest : AbstractUnitTest() {

    private lateinit var t: GetAssetIds

    private lateinit var mockAssetExchange: AssetExchange

    @Before
    fun setUp() {
        mockAssetExchange = mock()

        t = GetAssetIds(mockAssetExchange)
    }

    @Test
    fun processRequest() {
        val firstIndex = 1
        val lastIndex = 2

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).doReturn(5L)

        val mockAssetIterator = mockCollection(mockAsset)

        whenever(mockAssetExchange.getAllAssets(eq(firstIndex), eq(lastIndex)))
                .doReturn(mockAssetIterator)

        val request = QuickMocker.httpServletRequest(
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex)
        )

        val result = t.processRequest(request) as JsonObject

        assertNotNull(result)

        val resultAssetIds = result.get(ASSET_IDS_RESPONSE) as JsonArray
        assertNotNull(resultAssetIds)
        assertEquals(1, resultAssetIds.size().toLong())

        val resultAssetId = resultAssetIds.get(0).safeGetAsString()
        assertEquals("5", resultAssetId)
    }

}
