package brs.http

import brs.Alias
import brs.Alias.Offer
import brs.common.QuickMocker
import brs.http.common.ResultFields.ALIAS_NAME_RESPONSE
import brs.http.common.ResultFields.BUYER_RESPONSE
import brs.http.common.ResultFields.PRICE_NQT_RESPONSE
import brs.services.AliasService
import brs.services.ParameterService
import brs.util.JSON
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

class GetAliasTest {

    private lateinit var t: GetAlias

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockAliasService: AliasService

    @Before
    fun setUp() {
        mockParameterService = mock()
        mockAliasService = mock()

        t = GetAlias(mockParameterService, mockAliasService)
    }

    @Test
    fun processRequest() = runBlocking {
        val mockAlias = mock<Alias>()
        whenever(mockAlias.aliasName).doReturn("mockAliasName")

        val mockOffer = mock<Offer>()
        whenever(mockOffer.priceNQT).doReturn(123L)
        whenever(mockOffer.buyerId).doReturn(345L)

        val request = QuickMocker.httpServletRequest()

        whenever(mockParameterService.getAlias(eq(request))).doReturn(mockAlias)
        whenever(mockAliasService.getOffer(eq(mockAlias))).doReturn(mockOffer)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)
        assertEquals(mockAlias.aliasName, JSON.getAsString(result.get(ALIAS_NAME_RESPONSE)))
        assertEquals("" + mockOffer.priceNQT, JSON.getAsString(result.get(PRICE_NQT_RESPONSE)))
        assertEquals("" + mockOffer.buyerId, JSON.getAsString(result.get(BUYER_RESPONSE)))
    }

}
