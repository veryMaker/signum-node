package brs.api.http

import brs.api.http.common.Parameters.RECIPIENT_PARAMETER
import brs.common.JSONTestHelper
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.common.TestConstants
import brs.entity.Account
import brs.entity.DependencyProvider
import brs.objects.FluxValues
import brs.services.AccountService
import brs.services.BlockchainService
import brs.services.FluxCapacitorService
import brs.services.ParameterService
import brs.transaction.appendix.Attachment
import brs.transaction.type.TransactionType
import brs.transaction.type.burstMining.RewardRecipientAssignment
import brs.util.crypto.Crypto
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SetRewardRecipientTest : AbstractTransactionTest() {

    private lateinit var t: SetRewardRecipient
    private lateinit var dp: DependencyProvider
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var blockchainServiceMock: BlockchainService
    private lateinit var accountServiceMock: AccountService
    private lateinit var apiTransactionManagerMock: APITransactionManager
    private lateinit var fluxCapacitorServiceMock: FluxCapacitorService

    @Before
    fun setUp() {
        parameterServiceMock = mockk()
        every { parameterServiceMock.getSenderAccount(any()) } returns mockk()
        blockchainServiceMock = mockk()
        accountServiceMock = mockk()
        apiTransactionManagerMock = mockk()
        fluxCapacitorServiceMock = mockk()
        dp = QuickMocker.dependencyProvider(
            parameterServiceMock,
            blockchainServiceMock,
            accountServiceMock,
            apiTransactionManagerMock,
            fluxCapacitorServiceMock
        )
        t = SetRewardRecipient(dp)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest(MockParam(RECIPIENT_PARAMETER, "123"))
        val mockSenderAccount = mockk<Account>()
        val mockRecipientAccount = mockk<Account>()

        every { mockRecipientAccount.publicKey } returns Crypto.getPublicKey(TestConstants.TEST_SECRET_PHRASE)

        every { parameterServiceMock.getAccount(eq(request)) } returns mockSenderAccount
        every { accountServiceMock.getAccount(eq(123L)) } returns mockRecipientAccount

        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.BurstMiningRewardRecipientAssignment
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is RewardRecipientAssignment)
    }

    @Test
    fun processRequest_recipientAccountDoesNotExist_errorCode8() {
        val request = QuickMocker.httpServletRequest(MockParam(RECIPIENT_PARAMETER, "123"))
        val mockSenderAccount = mockk<Account>()

        every { parameterServiceMock.getAccount(eq(request)) } returns mockSenderAccount

        assertEquals(8, JSONTestHelper.errorCode(t.processRequest(request)).toLong())
    }

    @Test
    fun processRequest_recipientAccountDoesNotHavePublicKey_errorCode8() {
        val request = QuickMocker.httpServletRequest(MockParam(RECIPIENT_PARAMETER, "123"))
        val mockSenderAccount = mockk<Account>()
        val mockRecipientAccount = mockk<Account>()

        every { parameterServiceMock.getAccount(eq(request)) } returns mockSenderAccount
        every { accountServiceMock.getAccount(eq(123L)) } returns mockRecipientAccount

        assertEquals(8, JSONTestHelper.errorCode(t.processRequest(request)).toLong())
    }
}
