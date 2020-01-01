package brs.api.http

import brs.api.http.common.ResultFields.ALIASES_RESPONSE
import brs.api.http.common.ResultFields.ALIAS_RESPONSE
import brs.api.http.common.ResultFields.PRICE_PLANCK_RESPONSE
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.entity.Account
import brs.entity.Alias
import brs.entity.Alias.Offer
import brs.services.AliasService
import brs.services.ParameterService
import brs.util.json.getMemberAsString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAliasesTest : AbstractUnitTest() {

    private lateinit var t: GetAliases

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockAliasService: AliasService

    @Before
    fun setUp() {
        mockParameterService = mockk(relaxed = true)
        mockAliasService = mockk(relaxed = true)

        t = GetAliases(mockParameterService, mockAliasService)
    }

    @Test
    fun processRequest() {
        val accountId = 123L
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mockk<Account>(relaxed = true)
        every { mockAccount.id } returns accountId

        val mockAlias = mockk<Alias>(relaxed = true)
        every { mockAlias.id } returns 567L

        val mockOffer = mockk<Offer>(relaxed = true)
        every { mockOffer.pricePlanck } returns 234L

        val mockAliasIterator = mockCollection(mockAlias)

        every { mockParameterService.getAccount(eq(request)) } returns mockAccount

        every { mockAliasService.getAliasesByOwner(eq(accountId), eq(0), eq(-1)) } returns mockAliasIterator
        every { mockAliasService.getOffer(eq(mockAlias)) } returns mockOffer

        val resultOverview = t.processRequest(request) as JsonObject
        assertNotNull(resultOverview)

        val resultList = resultOverview.get(ALIASES_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size().toLong())

        val result = resultList.get(0) as JsonObject
        assertNotNull(result)
        assertEquals(mockAlias.id.toString(), result.getMemberAsString(ALIAS_RESPONSE))
        assertEquals(mockOffer.pricePlanck.toString(), result.getMemberAsString(PRICE_PLANCK_RESPONSE))
    }

}
