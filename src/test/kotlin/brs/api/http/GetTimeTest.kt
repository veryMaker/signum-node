package brs.api.http

import brs.common.QuickMocker
import brs.api.http.common.ResultFields.TIME_RESPONSE
import brs.services.TimeService
import brs.util.json.safeGetAsLong
import com.google.gson.JsonObject
import io.mockk.mockk
import io.mockk.every
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

        assertEquals(currentEpochTime.toLong(), result.get(TIME_RESPONSE).safeGetAsLong())
    }

}
