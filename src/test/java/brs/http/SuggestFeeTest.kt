package brs.http

import brs.BurstException
import brs.common.QuickMocker
import brs.feesuggestions.FeeSuggestion
import brs.feesuggestions.FeeSuggestionCalculator
import brs.util.JSON
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.Constants.FEE_QUANT
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals

class SuggestFeeTest {

    private var t: SuggestFee? = null

    private var feeSuggestionCalculator: FeeSuggestionCalculator? = null

    @Before
    fun setUp() {
        feeSuggestionCalculator = mock<FeeSuggestionCalculator>()

        t = SuggestFee(feeSuggestionCalculator!!)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val cheap = 1 * FEE_QUANT
        val standard = 5 * FEE_QUANT
        val priority = 10 * FEE_QUANT
        val feeSuggestion = FeeSuggestion(cheap, standard, priority)

        whenever(feeSuggestionCalculator!!.giveFeeSuggestion()).doReturn(feeSuggestion)

        val result = t!!.processRequest(request) as JsonObject

        assertEquals(cheap, JSON.getAsLong(result.get(CHEAP_FEE_RESPONSE)))
        assertEquals(standard, JSON.getAsLong(result.get(STANDARD_FEE_RESPONSE)))
        assertEquals(priority, JSON.getAsLong(result.get(PRIORITY_FEE_RESPONSE)))
    }
}