package brs.http

import brs.Account
import brs.Account.AccountAsset
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.http.common.ResultFields.ASSET_BALANCES_RESPONSE
import brs.http.common.ResultFields.ASSET_RESPONSE
import brs.http.common.ResultFields.BALANCE_QUANTITY_RESPONSE
import brs.http.common.ResultFields.DESCRIPTION_RESPONSE
import brs.http.common.ResultFields.NAME_RESPONSE
import brs.http.common.ResultFields.PUBLIC_KEY_RESPONSE
import brs.http.common.ResultFields.UNCONFIRMED_ASSET_BALANCES_RESPONSE
import brs.http.common.ResultFields.UNCONFIRMED_BALANCE_QUANTITY_RESPONSE
import brs.services.AccountService
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

class GetAccountTest : AbstractUnitTest() {

    private lateinit var t: GetAccount

    private lateinit var parameterServiceMock: ParameterService
    private lateinit var accountServiceMock: AccountService

    @Before
    fun setUp() {
        parameterServiceMock = mock()
        accountServiceMock = mock()

        t = GetAccount(parameterServiceMock, accountServiceMock)
    }

    @Test
    fun processRequest() {
        val mockAccountId = 123L
        val mockAccountName = "accountName"
        val mockAccountDescription = "accountDescription"

        val mockAssetId = 321L
        val balancePlanck = 23L
        val mockUnconfirmedQuantityPlanck = 12L

        val request = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(mockAccountId)
        whenever(mockAccount.publicKey).doReturn(byteArrayOf(1.toByte()))
        whenever(mockAccount.name).doReturn(mockAccountName)
        whenever(mockAccount.description).doReturn(mockAccountDescription)

        whenever(parameterServiceMock.getAccount(eq(request))).doReturn(mockAccount)

        val mockAccountAsset = mock<AccountAsset>()
        whenever(mockAccountAsset.assetId).doReturn(mockAssetId)
        whenever(mockAccountAsset.unconfirmedQuantity).doReturn(mockUnconfirmedQuantityPlanck)
        whenever(mockAccountAsset.quantity).doReturn(balancePlanck)
        val mockAssetOverview = mockCollection(mockAccountAsset)
        whenever(accountServiceMock.getAssets(eq(mockAccountId), eq(0), eq(-1))).doReturn(mockAssetOverview)

        val response = t.processRequest(request) as JsonObject
        assertEquals("01", response.get(PUBLIC_KEY_RESPONSE).safeGetAsString())
        assertEquals(mockAccountName, response.get(NAME_RESPONSE).safeGetAsString())
        assertEquals(mockAccountDescription, response.get(DESCRIPTION_RESPONSE).safeGetAsString())

        val confirmedBalanceResponses = response.get(ASSET_BALANCES_RESPONSE) as JsonArray
        assertNotNull(confirmedBalanceResponses)
        assertEquals(1, confirmedBalanceResponses.size().toLong())
        val balanceResponse = confirmedBalanceResponses.get(0) as JsonObject
        assertEquals(mockAssetId.toString(), balanceResponse.get(ASSET_RESPONSE).safeGetAsString())
        assertEquals(balancePlanck.toString(), balanceResponse.get(BALANCE_QUANTITY_RESPONSE).safeGetAsString())

        val unconfirmedBalanceResponses = response.get(UNCONFIRMED_ASSET_BALANCES_RESPONSE) as JsonArray
        assertNotNull(unconfirmedBalanceResponses)
        assertEquals(1, unconfirmedBalanceResponses.size().toLong())
        val unconfirmedBalanceResponse = unconfirmedBalanceResponses.get(0) as JsonObject
        assertEquals(mockAssetId.toString(), unconfirmedBalanceResponse.get(ASSET_RESPONSE).safeGetAsString())
        assertEquals(mockUnconfirmedQuantityPlanck.toString(), unconfirmedBalanceResponse.get(UNCONFIRMED_BALANCE_QUANTITY_RESPONSE).safeGetAsString())
    }
}
