package brs.http

import brs.Account
import brs.Order.Ask
import brs.assetexchange.AssetExchange
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.ASSET_PARAMETER
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.ResultFields.ASK_ORDERS_RESPONSE
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test
import javax.servlet.http.HttpServletRequest

class GetAccountCurrentAskOrdersTest : AbstractUnitTest() {

    private lateinit var t: GetAccountCurrentAskOrders

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockAssetExchange: AssetExchange

    @Before
    fun setUp() {
        mockParameterService = mock()
        mockAssetExchange = mock()

        t = GetAccountCurrentAskOrders(mockParameterService, mockAssetExchange)
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

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(accountId)
        whenever(mockParameterService.getAccount(eq(request))).doReturn(mockAccount)

        val mockAsk = mock<Ask>()
        val mockAskId = 1L
        whenever(mockAsk.id).doReturn(mockAskId)

        val mockAskIterator = mockCollection(mockAsk)

        whenever(mockAssetExchange.getAskOrdersByAccount(eq(accountId), eq(firstIndex), eq(lastIndex))).doReturn(mockAskIterator)

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

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(accountId)
        whenever(mockParameterService.getAccount(eq(request))).doReturn(mockAccount)

        val mockAsk = mock<Ask>()
        val mockAskId = 1L
        whenever(mockAsk.id).doReturn(mockAskId)

        val mockAskIterator = mockCollection(mockAsk)

        whenever(mockAssetExchange.getAskOrdersByAccountAsset(eq(accountId), eq(assetId), eq(firstIndex), eq(lastIndex))).doReturn(mockAskIterator)

        val result = t.processRequest(request) as JsonObject

        assertNotNull(result)
        assertEquals(1, (result.get(ASK_ORDERS_RESPONSE) as JsonArray).size())
    }

}
