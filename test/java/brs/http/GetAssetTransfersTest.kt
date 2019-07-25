package brs.http

import brs.Account
import brs.Asset
import brs.AssetTransfer
import brs.BurstException
import brs.assetexchange.AssetExchange
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.services.AccountService
import brs.services.ParameterService
import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.*
import brs.http.common.ResultFields.*
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAssetTransfersTest : AbstractUnitTest() {

    private var t: GetAssetTransfers? = null

    private var mockParameterService: ParameterService? = null
    private var mockAccountService: AccountService? = null
    private var mockAssetExchange: AssetExchange? = null

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        mockAccountService = mock<AccountService>()
        mockAssetExchange = mock<AssetExchange>()

        t = GetAssetTransfers(mockParameterService!!, mockAccountService!!, mockAssetExchange!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_byAsset() {
        val assetId = 123L
        val firstIndex = 0
        val lastIndex = 1
        val includeAssetInfo = true

        val req = QuickMocker.httpServletRequest(
                MockParam(ASSET_PARAMETER, assetId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex),
                MockParam(INCLUDE_ASSET_INFO_PARAMETER, includeAssetInfo)
        )

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).thenReturn(assetId)

        val mockAssetTransfer = mock<AssetTransfer>()
        val mockAssetTransferIterator = mockCollection<AssetTransfer>(mockAssetTransfer)

        whenever(mockParameterService!!.getAsset(eq<HttpServletRequest>(req))).thenReturn(mockAsset)

        whenever(mockAssetExchange!!.getAssetTransfers(eq(assetId), eq(firstIndex), eq(lastIndex))).thenReturn(mockAssetTransferIterator)

        val result = t!!.processRequest(req) as JsonObject
        assertNotNull(result)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_byAccount() {
        val accountId = 234L
        val firstIndex = 0
        val lastIndex = 1
        val includeAssetInfo = true

        val req = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, accountId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex),
                MockParam(INCLUDE_ASSET_INFO_PARAMETER, includeAssetInfo)
        )

        val mockAccount = mock<Account>()
        whenever(mockAccount.getId()).thenReturn(accountId)

        val mockAssetTransfer = mock<AssetTransfer>()
        val mockAssetTransferIterator = mockCollection<AssetTransfer>(mockAssetTransfer)

        whenever(mockParameterService!!.getAccount(eq<HttpServletRequest>(req))).thenReturn(mockAccount)

        whenever(mockAccountService!!.getAssetTransfers(eq(accountId), eq(firstIndex), eq(lastIndex))).thenReturn(mockAssetTransferIterator)

        val result = t!!.processRequest(req) as JsonObject
        assertNotNull(result)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_byAccountAndAsset() {
        val assetId = 123L
        val accountId = 234L
        val firstIndex = 0
        val lastIndex = 1
        val includeAssetInfo = true

        val req = QuickMocker.httpServletRequest(
                MockParam(ASSET_PARAMETER, assetId),
                MockParam(ACCOUNT_PARAMETER, accountId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex),
                MockParam(INCLUDE_ASSET_INFO_PARAMETER, includeAssetInfo)
        )

        val mockAsset = mock<Asset>()
        whenever(mockAsset.id).thenReturn(assetId)
        whenever(mockAsset.name).thenReturn("assetName")

        val mockAccount = mock<Account>()
        whenever(mockAccount.getId()).thenReturn(accountId)

        val mockAssetTransfer = mock<AssetTransfer>()
        whenever(mockAssetTransfer.assetId).thenReturn(assetId)
        val mockAssetTransferIterator = mockCollection<AssetTransfer>(mockAssetTransfer)

        whenever(mockParameterService!!.getAsset(eq<HttpServletRequest>(req))).thenReturn(mockAsset)
        whenever(mockParameterService!!.getAccount(eq<HttpServletRequest>(req))).thenReturn(mockAccount)

        whenever(mockAssetExchange!!.getAsset(eq(mockAssetTransfer.assetId))).thenReturn(mockAsset)

        whenever(mockAssetExchange!!.getAccountAssetTransfers(eq(accountId), eq(assetId), eq(firstIndex), eq(lastIndex))).thenReturn(mockAssetTransferIterator)

        val result = t!!.processRequest(req) as JsonObject
        assertNotNull(result)

        val resultList = result.get(TRANSFERS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size().toLong())

        val transferInfoResult = resultList.get(0) as JsonObject
        assertEquals("" + assetId, JSON.getAsString(transferInfoResult.get(ASSET_RESPONSE)))
        assertEquals(mockAsset.name, JSON.getAsString(transferInfoResult.get(NAME_RESPONSE)))
    }

}
