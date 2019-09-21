package brs.http

import brs.Attachment
import brs.Blockchain
import brs.Burst
import brs.BurstException
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxCapacitor
import brs.fluxcapacitor.FluxValues
import brs.services.AliasService
import brs.services.ParameterService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import javax.servlet.http.HttpServletRequest

import brs.TransactionType.Messaging.ALIAS_ASSIGNMENT
import brs.http.JSONResponses.INCORRECT_ALIAS_LENGTH
import brs.http.JSONResponses.INCORRECT_ALIAS_NAME
import brs.http.JSONResponses.INCORRECT_URI_LENGTH
import brs.http.JSONResponses.MISSING_ALIAS_NAME
import brs.http.common.Parameters.ALIAS_NAME_PARAMETER
import brs.http.common.Parameters.ALIAS_URI_PARAMETER
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SetAliasTest : AbstractTransactionTest() {

    private var t: SetAlias? = null

    private var parameterServiceMock: ParameterService? = null
    private var blockchainMock: Blockchain? = null
    private var aliasServiceMock: AliasService? = null
    private var apiTransactionManagerMock: APITransactionManager? = null

    @Before
    fun setUp() {
        parameterServiceMock = mock()
        whenever(parameterServiceMock!!.getSenderAccount(any())).thenReturn(mock())
        blockchainMock = mock()
        aliasServiceMock = mock()
        apiTransactionManagerMock = mock()

        t = SetAlias(parameterServiceMock!!, blockchainMock!!, aliasServiceMock!!, apiTransactionManagerMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val aliasNameParameter = "aliasNameParameter"
        val aliasUrl = "aliasUrl"

        val request = QuickMocker.httpServletRequest(
                MockParam(ALIAS_NAME_PARAMETER, aliasNameParameter),
                MockParam(ALIAS_URI_PARAMETER, aliasUrl)
        )

        QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) }, apiTransactionManagerMock!!) as Attachment.MessagingAliasAssignment
        assertNotNull(attachment)

        assertEquals(ALIAS_ASSIGNMENT, attachment.transactionType)
        assertEquals(aliasNameParameter, attachment.aliasName)
        assertEquals(aliasUrl, attachment.aliasURI)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_missingAliasName() {
        val request = QuickMocker.httpServletRequest(
                MockParam(ALIAS_NAME_PARAMETER, null as String?),
                MockParam(ALIAS_URI_PARAMETER, "aliasUrl")
        )

        assertEquals(MISSING_ALIAS_NAME, t!!.processRequest(request))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectAliasLength_nameOnlySpaces() {
        val request = QuickMocker.httpServletRequest(
                MockParam(ALIAS_NAME_PARAMETER, "  "),
                MockParam(ALIAS_URI_PARAMETER, null as String?)
        )

        assertEquals(INCORRECT_ALIAS_LENGTH, t!!.processRequest(request))
    }


    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectAliasLength_incorrectAliasName() {
        val request = QuickMocker.httpServletRequest(
                MockParam(ALIAS_NAME_PARAMETER, "[]"),
                MockParam(ALIAS_URI_PARAMETER, null as String?)
        )

        assertEquals(INCORRECT_ALIAS_NAME, t!!.processRequest(request))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectUriLengthWhenOver1000Characters() {
        val uriOver1000Characters = StringBuilder()

        for (i in 0..1000) {
            uriOver1000Characters.append("a")
        }

        val request = QuickMocker.httpServletRequest(
                MockParam(ALIAS_NAME_PARAMETER, "name"),
                MockParam(ALIAS_URI_PARAMETER, uriOver1000Characters.toString())
        )

        assertEquals(INCORRECT_URI_LENGTH, t!!.processRequest(request))
    }

}
