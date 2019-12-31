package brs.peer

import brs.util.json.JSON
import brs.util.json.safeGetAsJsonObject
import brs.util.json.safeGetAsString
import brs.util.jetty.get
import com.google.gson.JsonObject
import io.mockk.mockk
import org.junit.Assert.assertEquals

object PeerApiTestUtils {
    fun testWithNothingProvided(handler: PeerServlet.PeerRequestHandler, expectedError: String? = null) {
        val json = JsonObject()
        json.addProperty("requestType", "processTransactions")
        JSON.prepareRequest(json)
        val response = handler.processRequest(json, mockk(relaxed = true))
        assertEquals(expectedError, response.safeGetAsJsonObject()?.get("error").safeGetAsString())
    }
}