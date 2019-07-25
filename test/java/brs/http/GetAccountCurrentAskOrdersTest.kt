package brs.http

import brs.Account
import brs.BurstException
import brs.Order.Ask
import brs.assetexchange.AssetExchange
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.*
import brs.http.common.ResultFields.ASK_ORDERS_RESPONSE
import com.nhaarman.mockitokotlin2.eq
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAccountCurrentAskOrdersTest : AbstractUnitTest() {

    private var t: GetAccountCurrentAskOrders? = null

    private var mockParameterService: ParameterService? = null
    private var mockAssetExchange: AssetExchange? = null

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        mockAssetExchange = mock<AssetExchange>()

        t = GetAccountCurrentAskOrders(mockParameterService!!, mockAssetExchange!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_getAskOrdersByAccount() {
        val accountId = 2L
        val firstIndex = 1
        val lastIndex = 2

        val req = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, accountId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex)
        )

        val mockAccount = mock<Account>()
        whenever(mockAccount.getId()).thenReturn(accountId)
        whenever(mockParameterService!!.getAccount(eq<HttpServletRequest>(req))).thenReturn(mockAccount)

        val mockAsk = mock<Ask>()
        val mockAskId = 1L
        whenever(mockAsk.id).thenReturn(mockAskId)

        val mockAskIterator = mockCollection<Ask>(mockAsk)

        whenever(mockAssetExchange!!.getAskOrdersByAccount(eq(accountId), eq(firstIndex), eq(lastIndex))).thenReturn(mockAskIterator)

        val result = t!!.processRequest(req) as JsonObject

        assertNotNull(result)
        assertEquals(1, (result.get(ASK_ORDERS_RESPONSE) as JsonArray).size())
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_getAskOrdersByAccountAsset() {
        val assetId = 1L
        val accountId = 2L
        val firstIndex = 1
        val lastIndex = 2

        val req = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, accountId),
                MockParam(ASSET_PARAMETER, assetId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex)
        )

        val mockAccount = mock<Account>()
        whenever(mockAccount.getId()).thenReturn(accountId)
        whenever(mockParameterService!!.getAccount(eq<HttpServletRequest>(req))).thenReturn(mockAccount)

        val mockAsk = mock<Ask>()
        val mockAskId = 1L
        whenever(mockAsk.id).thenReturn(mockAskId)

        val mockAskIterator = mockCollection<Ask>(mockAsk)

        whenever(mockAssetExchange!!.getAskOrdersByAccountAsset(eq(accountId), eq(assetId), eq(firstIndex), eq(lastIndex))).thenReturn(mockAskIterator)

        val result = t!!.processRequest(req) as JsonObject

        assertNotNull(result)
        assertEquals(1, (result.get(ASK_ORDERS_RESPONSE) as JsonArray).size())
    }

}
