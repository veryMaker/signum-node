package brs.http

import brs.*
import brs.common.JSONTestHelper
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.common.TestConstants
import brs.crypto.Crypto
import brs.fluxcapacitor.FluxCapacitor
import brs.fluxcapacitor.FluxValues
import brs.services.AccountService
import brs.services.ParameterService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import javax.servlet.http.HttpServletRequest

import brs.TransactionType.BurstMining.REWARD_RECIPIENT_ASSIGNMENT
import brs.http.common.Parameters.RECIPIENT_PARAMETER
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SetRewardRecipientTest : AbstractTransactionTest() {

    private var t: SetRewardRecipient? = null

    private var parameterServiceMock: ParameterService? = null
    private var blockchainMock: Blockchain? = null
    private var accountServiceMock: AccountService? = null
    private var apiTransactionManagerMock: APITransactionManager? = null

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        whenever(parameterServiceMock!!.getSenderAccount(any())).doReturn(mock())
        blockchainMock = mock<Blockchain>()
        accountServiceMock = mock<AccountService>()
        apiTransactionManagerMock = mock<APITransactionManager>()

        t = SetRewardRecipient(parameterServiceMock!!, blockchainMock!!, accountServiceMock!!, apiTransactionManagerMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val req = QuickMocker.httpServletRequest(MockParam(RECIPIENT_PARAMETER, "123"))
        val mockSenderAccount = mock<Account>()
        val mockRecipientAccount = mock<Account>()

        whenever(mockRecipientAccount.publicKey).doReturn(Crypto.getPublicKey(TestConstants.TEST_SECRET_PHRASE))

        whenever(parameterServiceMock!!.getAccount(eq<HttpServletRequest>(req))).doReturn(mockSenderAccount)
        whenever(accountServiceMock!!.getAccount(eq(123L))).doReturn(mockRecipientAccount)

        QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(req) }, apiTransactionManagerMock!!) as Attachment.BurstMiningRewardRecipientAssignment
        assertNotNull(attachment)

        assertEquals(REWARD_RECIPIENT_ASSIGNMENT, attachment.transactionType)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_recipientAccountDoesNotExist_errorCode8() {
        val req = QuickMocker.httpServletRequest(MockParam(RECIPIENT_PARAMETER, "123"))
        val mockSenderAccount = mock<Account>()

        whenever(parameterServiceMock!!.getAccount(eq<HttpServletRequest>(req))).doReturn(mockSenderAccount)

        assertEquals(8, JSONTestHelper.errorCode(t!!.processRequest(req)).toLong())
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_recipientAccountDoesNotHavePublicKey_errorCode8() {
        val req = QuickMocker.httpServletRequest(MockParam(RECIPIENT_PARAMETER, "123"))
        val mockSenderAccount = mock<Account>()
        val mockRecipientAccount = mock<Account>()

        whenever(parameterServiceMock!!.getAccount(eq<HttpServletRequest>(req))).doReturn(mockSenderAccount)
        whenever(accountServiceMock!!.getAccount(eq(123L))).doReturn(mockRecipientAccount)

        assertEquals(8, JSONTestHelper.errorCode(t!!.processRequest(req)).toLong())
    }
}
