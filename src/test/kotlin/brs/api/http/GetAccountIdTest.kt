package brs.api.http

import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.common.TestConstants.TEST_ACCOUNT_NUMERIC_ID
import brs.common.TestConstants.TEST_PUBLIC_KEY
import brs.common.TestConstants.TEST_SECRET_PHRASE
import brs.api.http.common.JSONResponses.MISSING_SECRET_PHRASE_OR_PUBLIC_KEY
import brs.api.http.common.Parameters.PUBLIC_KEY_PARAMETER
import brs.api.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.api.http.common.ResultFields.ACCOUNT_RESPONSE
import brs.api.http.common.ResultFields.PUBLIC_KEY_RESPONSE
import brs.util.json.safeGetAsString
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAccountIdTest {

    private lateinit var t: GetAccountId

    @Before
    fun setUp() {
        t = GetAccountId()
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest(
                MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
                MockParam(PUBLIC_KEY_PARAMETER, TEST_PUBLIC_KEY)
        )

        val result = t.processRequest(request) as JsonObject

        assertEquals(TEST_ACCOUNT_NUMERIC_ID, result.get(ACCOUNT_RESPONSE).safeGetAsString())
        assertEquals(TEST_PUBLIC_KEY, result.get(PUBLIC_KEY_RESPONSE).safeGetAsString())
    }

    @Test
    fun processRequest_missingSecretPhraseUsesPublicKey() {
        val request = QuickMocker.httpServletRequest(
                MockParam(PUBLIC_KEY_PARAMETER, TEST_PUBLIC_KEY)
        )

        val result = t.processRequest(request) as JsonObject

        assertEquals(TEST_ACCOUNT_NUMERIC_ID, result.get(ACCOUNT_RESPONSE).safeGetAsString())
        assertEquals(TEST_PUBLIC_KEY, result.get(PUBLIC_KEY_RESPONSE).safeGetAsString())
    }

    @Test
    fun processRequest_missingSecretPhraseAndPublicKey() {
        assertEquals(MISSING_SECRET_PHRASE_OR_PUBLIC_KEY, t.processRequest(QuickMocker.httpServletRequest()))
    }

    @Test
    fun requirePost() {
        assertTrue(t.requirePost())
    }
}