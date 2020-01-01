package brs.api.http

import brs.entity.Account
import brs.entity.Order.Ask
import brs.services.AssetExchangeService
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.ASSET_PARAMETER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.ResultFields.ASK_ORDERS_RESPONSE
import brs.services.ParameterService
import com.google.gson.JsonArray
import brs.util.jetty.get
import com.google.gson.JsonObject
import io.mockk.mockk
import io.mockk.every
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAccountCurrentAskOrdersTest : AbstractUnitTest() {
    private lateinit var t: GetAccountCurrentAskOrders

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockAssetExchangeService: AssetExchangeService

    @Before
    fun setUp() {
        mockParameterService = mockk(relaxed = true)
        mockAssetExchangeService = mockk(relaxed = true)

        t = GetAccountCurrentAskOrders(mockParameterService, mockAssetExchangeService)
    }

    @Test
    fun processRequest_getAskOrdersByAccount() {
        val accountId = 2L
        val firstIndex = 1
        val lastIndex = 2

        val request = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, accountId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex)
        )

        val mockAccount = mockk<Account>(relaxed = true)
        every { mockAccount.id } returns accountId
        every { mockParameterService.getAccount(eq(request)) } returns mockAccount

        val mockAsk = mockk<Ask>(relaxed = true)
        val mockAskId = 1L
        every { mockAsk.id } returns mockAskId

        val mockAskIterator = mockCollection(mockAsk)

        every { mockAssetExchangeService.getAskOrdersByAccount(eq(accountId), eq(firstIndex), eq(lastIndex)) } returns mockAskIterator

        val result = t.processRequest(request) as JsonObject

        assertNotNull(result)
        assertEquals(1, (result.get(ASK_ORDERS_RESPONSE) as JsonArray).size())
    }

    @Test
    fun processRequest_getAskOrdersByAccountAsset() {
        val assetId = 1L
        val accountId = 2L
        val firstIndex = 1
        val lastIndex = 2

        val request = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, accountId),
                MockParam(ASSET_PARAMETER, assetId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex)
        )

        val mockAccount = mockk<Account>(relaxed = true)
        every { mockAccount.id } returns accountId
        every { mockParameterService.getAccount(eq(request)) } returns mockAccount

        val mockAsk = mockk<Ask>(relaxed = true)
        val mockAskId = 1L
        every { mockAsk.id } returns mockAskId

        val mockAskIterator = mockCollection(mockAsk)

        every { mockAssetExchangeService.getAskOrdersByAccountAsset(eq(accountId), eq(assetId), eq(firstIndex), eq(lastIndex)) } returns mockAskIterator

        val result = t.processRequest(request) as JsonObject

        assertNotNull(result)
        assertEquals(1, (result.get(ASK_ORDERS_RESPONSE) as JsonArray).size())
    }
}
