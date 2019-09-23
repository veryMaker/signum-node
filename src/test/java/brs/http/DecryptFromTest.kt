package brs.http

import brs.Account
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.common.TestConstants.TEST_PUBLIC_KEY_BYTES
import brs.common.TestConstants.TEST_SECRET_PHRASE
import brs.http.JSONResponses.INCORRECT_ACCOUNT
import brs.http.common.Parameters.DATA_PARAMETER
import brs.http.common.Parameters.DECRYPTED_MESSAGE_IS_TEXT_PARAMETER
import brs.http.common.Parameters.NONCE_PARAMETER
import brs.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.http.common.ResultFields.DECRYPTED_MESSAGE_RESPONSE
import brs.services.ParameterService
import brs.util.JSON
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DecryptFromTest {

    private var t: DecryptFrom? = null

    private var mockParameterService: ParameterService? = null

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()

        t = DecryptFrom(mockParameterService!!)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest(
                MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
                MockParam(DECRYPTED_MESSAGE_IS_TEXT_PARAMETER, "true"),
                MockParam(DATA_PARAMETER, "abc"),
                MockParam(NONCE_PARAMETER, "def")
        )

        val mockAccount = mock<Account>()

        whenever(mockAccount.decryptFrom(any(), eq<String>(TEST_SECRET_PHRASE)))
                .doReturn(byteArrayOf(1.toByte()))

        whenever(mockAccount.publicKey).doReturn(TEST_PUBLIC_KEY_BYTES)

        whenever(mockParameterService!!.getAccount(request)).doReturn(mockAccount)

        assertEquals("\u0001", JSON.getAsString(JSON.getAsJsonObject(t!!.processRequest(request)).get(DECRYPTED_MESSAGE_RESPONSE)))
    }

    @Test
    fun processRequest_accountWithoutPublicKeyIsIncorrectAccount() {
        val request = QuickMocker.httpServletRequest()

        whenever(mockParameterService!!.getAccount(request)).doReturn(mock<Account>())

        assertEquals(INCORRECT_ACCOUNT, t!!.processRequest(request))
    }

}
