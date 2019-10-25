package brs.api.http

import brs.objects.Constants.MAX_BALANCE_PLANCK
import brs.entity.Account
import brs.services.BlockchainService
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.objects.FluxValues
import brs.api.http.JSONResponses.INCORRECT_ALIAS_OWNER
import brs.api.http.JSONResponses.INCORRECT_PRICE
import brs.api.http.JSONResponses.INCORRECT_RECIPIENT
import brs.api.http.JSONResponses.MISSING_PRICE
import brs.api.http.common.Parameters.PRICE_PLANCK_PARAMETER
import brs.api.http.common.Parameters.RECIPIENT_PARAMETER
import brs.entity.Alias
import brs.entity.DependencyProvider
import brs.services.ParameterService
import brs.transaction.appendix.Attachment
import brs.transaction.type.TransactionType
import brs.transaction.type.messaging.AliasSell
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SellAliasTest : AbstractTransactionTest() {

    private lateinit var t: SellAlias
    private lateinit var dp: DependencyProvider
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var blockchainServiceMock: BlockchainService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        parameterServiceMock = mock()
        blockchainServiceMock = mock()
        apiTransactionManagerMock = mock()
        dp = QuickMocker.dependencyProvider(parameterServiceMock, blockchainServiceMock, apiTransactionManagerMock)
        t = SellAlias(dp)
    }

    @Test
    fun processRequest() {
        val priceParameter = 10
        val recipientId = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_PLANCK_PARAMETER, priceParameter),
                MockParam(RECIPIENT_PARAMETER, recipientId)
        )

        val aliasAccountId = 1L
        val mockAlias = mock<Alias>()
        whenever(mockAlias.accountId).doReturn(aliasAccountId)
        whenever(mockAlias.aliasName).doReturn("")

        val mockSender = mock<Account>()
        whenever(mockSender.id).doReturn(aliasAccountId)

        whenever(parameterServiceMock.getSenderAccount(request)).doReturn(mockSender)
        whenever(parameterServiceMock.getAlias(request)).doReturn(mockAlias)

        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.MessagingAliasSell
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is AliasSell)
        assertEquals(priceParameter.toLong(), attachment.pricePlanck)
    }

    @Test
    fun processRequest_missingPrice() {
        val request = QuickMocker.httpServletRequest()

        assertEquals(MISSING_PRICE, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectPrice_unParsable() {
        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_PLANCK_PARAMETER, "unParsable")
        )

        assertEquals(INCORRECT_PRICE, t.processRequest(request))
    }

    @Test(expected = ParameterException::class)
    fun processRequest_incorrectPrice_negative() {
        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_PLANCK_PARAMETER, -10L)
        )

        t.processRequest(request)
    }

    @Test(expected = ParameterException::class)
    fun processRequest_incorrectPrice_overMaxBalance() {
        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_PLANCK_PARAMETER, MAX_BALANCE_PLANCK + 1)
        )

        t.processRequest(request)
    }

    @Test
    fun processRequest_incorrectRecipient_unparsable() {
        val price = 10

        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_PLANCK_PARAMETER, price),
                MockParam(RECIPIENT_PARAMETER, "unParsable")
        )

        assertEquals(INCORRECT_RECIPIENT, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectRecipient_zero() {
        val price = 10
        val recipientId = 0

        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_PLANCK_PARAMETER, price),
                MockParam(RECIPIENT_PARAMETER, recipientId)
        )

        assertEquals(INCORRECT_RECIPIENT, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectAliasOwner() {
        val price = 10
        val recipientId = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_PLANCK_PARAMETER, price),
                MockParam(RECIPIENT_PARAMETER, recipientId)
        )

        val aliasAccountId = 1L
        val mockAlias = mock<Alias>()
        whenever(mockAlias.accountId).doReturn(aliasAccountId)

        val mockSenderId = 2L
        val mockSender = mock<Account>()
        whenever(mockSender.id).doReturn(mockSenderId)

        whenever(parameterServiceMock.getSenderAccount(request)).doReturn(mockSender)
        whenever(parameterServiceMock.getAlias(request)).doReturn(mockAlias)

        assertEquals(INCORRECT_ALIAS_OWNER, t.processRequest(request))
    }

}
