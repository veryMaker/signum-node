package brs.http

import brs.Account
import brs.Blockchain
import brs.BurstException
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.services.ParameterService
import brs.util.JSON
import com.google.gson.JsonObject
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.common.ResultFields.ACCOUNT_RESPONSE
import brs.http.common.ResultFields.LESSORS_RESPONSE
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAccountLessorsTest : AbstractUnitTest() {

    private var parameterServiceMock: ParameterService? = null
    private var blockchainMock: Blockchain? = null

    private var t: GetAccountLessors? = null

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        blockchainMock = mock<Blockchain>()

        t = GetAccountLessors(parameterServiceMock!!, blockchainMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val mockAccount = mock<Account>()
        whenever(mockAccount.getId()).doReturn(123L)

        val req = QuickMocker.httpServletRequest()

        whenever(parameterServiceMock!!.getAccount(eq<HttpServletRequest>(req))).doReturn(mockAccount)
        whenever(parameterServiceMock!!.getHeight(eq<HttpServletRequest>(req))).doReturn(0)

        val result = JSON.getAsJsonObject(t!!.processRequest(req))

        assertNotNull(result)
        assertEquals("" + mockAccount.getId(), JSON.getAsString(result.get(ACCOUNT_RESPONSE)))
        TestCase.assertEquals(0, JSON.getAsJsonArray(result.get(LESSORS_RESPONSE)).size())
    }

}
