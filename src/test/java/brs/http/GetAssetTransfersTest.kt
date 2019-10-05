package brs.http

import brs.Account
import brs.Asset
import brs.AssetTransfer
import brs.assetexchange.AssetExchange
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.ASSET_PARAMETER
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.INCLUDE_ASSET_INFO_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.ResultFields.ASSET_RESPONSE
import brs.http.common.ResultFields.NAME_RESPONSE
import brs.http.common.ResultFields.TRANSFERS_RESPONSE
import brs.services.AccountService
import brs.services.ParameterService
import brs.util.JSON
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
import javax.servlet.http.HttpServletRequest

class GetAssetTransfersTest : AbstractUnitTest() {

    private lateinit var t: GetAssetTransfers

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockAccountService: AccountService
    private lateinit var mockAssetExchange: AssetExchange

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        mockAccountService = mock<AccountService>()
        mockAssetExchange = mock<AssetExchange>()

        t = GetAssetTransfers(mockParameterService!!, mockAccountService!!, mockAssetExchange!!)
    }

    @Test
    fun processRequest_byAsset() = runBlocking {
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

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).doReturn(assetId)

        val mockAssetTransfer = mock<AssetTransfer>()
        val mockAssetTransferIterator = mockCollection<AssetTransfer>(mockAssetTransfer)

        whenever(mockParameterService!!.getAsset(eq<HttpServletRequest>(request))).doReturn(mockAsset)

        whenever(mockAssetExchange!!.getAssetTransfers(eq(assetId), eq(firstIndex), eq(lastIndex))).doReturn(mockAssetTransferIterator)

        val result = t!!.processRequest(request) as JsonObject
        assertNotNull(result)
    }

    @Test
    fun processRequest_byAccount() = runBlocking {
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

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(accountId)

        val mockAssetTransfer = mock<AssetTransfer>()
        val mockAssetTransferIterator = mockCollection<AssetTransfer>(mockAssetTransfer)

        whenever(mockParameterService!!.getAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)

        whenever(mockAccountService!!.getAssetTransfers(eq(accountId), eq(firstIndex), eq(lastIndex))).doReturn(mockAssetTransferIterator)

        val result = t!!.processRequest(request) as JsonObject
        assertNotNull(result)
    }

    @Test
    fun processRequest_byAccountAndAsset() = runBlocking {
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

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).doReturn(assetId)
        whenever(mockAsset.name).doReturn("assetName")

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(accountId)

        val mockAssetTransfer = mock<AssetTransfer>()
        whenever(mockAssetTransfer.assetId).doReturn(assetId)
        val mockAssetTransferIterator = mockCollection<AssetTransfer>(mockAssetTransfer)

        whenever(mockParameterService!!.getAsset(eq<HttpServletRequest>(request))).doReturn(mockAsset)
        whenever(mockParameterService!!.getAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)

        whenever(mockAssetExchange!!.getAsset(eq(mockAssetTransfer.assetId))).doReturn(mockAsset)

        whenever(mockAssetExchange!!.getAccountAssetTransfers(eq(accountId), eq(assetId), eq(firstIndex), eq(lastIndex))).doReturn(mockAssetTransferIterator)

        val result = t!!.processRequest(request) as JsonObject
        assertNotNull(result)

        val resultList = result.get(TRANSFERS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size().toLong())

        val transferInfoResult = resultList.get(0) as JsonObject
        assertEquals("" + assetId, JSON.getAsString(transferInfoResult.get(ASSET_RESPONSE)))
        assertEquals(mockAsset.name, JSON.getAsString(transferInfoResult.get(NAME_RESPONSE)))
    }

}
