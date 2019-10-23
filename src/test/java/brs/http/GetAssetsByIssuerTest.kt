package brs.http

import brs.Account
import brs.Asset
import brs.assetexchange.AssetExchange
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.ResultFields.ASSETS_RESPONSE
import brs.http.common.ResultFields.ASSET_RESPONSE
import brs.http.common.ResultFields.DECIMALS_RESPONSE
import brs.http.common.ResultFields.DESCRIPTION_RESPONSE
import brs.http.common.ResultFields.NAME_RESPONSE
import brs.http.common.ResultFields.NUMBER_OF_ACCOUNTS_RESPONSE
import brs.http.common.ResultFields.NUMBER_OF_TRADES_RESPONSE
import brs.http.common.ResultFields.NUMBER_OF_TRANSFERS_RESPONSE
import brs.http.common.ResultFields.QUANTITY_QNT_RESPONSE
import brs.services.ParameterService
import brs.util.safeGetAsLong
import brs.util.safeGetAsString
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

class GetAssetsByIssuerTest : AbstractUnitTest() {

    private lateinit var t: GetAssetsByIssuer

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockAssetExchange: AssetExchange

    @Before
    fun setUp() {
        mockParameterService = mock()
        mockAssetExchange = mock()

        t = GetAssetsByIssuer(mockParameterService, mockAssetExchange)
    }

    @Test
    fun processRequest() = runBlocking {
        val firstIndex = 1
        val lastIndex = 2

        val request = QuickMocker.httpServletRequest(
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex)
        )


        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(1L)
        whenever(mockParameterService.getAccounts(eq(request))).doReturn(listOf(mockAccount))

        val mockAssetId: Long = 1

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).doReturn(1L)
        whenever(mockAsset.id).doReturn(mockAssetId)
        whenever(mockAsset.name).doReturn("name")
        whenever(mockAsset.description).doReturn("description")
        whenever(mockAsset.decimals).doReturn(1.toByte())
        whenever(mockAsset.quantityQNT).doReturn(2L)

        val mockAssetIterator = mockCollection(mockAsset)

        whenever(mockAssetExchange.getAssetsIssuedBy(eq(mockAccount.id), eq(firstIndex), eq(lastIndex))).doReturn(mockAssetIterator)
        whenever(mockAssetExchange.getAssetAccountsCount(eq(mockAssetId))).doReturn(1)
        whenever(mockAssetExchange.getTransferCount(eq(mockAssetId))).doReturn(2)
        whenever(mockAssetExchange.getTradeCount(eq(mockAssetId))).doReturn(3)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val assetsForAccountsResult = result.get(ASSETS_RESPONSE) as JsonArray
        assertNotNull(assetsForAccountsResult)
        assertEquals(1, assetsForAccountsResult.size().toLong())

        val assetsForAccount1Result = assetsForAccountsResult.get(0) as JsonArray
        assertNotNull(assetsForAccount1Result)
        assertEquals(1, assetsForAccount1Result.size().toLong())

        val assetResult = assetsForAccount1Result.get(0) as JsonObject
        assertNotNull(assetResult)

        assertEquals(mockAsset.name, assetResult.get(NAME_RESPONSE).safeGetAsString())
        assertEquals(mockAsset.description, assetResult.get(DESCRIPTION_RESPONSE).safeGetAsString())
        assertEquals(mockAsset.decimals.toLong(), assetResult.get(DECIMALS_RESPONSE).safeGetAsLong())
        assertEquals("" + mockAsset.quantityQNT, assetResult.get(QUANTITY_QNT_RESPONSE).safeGetAsString())
        assertEquals("" + mockAsset.id, assetResult.get(ASSET_RESPONSE).safeGetAsString())
        assertEquals(1L, assetResult.get(NUMBER_OF_ACCOUNTS_RESPONSE).safeGetAsLong())
        assertEquals(2L, assetResult.get(NUMBER_OF_TRANSFERS_RESPONSE).safeGetAsLong())
        assertEquals(3L, assetResult.get(NUMBER_OF_TRADES_RESPONSE).safeGetAsLong())
    }

}
