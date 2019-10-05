package brs.http

import brs.common.QuickMocker
import brs.http.common.ResultFields.TIME_RESPONSE
import brs.services.TimeService
import brs.util.JSON
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetTimeTest {

    private lateinit var t: GetTime

    private lateinit var mockTimeService: TimeService

    @Before
    fun setUp() {
        mockTimeService = mock<TimeService>()

        t = GetTime(mockTimeService!!)
    }

    @Test
    fun processRequest() = runBlocking {
        val request = QuickMocker.httpServletRequest()

        val currentEpochTime = 123

        whenever(mockTimeService!!.epochTime).doReturn(currentEpochTime)

        val result = t!!.processRequest(request) as JsonObject

        assertEquals(currentEpochTime.toLong(), JSON.getAsInt(result.get(TIME_RESPONSE)).toLong())
    }

}
