package brs.api.http

import brs.entity.Account
import brs.entity.Order.Bid
import brs.services.AssetExchangeService
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.ASSET_PARAMETER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.BID_ORDER_IDS_RESPONSE
import brs.services.ParameterService
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

class GetAccountCurrentBidOrderIdsTest : AbstractUnitTest() {

    private lateinit var t: GetAccountCurrentBidOrderIds

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockAssetExchangeService: AssetExchangeService

    @Before
    fun setUp() {
        mockParameterService = mock()
        mockAssetExchangeService = mock()

        t = GetAccountCurrentBidOrderIds(mockParameterService, mockAssetExchangeService)
    }

    @Test
    fun processRequest_byAccount() {
        val accountId = 123L
        val firstIndex = 0
        val lastIndex = 1

        val request = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, accountId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex)
        )

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(accountId)

        val mockBidId = 456L
        val bid = mock<Bid>()
        whenever(bid.id).doReturn(mockBidId)

        val mockBidIterator = mockCollection(bid)

        whenever(mockParameterService.getAccount(eq(request))).doReturn(mockAccount)
        whenever(mockAssetExchangeService.getBidOrdersByAccount(eq(accountId), eq(firstIndex), eq(lastIndex))).doReturn(mockBidIterator)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val resultList = result.get(BID_ORDER_IDS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size().toLong())
        assertEquals(mockBidId.toString(), resultList.get(0).safeGetAsString())
    }

    @Test
    fun processRequest_byAccountAsset() {
        val accountId = 123L
        val assetId = 234L
        val firstIndex = 0
        val lastIndex = 1

        val request = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, accountId),
                MockParam(ASSET_PARAMETER, assetId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex)
        )

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(accountId)

        val mockBidId = 456L
        val bid = mock<Bid>()
        whenever(bid.id).doReturn(mockBidId)

        val mockBidIterator = mockCollection(bid)

        whenever(mockParameterService.getAccount(eq(request))).doReturn(mockAccount)
        whenever(mockAssetExchangeService.getBidOrdersByAccountAsset(eq(accountId), eq(assetId), eq(firstIndex), eq(lastIndex))).doReturn(mockBidIterator)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val resultList = result.get(BID_ORDER_IDS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size().toLong())
        assertEquals(mockBidId.toString(), resultList.get(0).safeGetAsString())
    }

}
