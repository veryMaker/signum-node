package brs.http

import brs.Asset
import brs.BurstException
import brs.assetexchange.AssetExchange
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.services.ParameterService
import brs.util.JSON
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.ASSET_PARAMETER
import brs.http.common.ResultFields.*
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class GetAssetTest : AbstractUnitTest() {

    private var parameterServiceMock: ParameterService? = null
    private var mockAssetExchange: AssetExchange? = null

    private var t: GetAsset? = null

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        mockAssetExchange = mock<AssetExchange>()

        t = GetAsset(parameterServiceMock!!, mockAssetExchange!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val assetId: Long = 4

        val req = QuickMocker.httpServletRequest(
                MockParam(ASSET_PARAMETER, assetId)
        )

        val asset = mock<Asset>()
        whenever(asset.id).thenReturn(assetId)
        whenever(asset.name).thenReturn("assetName")
        whenever(asset.description).thenReturn("assetDescription")
        whenever(asset.decimals).thenReturn(3)

        whenever(parameterServiceMock!!.getAsset(eq<HttpServletRequest>(req))).thenReturn(asset)

        val tradeCount = 1
        val transferCount = 2
        val assetAccountsCount = 3

        whenever(mockAssetExchange!!.getTradeCount(eq(assetId))).thenReturn(tradeCount)
        whenever(mockAssetExchange!!.getTransferCount(eq(assetId))).thenReturn(transferCount)
        whenever(mockAssetExchange!!.getAssetAccountsCount(eq(assetId))).thenReturn(assetAccountsCount)

        val result = t!!.processRequest(req) as JsonObject

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