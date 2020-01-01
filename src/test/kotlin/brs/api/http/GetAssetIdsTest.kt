package brs.api.http

import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.ASSET_IDS_RESPONSE
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.entity.Asset
import brs.services.AssetExchangeService
import brs.util.json.getElementAsString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAssetIdsTest : AbstractUnitTest() {

    private lateinit var t: GetAssetIds

    private lateinit var mockAssetExchangeService: AssetExchangeService

    @Before
    fun setUp() {
        mockAssetExchangeService = mockk(relaxed = true)

        t = GetAssetIds(mockAssetExchangeService)
    }

    @Test
    fun processRequest() {
        val firstIndex = 1
        val lastIndex = 2

        val mockAsset = mockk<Asset>(relaxed = true)
        every { mockAsset.id } returns 5L

        val mockAssetIterator = mockCollection(mockAsset)

        every { mockAssetExchangeService.getAllAssets(eq(firstIndex), eq(lastIndex)) } returns mockAssetIterator

        val request = QuickMocker.httpServletRequest(
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex)
        )

        val result = t.processRequest(request) as JsonObject

        assertNotNull(result)

        val resultAssetIds = result.get(ASSET_IDS_RESPONSE) as JsonArray
        assertNotNull(resultAssetIds)
        assertEquals(1, resultAssetIds.size().toLong())

        val resultAssetId = resultAssetIds.getElementAsString(0)
        assertEquals("5", resultAssetId)
    }

}
