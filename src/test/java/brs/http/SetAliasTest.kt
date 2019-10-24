package brs.http

import brs.Attachment
import brs.Blockchain
import brs.DependencyProvider
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxValues
import brs.http.JSONResponses.INCORRECT_ALIAS_LENGTH
import brs.http.JSONResponses.INCORRECT_ALIAS_NAME
import brs.http.JSONResponses.INCORRECT_URI_LENGTH
import brs.http.JSONResponses.MISSING_ALIAS_NAME
import brs.http.common.Parameters.ALIAS_NAME_PARAMETER
import brs.http.common.Parameters.ALIAS_URI_PARAMETER
import brs.services.AliasService
import brs.services.ParameterService
import brs.transaction.TransactionType
import brs.transaction.messaging.AliasAssignment
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SetAliasTest : AbstractTransactionTest() {

    private lateinit var t: SetAlias
    private lateinit var dp: DependencyProvider
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var blockchainMock: Blockchain
    private lateinit var aliasServiceMock: AliasService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        parameterServiceMock = mock()
        whenever(parameterServiceMock.getSenderAccount(any())).thenReturn(mock())
        blockchainMock = mock()
        aliasServiceMock = mock()
        apiTransactionManagerMock = mock()
        dp = QuickMocker.dependencyProvider(
            parameterServiceMock,
            blockchainMock,
            aliasServiceMock,
            apiTransactionManagerMock, QuickMocker.latestValueFluxCapacitor())
        t = SetAlias(dp)
    }

    @Test
    fun processRequest() {
        val aliasNameParameter = "aliasNameParameter"
        val aliasUrl = "aliasUrl"

        val request = QuickMocker.httpServletRequest(
                MockParam(ALIAS_NAME_PARAMETER, aliasNameParameter),
                MockParam(ALIAS_URI_PARAMETER, aliasUrl)
        )

        dp.fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.MessagingAliasAssignment
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is AliasAssignment)
        assertEquals(aliasNameParameter, attachment.aliasName)
        assertEquals(aliasUrl, attachment.aliasURI)
    }

    @Test
    fun processRequest_missingAliasName() {
        val request = QuickMocker.httpServletRequest(
                MockParam(ALIAS_NAME_PARAMETER, null as String?),
                MockParam(ALIAS_URI_PARAMETER, "aliasUrl")
        )

        assertEquals(MISSING_ALIAS_NAME, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectAliasLength_nameOnlySpaces() {
        val request = QuickMocker.httpServletRequest(
                MockParam(ALIAS_NAME_PARAMETER, "  "),
                MockParam(ALIAS_URI_PARAMETER, null as String?)
        )

        assertEquals(INCORRECT_ALIAS_LENGTH, t.processRequest(request))
    }


    @Test
    fun processRequest_incorrectAliasLength_incorrectAliasName() {
        val request = QuickMocker.httpServletRequest(
                MockParam(ALIAS_NAME_PARAMETER, "[]"),
                MockParam(ALIAS_URI_PARAMETER, null as String?)
        )

        assertEquals(INCORRECT_ALIAS_NAME, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectUriLengthWhenOver1000Characters() {
        val uriOver1000Characters = stringWithLength(1001)

        val request = QuickMocker.httpServletRequest(
                MockParam(ALIAS_NAME_PARAMETER, "name"),
                MockParam(ALIAS_URI_PARAMETER, uriOver1000Characters)
        )

        assertEquals(INCORRECT_URI_LENGTH, t.processRequest(request))
    }

}
