package brs.api.http

import brs.objects.Constants.FEE_QUANT
import brs.common.QuickMocker
import brs.entity.FeeSuggestion
import brs.services.impl.FeeSuggestionServiceImpl
import brs.api.http.common.ResultFields.CHEAP_FEE_RESPONSE
import brs.api.http.common.ResultFields.PRIORITY_FEE_RESPONSE
import brs.api.http.common.ResultFields.STANDARD_FEE_RESPONSE
import brs.util.json.safeGetAsLong
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SuggestFeeTest {

    private lateinit var t: SuggestFee

    private lateinit var feeSuggestionServiceImpl: FeeSuggestionServiceImpl

    @Before
    fun setUp() {
        feeSuggestionServiceImpl = mock()

        t = SuggestFee(feeSuggestionServiceImpl)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val cheap = 1 * FEE_QUANT
        val standard = 5 * FEE_QUANT
        val priority = 10 * FEE_QUANT
        val feeSuggestion = FeeSuggestion(cheap, standard, priority)

        whenever(feeSuggestionServiceImpl.giveFeeSuggestion()).doReturn(feeSuggestion)

        val result = t.processRequest(request) as JsonObject

        assertEquals(cheap, result.get(CHEAP_FEE_RESPONSE).safeGetAsLong())
        assertEquals(standard, result.get(STANDARD_FEE_RESPONSE).safeGetAsLong())
        assertEquals(priority, result.get(PRIORITY_FEE_RESPONSE).safeGetAsLong())
    }
}