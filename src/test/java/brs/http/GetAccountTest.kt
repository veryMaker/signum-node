package brs.http

import brs.Account
import brs.Account.AccountAsset
import brs.BurstException
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.http.common.ResultFields.ASSET_BALANCES_RESPONSE
import brs.http.common.ResultFields.ASSET_RESPONSE
import brs.http.common.ResultFields.BALANCE_QNT_RESPONSE
import brs.http.common.ResultFields.DESCRIPTION_RESPONSE
import brs.http.common.ResultFields.NAME_RESPONSE
import brs.http.common.ResultFields.PUBLIC_KEY_RESPONSE
import brs.http.common.ResultFields.UNCONFIRMED_ASSET_BALANCES_RESPONSE
import brs.http.common.ResultFields.UNCONFIRMED_BALANCE_QNT_RESPONSE
import brs.services.AccountService
import brs.services.ParameterService
import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAccountTest : AbstractUnitTest() {

    private var t: GetAccount? = null

    private var parameterServiceMock: ParameterService? = null
    private var accountServiceMock: AccountService? = null

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        accountServiceMock = mock<AccountService>()

        t = GetAccount(parameterServiceMock!!, accountServiceMock!!)
    }

    @Test
    fun processRequest() {
        val mockAccountId = 123L
        val mockAccountName = "accountName"
        val mockAccountDescription = "accountDescription"

        val mockAssetId = 321L
        val balanceNQT = 23L
        val mockUnconfirmedQuantityNQT = 12L

        val request = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(mockAccountId)
        whenever(mockAccount.publicKey).doReturn(byteArrayOf(1.toByte()))
        whenever(mockAccount.name).doReturn(mockAccountName)
        whenever(mockAccount.description).doReturn(mockAccountDescription)

        whenever(parameterServiceMock!!.getAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)

        val mockAccountAsset = mock<AccountAsset>()
        whenever(mockAccountAsset.assetId).doReturn(mockAssetId)
        whenever(mockAccountAsset.unconfirmedQuantityQNT).doReturn(mockUnconfirmedQuantityNQT)
        whenever(mockAccountAsset.quantityQNT).doReturn(balanceNQT)
        val mockAssetOverview = mockCollection<AccountAsset>(mockAccountAsset)
        whenever(accountServiceMock!!.getAssets(eq(mockAccountId), eq(0), eq(-1))).doReturn(mockAssetOverview)

        val response = t!!.processRequest(request) as JsonObject
        assertEquals("01", JSON.getAsString(response.get(PUBLIC_KEY_RESPONSE)))
        assertEquals(mockAccountName, JSON.getAsString(response.get(NAME_RESPONSE)))
        assertEquals(mockAccountDescription, JSON.getAsString(response.get(DESCRIPTION_RESPONSE)))

        val confirmedBalanceResponses = response.get(ASSET_BALANCES_RESPONSE) as JsonArray
        assertNotNull(confirmedBalanceResponses)
        assertEquals(1, confirmedBalanceResponses.size().toLong())
        val balanceResponse = confirmedBalanceResponses.get(0) as JsonObject
        assertEquals("" + mockAssetId, JSON.getAsString(balanceResponse.get(ASSET_RESPONSE)))
        assertEquals("" + balanceNQT, JSON.getAsString(balanceResponse.get(BALANCE_QNT_RESPONSE)))

        val unconfirmedBalanceResponses = response.get(UNCONFIRMED_ASSET_BALANCES_RESPONSE) as JsonArray
        assertNotNull(unconfirmedBalanceResponses)
        assertEquals(1, unconfirmedBalanceResponses.size().toLong())
        val unconfirmedBalanceResponse = unconfirmedBalanceResponses.get(0) as JsonObject
        assertEquals("" + mockAssetId, JSON.getAsString(unconfirmedBalanceResponse.get(ASSET_RESPONSE)))
        assertEquals("" + mockUnconfirmedQuantityNQT, JSON.getAsString(unconfirmedBalanceResponse.get(UNCONFIRMED_BALANCE_QNT_RESPONSE)))
    }
}
