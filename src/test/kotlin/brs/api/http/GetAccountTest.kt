package brs.api.http

import brs.entity.Account
import brs.entity.Account.AccountAsset
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.api.http.common.ResultFields.ASSET_BALANCES_RESPONSE
import brs.api.http.common.ResultFields.ASSET_RESPONSE
import brs.api.http.common.ResultFields.BALANCE_QUANTITY_RESPONSE
import brs.api.http.common.ResultFields.DESCRIPTION_RESPONSE
import brs.api.http.common.ResultFields.NAME_RESPONSE
import brs.api.http.common.ResultFields.PUBLIC_KEY_RESPONSE
import brs.api.http.common.ResultFields.UNCONFIRMED_ASSET_BALANCES_RESPONSE
import brs.api.http.common.ResultFields.UNCONFIRMED_BALANCE_QUANTITY_RESPONSE
import brs.services.AccountService
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

class GetAccountTest : AbstractUnitTest() {

    private lateinit var t: GetAccount

    private lateinit var parameterServiceMock: ParameterService
    private lateinit var accountServiceMock: AccountService

    @Before
    fun setUp() {
        parameterServiceMock = mockk()
        accountServiceMock = mockk()

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

        val mockAccount = mockk<Account>()
        every { mockAccount.id } returns mockAccountId
        every { mockAccount.publicKey } returns byteArrayOf(1.toByte())
        every { mockAccount.name } returns mockAccountName
        every { mockAccount.description } returns mockAccountDescription

        every { parameterServiceMock.getAccount(eq(request)) } returns mockAccount

        val mockAccountAsset = mockk<AccountAsset>()
        every { mockAccountAsset.assetId } returns mockAssetId
        every { mockAccountAsset.unconfirmedQuantity } returns mockUnconfirmedQuantityPlanck
        every { mockAccountAsset.quantity } returns balancePlanck
        val mockAssetOverview = mockCollection(mockAccountAsset)
        every { accountServiceMock.getAssets(eq(mockAccountId), eq(0), eq(-1)) } returns mockAssetOverview

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
