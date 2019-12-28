package brs.api.http

import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.ASSET_PARAMETER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.INCLUDE_ASSET_INFO_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.ASSET_RESPONSE
import brs.api.http.common.ResultFields.NAME_RESPONSE
import brs.api.http.common.ResultFields.TRANSFERS_RESPONSE
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.entity.Account
import brs.entity.Asset
import brs.entity.AssetTransfer
import brs.services.AccountService
import brs.services.AssetExchangeService
import brs.services.ParameterService
import brs.util.json.mustGetAsString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAssetTransfersTest : AbstractUnitTest() {

    private lateinit var t: GetAssetTransfers

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockAccountService: AccountService
    private lateinit var mockAssetExchangeService: AssetExchangeService

    @Before
    fun setUp() {
        mockParameterService = mockk()
        mockAccountService = mockk()
        mockAssetExchangeService = mockk()
        every { mockAssetExchangeService.getAsset(any()) } returns null

        t = GetAssetTransfers(mockParameterService, mockAccountService, mockAssetExchangeService)
    }

    @Test
    fun processRequest_byAsset() {
        val assetId = 123L
        val firstIndex = 0
        val lastIndex = 1
        val includeAssetInfo = true

        val request = QuickMocker.httpServletRequest(
                MockParam(ASSET_PARAMETER, assetId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex),
                MockParam(INCLUDE_ASSET_INFO_PARAMETER, includeAssetInfo)
        )

        val mockAsset = mockk<Asset>()
        every { mockAsset.id } returns assetId

        val mockAssetTransfer = mockk<AssetTransfer>(relaxed = true)
        val mockAssetTransferIterator = mockCollection(mockAssetTransfer)

        every { mockParameterService.getAsset(eq(request)) } returns mockAsset

        every { mockAssetExchangeService.getAssetTransfers(eq(assetId), eq(firstIndex), eq(lastIndex)) } returns mockAssetTransferIterator

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)
    }

    @Test
    fun processRequest_byAccount() {
        val accountId = 234L
        val firstIndex = 0
        val lastIndex = 1
        val includeAssetInfo = true

        val request = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, accountId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex),
                MockParam(INCLUDE_ASSET_INFO_PARAMETER, includeAssetInfo)
        )

        val mockAccount = mockk<Account>()
        every { mockAccount.id } returns accountId

        val mockAssetTransfer = mockk<AssetTransfer>(relaxed = true)
        val mockAssetTransferIterator = mockCollection(mockAssetTransfer)

        every { mockParameterService.getAccount(eq(request)) } returns mockAccount

        every { mockAccountService.getAssetTransfers(eq(accountId), eq(firstIndex), eq(lastIndex)) } returns mockAssetTransferIterator

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)
    }

    @Test
    fun processRequest_byAccountAndAsset() {
        val assetId = 123L
        val accountId = 234L
        val firstIndex = 0
        val lastIndex = 1
        val includeAssetInfo = true

        val request = QuickMocker.httpServletRequest(
                MockParam(ASSET_PARAMETER, assetId),
                MockParam(ACCOUNT_PARAMETER, accountId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex),
                MockParam(INCLUDE_ASSET_INFO_PARAMETER, includeAssetInfo)
        )

        val mockAsset = mockk<Asset>(relaxed = true)
        every { mockAsset.id } returns assetId
        every { mockAsset.name } returns "assetName"

        val mockAccount = mockk<Account>()
        every { mockAccount.id } returns accountId

        val mockAssetTransfer = mockk<AssetTransfer>(relaxed = true)
        every { mockAssetTransfer.assetId } returns assetId
        val mockAssetTransferIterator = mockCollection(mockAssetTransfer)

        every { mockParameterService.getAsset(eq(request)) } returns mockAsset
        every { mockParameterService.getAccount(eq(request)) } returns mockAccount

        every { mockAssetExchangeService.getAsset(eq(assetId)) } returns mockAsset

        every { mockAssetExchangeService.getAccountAssetTransfers(eq(accountId), eq(assetId), eq(firstIndex), eq(lastIndex)) } returns mockAssetTransferIterator

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val resultList = result.get(TRANSFERS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size().toLong())

        val transferInfoResult = resultList.get(0) as JsonObject
        assertEquals(assetId.toString(), transferInfoResult.get(ASSET_RESPONSE).mustGetAsString(ASSET_RESPONSE))
        assertEquals(mockAsset.name, transferInfoResult.get(NAME_RESPONSE).mustGetAsString(NAME_RESPONSE))
    }
}
