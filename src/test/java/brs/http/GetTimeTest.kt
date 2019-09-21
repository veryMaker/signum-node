package brs.http

import brs.common.QuickMocker
import brs.services.TimeService
import brs.util.JSON
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.common.ResultFields.TIME_RESPONSE
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals

class GetTimeTest {

    private var t: GetTime? = null

    private var mockTimeService: TimeService? = null

    @Before
    fun setUp() {
        mockTimeService = mock<TimeService>()

        t = GetTime(mockTimeService!!)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val currentEpochTime = 123

        whenever(mockTimeService!!.epochTime).doReturn(currentEpochTime)

        val result = t!!.processRequest(request) as JsonObject

        assertEquals(currentEpochTime.toLong(), JSON.getAsInt(result.get(TIME_RESPONSE)).toLong())
    }

}
