package brs.api.http

import brs.entity.Asset
import brs.services.AssetExchangeService
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.ASSETS_RESPONSE
import brs.api.http.common.ResultFields.ASSET_RESPONSE
import brs.api.http.common.ResultFields.DECIMALS_RESPONSE
import brs.api.http.common.ResultFields.DESCRIPTION_RESPONSE
import brs.api.http.common.ResultFields.NAME_RESPONSE
import brs.api.http.common.ResultFields.NUMBER_OF_ACCOUNTS_RESPONSE
import brs.api.http.common.ResultFields.NUMBER_OF_TRADES_RESPONSE
import brs.api.http.common.ResultFields.NUMBER_OF_TRANSFERS_RESPONSE
import brs.api.http.common.ResultFields.QUANTITY_QNT_RESPONSE
import brs.util.json.safeGetAsLong
import brs.util.json.safeGetAsString
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

class GetAllAssetsTest : AbstractUnitTest() {

    private lateinit var t: GetAllAssets

    private lateinit var assetExchangeService: AssetExchangeService

    @Before
    fun setUp() {
        assetExchangeService = mock()

        t = GetAllAssets(assetExchangeService)
    }

    @Test
    fun processRequest() {
        val firstIndex = 1
        val lastIndex = 2

        val request = QuickMocker.httpServletRequest(
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex)
        )

        val mockAssetId: Long = 1

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).doReturn(1L)
        whenever(mockAsset.id).doReturn(mockAssetId)
        whenever(mockAsset.name).doReturn("name")
        whenever(mockAsset.description).doReturn("description")
        whenever(mockAsset.decimals).doReturn(1.toByte())
        whenever(mockAsset.quantity).doReturn(2L)

        val mockAssetIterator = mockCollection(mockAsset)

        whenever(assetExchangeService.getAllAssets(eq(firstIndex), eq(lastIndex))).doReturn(mockAssetIterator)
        whenever(assetExchangeService.getAssetAccountsCount(eq(mockAssetId))).doReturn(1)
        whenever(assetExchangeService.getTransferCount(eq(mockAssetId))).doReturn(2)
        whenever(assetExchangeService.getTradeCount(eq(mockAssetId))).doReturn(3)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val assetsResult = result.get(ASSETS_RESPONSE) as JsonArray
        assertNotNull(assetsResult)
        assertEquals(1, assetsResult.size().toLong())

        val assetResult = assetsResult.get(0) as JsonObject
        assertNotNull(assetResult)

        assertEquals(mockAsset.name, assetResult.get(NAME_RESPONSE).safeGetAsString())
        assertEquals(mockAsset.description, assetResult.get(DESCRIPTION_RESPONSE).safeGetAsString())
        assertEquals(mockAsset.decimals.toLong(), assetResult.get(DECIMALS_RESPONSE).safeGetAsLong())
        assertEquals(mockAsset.quantity.toString(), assetResult.get(QUANTITY_QNT_RESPONSE).safeGetAsString())
        assertEquals(mockAsset.id.toString(), assetResult.get(ASSET_RESPONSE).safeGetAsString())
        assertEquals(1L, assetResult.get(NUMBER_OF_ACCOUNTS_RESPONSE).safeGetAsLong())
        assertEquals(2L, assetResult.get(NUMBER_OF_TRANSFERS_RESPONSE).safeGetAsLong())
        assertEquals(3L, assetResult.get(NUMBER_OF_TRADES_RESPONSE).safeGetAsLong())
    }

}
