package brs.http

import brs.Alias
import brs.Alias.Offer
import brs.common.QuickMocker
import brs.services.AliasService
import brs.services.ParameterService
import brs.util.JSON
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

class GetAliasTest {

    private var t: GetAlias? = null

    private var mockParameterService: ParameterService? = null
    private var mockAliasService: AliasService? = null

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        mockAliasService = mock<AliasService>()

        t = GetAlias(mockParameterService!!, mockAliasService!!)
    }

    @Test
    fun processRequest() {
        val mockAlias = mock<Alias>()
        whenever(mockAlias.aliasName).doReturn("mockAliasName")

        val mockOffer = mock<Offer>()
        whenever(mockOffer.priceNQT).doReturn(123L)
        whenever(mockOffer.buyerId).doReturn(345L)

        val request = QuickMocker.httpServletRequest()

        whenever(mockParameterService!!.getAlias(eq<HttpServletRequest>(request))).doReturn(mockAlias)
        whenever(mockAliasService!!.getOffer(eq(mockAlias))).doReturn(mockOffer)

        val result = t!!.processRequest(request) as JsonObject
        assertNotNull(result)
        assertEquals(mockAlias.aliasName, JSON.getAsString(result.get(ALIAS_NAME_RESPONSE)))
        assertEquals("" + mockOffer.priceNQT, JSON.getAsString(result.get(PRICE_NQT_RESPONSE)))
        assertEquals("" + mockOffer.buyerId, JSON.getAsString(result.get(BUYER_RESPONSE)))
    }

}
