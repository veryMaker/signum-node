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
import brs.api.http.common.ResultFields.BID_ORDERS_RESPONSE
import brs.api.http.common.ResultFields.ORDER_RESPONSE
import brs.services.ParameterService
import brs.util.json.safeGetAsString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAccountCurrentBidOrdersTest : AbstractUnitTest() {

    private lateinit var t: GetAccountCurrentBidOrders

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockAssetExchangeService: AssetExchangeService

    @Before
    fun setUp() {
        mockParameterService = mockk()
        mockAssetExchangeService = mockk()

        t = GetAccountCurrentBidOrders(mockParameterService, mockAssetExchangeService)
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

        val mockAccount = mockk<Account>()
        every { mockAccount.id } returns accountId

        val mockBidId = 456L
        val bid = mockk<Bid>()
        every { bid.id } returns mockBidId

        val mockBidIterator = mockCollection(bid)

        every { mockParameterService.getAccount(eq(request)) } returns mockAccount
        every { mockAssetExchangeService.getBidOrdersByAccount(eq(accountId), eq(firstIndex), eq(lastIndex)) } returns mockBidIterator

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val resultList = result.get(BID_ORDERS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size().toLong())

        val resultBid = resultList.get(0) as JsonObject
        assertNotNull(resultBid)
        assertEquals(mockBidId.toString(), resultBid.get(ORDER_RESPONSE).safeGetAsString())
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

        val mockAccount = mockk<Account>()
        every { mockAccount.id } returns accountId

        val mockBidId = 456L
        val bid = mockk<Bid>()
        every { bid.id } returns mockBidId

        val mockBidIterator = mockCollection(bid)

        every { mockParameterService.getAccount(eq(request)) } returns mockAccount
        every { mockAssetExchangeService.getBidOrdersByAccountAsset(eq(accountId), eq(assetId), eq(firstIndex), eq(lastIndex)) } returns mockBidIterator

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val resultList = result.get(BID_ORDERS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size().toLong())

        val resultBid = resultList.get(0) as JsonObject
        assertNotNull(resultBid)
        assertEquals(mockBidId.toString(), resultBid.get(ORDER_RESPONSE).safeGetAsString())
    }

}
