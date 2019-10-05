package brs.http

import brs.Account
import brs.Alias
import brs.Alias.Offer
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.http.common.ResultFields.ALIASES_RESPONSE
import brs.http.common.ResultFields.ALIAS_RESPONSE
import brs.http.common.ResultFields.PRICE_NQT_RESPONSE
import brs.services.AliasService
import brs.services.ParameterService
import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import javax.servlet.http.HttpServletRequest

class GetAliasesTest : AbstractUnitTest() {

    private lateinit var t: GetAliases

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockAliasService: AliasService

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        mockAliasService = mock<AliasService>()

        t = GetAliases(mockParameterService!!, mockAliasService!!)
    }

    @Test
    fun processRequest() = runBlocking {
        val accountId = 123L
        val request = QuickMocker.httpServletRequest()

        val mockAccount = mock<Account>()
        whenever(mockAccount.id).doReturn(accountId)

        val mockAlias = mock<Alias>()
        whenever(mockAlias.id).doReturn(567L)

        val mockOffer = mock<Offer>()
        whenever(mockOffer.priceNQT).doReturn(234L)

        val mockAliasIterator = mockCollection<Alias>(mockAlias)

        whenever(mockParameterService!!.getAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)

        whenever(mockAliasService!!.getAliasesByOwner(eq(accountId), eq(0), eq(-1))).doReturn(mockAliasIterator)
        whenever(mockAliasService!!.getOffer(eq(mockAlias))).doReturn(mockOffer)

        val resultOverview = t!!.processRequest(request) as JsonObject
        assertNotNull(resultOverview)

        val resultList = resultOverview.get(ALIASES_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size().toLong())

        val result = resultList.get(0) as JsonObject
        assertNotNull(result)
        assertEquals("" + mockAlias.id, JSON.getAsString(result.get(ALIAS_RESPONSE)))
        assertEquals("" + mockOffer.priceNQT, JSON.getAsString(result.get(PRICE_NQT_RESPONSE)))
    }

}
