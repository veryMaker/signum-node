package brs.http

import brs.Account
import brs.BurstException
import brs.Order.Ask
import brs.assetexchange.AssetExchange
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.services.ParameterService
import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.*
import brs.http.common.ResultFields.ASK_ORDER_IDS_RESPONSE
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAccountCurrentAskOrderIdsTest : AbstractUnitTest() {

    private var t: GetAccountCurrentAskOrderIds? = null

    private var mockParameterService: ParameterService? = null
    private var mockAssetExchange: AssetExchange? = null

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        mockAssetExchange = mock<AssetExchange>()

        t = GetAccountCurrentAskOrderIds(mockParameterService!!, mockAssetExchange!!)
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
        whenever(mockAccount.id).doReturn(accountId)
        whenever(mockParameterService!!.getAccount(eq<HttpServletRequest>(req))).doReturn(mockAccount)

        val mockAsk = mock<Ask>()
        whenever(mockAsk.id).doReturn(1L)

        val mockAskIterator = mockCollection<Ask>(mockAsk)

        whenever(mockAssetExchange!!.getAskOrdersByAccount(eq(accountId), eq(firstIndex), eq(lastIndex))).doReturn(mockAskIterator)

        val result = t!!.processRequest(req) as JsonObject

        assertNotNull(result)

        val resultList = result.get(ASK_ORDER_IDS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size())

        assertEquals("" + mockAsk.id, JSON.getAsString(resultList.get(0)))
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
        whenever(mockAccount.id).doReturn(accountId)
        whenever(mockParameterService!!.getAccount(eq<HttpServletRequest>(req))).doReturn(mockAccount)

        val mockAsk = mock<Ask>()
        whenever(mockAsk.id).doReturn(1L)

        val mockAskIterator = mockCollection<Ask>(mockAsk)

        whenever(mockAssetExchange!!.getAskOrdersByAccountAsset(eq(accountId), eq(assetId), eq(firstIndex), eq(lastIndex))).doReturn(mockAskIterator)

        val result = t!!.processRequest(req) as JsonObject

        assertNotNull(result)

        val resultList = result.get(ASK_ORDER_IDS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size())

        assertEquals("" + mockAsk.id, JSON.getAsString(resultList.get(0)))
    }

}
