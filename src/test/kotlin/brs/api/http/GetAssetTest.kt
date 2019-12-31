package brs.api.http

import brs.api.http.common.Parameters.ASSET_PARAMETER
import brs.api.http.common.ResultFields.ASSET_RESPONSE
import brs.api.http.common.ResultFields.DECIMALS_RESPONSE
import brs.api.http.common.ResultFields.DESCRIPTION_RESPONSE
import brs.api.http.common.ResultFields.NAME_RESPONSE
import brs.api.http.common.ResultFields.NUMBER_OF_ACCOUNTS_RESPONSE
import brs.api.http.common.ResultFields.NUMBER_OF_TRADES_RESPONSE
import brs.api.http.common.ResultFields.NUMBER_OF_TRANSFERS_RESPONSE
import brs.api.http.common.ResultFields.QUANTITY_QNT_RESPONSE
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.entity.Asset
import brs.services.AssetExchangeService
import brs.services.ParameterService
import brs.util.json.safeGetAsLong
import brs.util.json.safeGetAsString
import brs.util.jetty.get
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAssetTest : AbstractUnitTest() {

    private lateinit var parameterServiceMock: ParameterService
    private lateinit var mockAssetExchangeService: AssetExchangeService

    private lateinit var t: GetAsset

    @Before
    fun setUp() {
        parameterServiceMock = mockk(relaxed = true)
        mockAssetExchangeService = mockk(relaxed = true)

        t = GetAsset(parameterServiceMock, mockAssetExchangeService)
    }

    @Test
    fun processRequest() {
        val assetId: Long = 4

        val request = QuickMocker.httpServletRequest(
                MockParam(ASSET_PARAMETER, assetId)
        )

        val asset = mockk<Asset>(relaxed = true)
        every { asset.id } returns assetId
        every { asset.accountId } returns 1L
        every { asset.name } returns "assetName"
        every { asset.description } returns "assetDescription"
        every { asset.decimals } returns 3

        every { parameterServiceMock.getAsset(eq(request)) } returns asset

        val tradeCount = 1
        val transferCount = 2
        val assetAccountsCount = 3

        every { mockAssetExchangeService.getTradeCount(eq(assetId)) } returns tradeCount
        every { mockAssetExchangeService.getTransferCount(eq(assetId)) } returns transferCount
        every { mockAssetExchangeService.getAssetAccountsCount(eq(assetId)) } returns assetAccountsCount

        val result = t.processRequest(request) as JsonObject

        assertNotNull(result)
        assertEquals(asset.name, result.get(NAME_RESPONSE).safeGetAsString())
        assertEquals(asset.description, result.get(DESCRIPTION_RESPONSE).safeGetAsString())
        assertEquals(asset.decimals.toLong(), result.get(DECIMALS_RESPONSE).safeGetAsLong())
        assertEquals(asset.quantity.toString(), result.get(QUANTITY_QNT_RESPONSE).safeGetAsString())
        assertEquals(asset.id.toString(), result.get(ASSET_RESPONSE).safeGetAsString())
        assertEquals(tradeCount.toLong(), result.get(NUMBER_OF_TRADES_RESPONSE).safeGetAsLong())
        assertEquals(transferCount.toLong(), result.get(NUMBER_OF_TRANSFERS_RESPONSE).safeGetAsLong())
        assertEquals(assetAccountsCount.toLong(), result.get(NUMBER_OF_ACCOUNTS_RESPONSE).safeGetAsLong())
    }
}