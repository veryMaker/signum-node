package brs.api.http

import brs.api.http.common.ResultFields.TIME_RESPONSE
import brs.common.QuickMocker
import brs.services.TimeService
import brs.util.json.getMemberAsLong
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetTimeTest {
    private lateinit var t: GetTime

    private lateinit var mockTimeService: TimeService

    @Before
    fun setUp() {
        mockTimeService = mockk(relaxed = true)

        t = GetTime(mockTimeService)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val currentEpochTime = 123

        every { mockTimeService.epochTime } returns currentEpochTime

        val result = t.processRequest(request) as JsonObject

        assertEquals(currentEpochTime.toLong(), result.getMemberAsLong(TIME_RESPONSE))
    }
}
