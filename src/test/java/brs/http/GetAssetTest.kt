package brs.http

import brs.Asset
import brs.assetexchange.AssetExchange
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.http.common.Parameters.ASSET_PARAMETER
import brs.http.common.ResultFields.ASSET_RESPONSE
import brs.http.common.ResultFields.DECIMALS_RESPONSE
import brs.http.common.ResultFields.DESCRIPTION_RESPONSE
import brs.http.common.ResultFields.NAME_RESPONSE
import brs.http.common.ResultFields.NUMBER_OF_ACCOUNTS_RESPONSE
import brs.http.common.ResultFields.NUMBER_OF_TRADES_RESPONSE
import brs.http.common.ResultFields.NUMBER_OF_TRANSFERS_RESPONSE
import brs.http.common.ResultFields.QUANTITY_QNT_RESPONSE
import brs.services.ParameterService
import brs.util.JSON
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
import javax.servlet.http.HttpServletRequest

class GetAssetTest : AbstractUnitTest() {

    private lateinit var parameterServiceMock: ParameterService
    private lateinit var mockAssetExchange: AssetExchange

    private lateinit var t: GetAsset

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        mockAssetExchange = mock<AssetExchange>()

        t = GetAsset(parameterServiceMock!!, mockAssetExchange!!)
    }

    @Test
    fun processRequest() = runBlocking {
        val assetId: Long = 4

        val request = QuickMocker.httpServletRequest(
                MockParam(ASSET_PARAMETER, assetId)
        )

        val asset = mock<Asset>()
        whenever(asset.id).doReturn(assetId)
        whenever(asset.name).doReturn("assetName")
        whenever(asset.description).doReturn("assetDescription")
        whenever(asset.decimals).doReturn(3)

        whenever(parameterServiceMock!!.getAsset(eq<HttpServletRequest>(request))).doReturn(asset)

        val tradeCount = 1
        val transferCount = 2
        val assetAccountsCount = 3

        whenever(mockAssetExchange!!.getTradeCount(eq(assetId))).doReturn(tradeCount)
        whenever(mockAssetExchange!!.getTransferCount(eq(assetId))).doReturn(transferCount)
        whenever(mockAssetExchange!!.getAssetAccountsCount(eq(assetId))).doReturn(assetAccountsCount)

        val result = t!!.processRequest(request) as JsonObject

        assertNotNull(result)
        assertEquals(asset.name, JSON.getAsString(result.get(NAME_RESPONSE)))
        assertEquals(asset.description, JSON.getAsString(result.get(DESCRIPTION_RESPONSE)))
        assertEquals(asset.decimals.toLong(), JSON.getAsInt(result.get(DECIMALS_RESPONSE)).toLong())
        assertEquals("" + asset.quantityQNT, JSON.getAsString(result.get(QUANTITY_QNT_RESPONSE)))
        assertEquals("" + asset.id, JSON.getAsString(result.get(ASSET_RESPONSE)))
        assertEquals(tradeCount.toLong(), JSON.getAsInt(result.get(NUMBER_OF_TRADES_RESPONSE)).toLong())
        assertEquals(transferCount.toLong(), JSON.getAsInt(result.get(NUMBER_OF_TRANSFERS_RESPONSE)).toLong())
        assertEquals(assetAccountsCount.toLong(), JSON.getAsInt(result.get(NUMBER_OF_ACCOUNTS_RESPONSE)).toLong())
    }
}