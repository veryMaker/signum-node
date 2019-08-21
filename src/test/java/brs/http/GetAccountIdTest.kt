package brs.http

import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.util.JSON
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.common.TestConstants.TEST_ACCOUNT_NUMERIC_ID
import brs.common.TestConstants.TEST_PUBLIC_KEY
import brs.common.TestConstants.TEST_SECRET_PHRASE
import brs.http.JSONResponses.MISSING_SECRET_PHRASE_OR_PUBLIC_KEY
import brs.http.common.Parameters.PUBLIC_KEY_PARAMETER
import brs.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.http.common.ResultFields.ACCOUNT_RESPONSE
import brs.http.common.ResultFields.PUBLIC_KEY_RESPONSE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class GetAccountIdTest {

    private var t: GetAccountId? = null

    @Before
    fun setUp() {
        t = GetAccountId()
    }

    @Test
    fun processRequest() {
        val req = QuickMocker.httpServletRequest(
                MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
                MockParam(PUBLIC_KEY_PARAMETER, TEST_PUBLIC_KEY)
        )

        val result = t!!.processRequest(req) as JsonObject

        assertEquals(TEST_ACCOUNT_NUMERIC_ID, JSON.getAsString(result.get(ACCOUNT_RESPONSE)))
        assertEquals(TEST_PUBLIC_KEY, JSON.getAsString(result.get(PUBLIC_KEY_RESPONSE)))
    }

    @Test
    fun processRequest_missingSecretPhraseUsesPublicKey() {
        val req = QuickMocker.httpServletRequest(
                MockParam(PUBLIC_KEY_PARAMETER, TEST_PUBLIC_KEY)
        )

        val result = t!!.processRequest(req) as JsonObject

        assertEquals(TEST_ACCOUNT_NUMERIC_ID, JSON.getAsString(result.get(ACCOUNT_RESPONSE)))
        assertEquals(TEST_PUBLIC_KEY, JSON.getAsString(result.get(PUBLIC_KEY_RESPONSE)))
    }

    @Test
    fun processRequest_missingSecretPhraseAndPublicKey() {
        assertEquals(MISSING_SECRET_PHRASE_OR_PUBLIC_KEY, t!!.processRequest(QuickMocker.httpServletRequest()))
    }

    @Test
    fun requirePost() {
        assertTrue(t!!.requirePost())
    }
}