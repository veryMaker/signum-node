package brs.http

import brs.Account
import brs.BurstException
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.crypto.EncryptedData
import brs.services.ParameterService
import brs.util.JSON
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.common.TestConstants.TEST_PUBLIC_KEY_BYTES
import brs.common.TestConstants.TEST_SECRET_PHRASE
import brs.http.JSONResponses.INCORRECT_ACCOUNT
import brs.http.common.Parameters.*
import brs.http.common.ResultFields.DECRYPTED_MESSAGE_RESPONSE
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class DecryptFromTest {

    private var t: DecryptFrom? = null

    private var mockParameterService: ParameterService? = null

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()

        t = DecryptFrom(mockParameterService!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val req = QuickMocker.httpServletRequest(
                MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
                MockParam(DECRYPTED_MESSAGE_IS_TEXT_PARAMETER, "true"),
                MockParam(DATA_PARAMETER, "abc"),
                MockParam(NONCE_PARAMETER, "def")
        )

        val mockAccount = mock<Account>()

        whenever(mockAccount.decryptFrom(any(), eq<String>(TEST_SECRET_PHRASE)))
                .thenReturn(byteArrayOf(1.toByte()))

        whenever(mockAccount.publicKey).thenReturn(TEST_PUBLIC_KEY_BYTES)

        whenever(mockParameterService!!.getAccount(req)).thenReturn(mockAccount)

        assertEquals("\u0001", JSON.getAsString(JSON.getAsJsonObject(t!!.processRequest(req)).get(DECRYPTED_MESSAGE_RESPONSE)))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_accountWithoutPublicKeyIsIncorrectAccount() {
        val req = QuickMocker.httpServletRequest()

        whenever(mockParameterService!!.getAccount(req)).thenReturn(mock<Account>())

        assertEquals(INCORRECT_ACCOUNT, t!!.processRequest(req))
    }

}
