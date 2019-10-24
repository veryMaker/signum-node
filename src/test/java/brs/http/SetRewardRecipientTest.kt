package brs.http

import brs.Account
import brs.Attachment
import brs.Blockchain
import brs.DependencyProvider
import brs.common.JSONTestHelper
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.common.TestConstants
import brs.crypto.Crypto
import brs.fluxcapacitor.FluxValues
import brs.http.common.Parameters.RECIPIENT_PARAMETER
import brs.services.AccountService
import brs.services.ParameterService
import brs.transaction.TransactionType
import brs.transaction.burstMining.RewardRecipientAssignment
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import javax.servlet.http.HttpServletRequest

@RunWith(JUnit4::class)
class SetRewardRecipientTest : AbstractTransactionTest() {

    private lateinit var t: SetRewardRecipient
    private lateinit var dp: DependencyProvider
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var blockchainMock: Blockchain
    private lateinit var accountServiceMock: AccountService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        parameterServiceMock = mock()
        whenever(parameterServiceMock.getSenderAccount(any())).doReturn(mock())
        blockchainMock = mock()
        accountServiceMock = mock()
        apiTransactionManagerMock = mock()
        dp = QuickMocker.dependencyProvider(
            parameterServiceMock,
            blockchainMock,
            accountServiceMock,
            apiTransactionManagerMock
        )
        t = SetRewardRecipient(dp)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest(MockParam(RECIPIENT_PARAMETER, "123"))
        val mockSenderAccount = mock<Account>()
        val mockRecipientAccount = mock<Account>()

        whenever(mockRecipientAccount.publicKey).doReturn(Crypto.getPublicKey(TestConstants.TEST_SECRET_PHRASE))

        whenever(parameterServiceMock.getAccount(eq(request))).doReturn(mockSenderAccount)
        whenever(accountServiceMock.getAccount(eq(123L))).doReturn(mockRecipientAccount)

        dp.fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.BurstMiningRewardRecipientAssignment
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is RewardRecipientAssignment)
    }

    @Test
    fun processRequest_recipientAccountDoesNotExist_errorCode8() {
        val request = QuickMocker.httpServletRequest(MockParam(RECIPIENT_PARAMETER, "123"))
        val mockSenderAccount = mock<Account>()

        whenever(parameterServiceMock.getAccount(eq(request))).doReturn(mockSenderAccount)

        assertEquals(8, JSONTestHelper.errorCode(t.processRequest(request)).toLong())
    }

    @Test
    fun processRequest_recipientAccountDoesNotHavePublicKey_errorCode8() {
        val request = QuickMocker.httpServletRequest(MockParam(RECIPIENT_PARAMETER, "123"))
        val mockSenderAccount = mock<Account>()
        val mockRecipientAccount = mock<Account>()

        whenever(parameterServiceMock.getAccount(eq(request))).doReturn(mockSenderAccount)
        whenever(accountServiceMock.getAccount(eq(123L))).doReturn(mockRecipientAccount)

        assertEquals(8, JSONTestHelper.errorCode(t.processRequest(request)).toLong())
    }
}
