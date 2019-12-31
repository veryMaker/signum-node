package brs.api.http

import brs.entity.Alias
import brs.entity.Alias.Offer
import brs.common.QuickMocker
import brs.api.http.common.ResultFields.ALIAS_NAME_RESPONSE
import brs.api.http.common.ResultFields.BUYER_RESPONSE
import brs.api.http.common.ResultFields.PRICE_PLANCK_RESPONSE
import brs.services.AliasService
import brs.services.ParameterService
import brs.util.json.safeGetAsString
import brs.util.jetty.get
import com.google.gson.JsonObject
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAliasTest {

    private lateinit var t: GetAlias

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockAliasService: AliasService

    @Before
    fun setUp() {
        mockParameterService = mockk(relaxed = true)
        mockAliasService = mockk(relaxed = true)

        t = GetAlias(mockParameterService, mockAliasService)
    }

    @Test
    fun processRequest() {
        val mockAlias = mockk<Alias>(relaxed = true)
        every { mockAlias.aliasName } returns "mockAliasName"

        val mockOffer = mockk<Offer>(relaxed = true)
        every { mockOffer.pricePlanck } returns 123L
        every { mockOffer.buyerId } returns 345L

        val request = QuickMocker.httpServletRequest()

        every { mockParameterService.getAlias(eq(request)) } returns mockAlias
        every { mockAliasService.getOffer(eq(mockAlias)) } returns mockOffer

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)
        assertEquals(mockAlias.aliasName, result.get(ALIAS_NAME_RESPONSE).safeGetAsString())
        assertEquals(mockOffer.pricePlanck.toString(), result.get(PRICE_PLANCK_RESPONSE).safeGetAsString())
        assertEquals(mockOffer.buyerId.toString(), result.get(BUYER_RESPONSE).safeGetAsString())
    }

}
