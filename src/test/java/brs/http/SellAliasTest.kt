package brs.http

import brs.*
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxCapacitor
import brs.fluxcapacitor.FluxValues
import brs.services.ParameterService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import javax.servlet.http.HttpServletRequest

import brs.Constants.MAX_BALANCE_NQT
import brs.TransactionType.Messaging.ALIAS_SELL
import brs.http.JSONResponses.INCORRECT_ALIAS_OWNER
import brs.http.JSONResponses.INCORRECT_PRICE
import brs.http.JSONResponses.INCORRECT_RECIPIENT
import brs.http.JSONResponses.MISSING_PRICE
import brs.http.common.Parameters.PRICE_NQT_PARAMETER
import brs.http.common.Parameters.RECIPIENT_PARAMETER
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SellAliasTest : AbstractTransactionTest() {

    private var t: SellAlias? = null

    private var parameterServiceMock: ParameterService? = null
    private var blockchainMock: Blockchain? = null
    private var apiTransactionManagerMock: APITransactionManager? = null

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        blockchainMock = mock<Blockchain>()
        apiTransactionManagerMock = mock<APITransactionManager>()

        t = SellAlias(parameterServiceMock!!, blockchainMock!!, apiTransactionManagerMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val priceParameter = 10
        val recipientId = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, priceParameter),
                MockParam(RECIPIENT_PARAMETER, recipientId)
        )

        val aliasAccountId = 1L
        val mockAlias = mock<Alias>()
        whenever(mockAlias.accountId).doReturn(aliasAccountId)
        whenever(mockAlias.aliasName).doReturn("")

        val mockSender = mock<Account>()
        whenever(mockSender.id).doReturn(aliasAccountId)

        whenever(parameterServiceMock!!.getSenderAccount(request)).doReturn(mockSender)
        whenever(parameterServiceMock!!.getAlias(request)).doReturn(mockAlias)

        QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) }, apiTransactionManagerMock!!) as Attachment.MessagingAliasSell
        assertNotNull(attachment)

        assertEquals(ALIAS_SELL, attachment.transactionType)
        assertEquals(priceParameter.toLong(), attachment.priceNQT)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_missingPrice() {
        val request = QuickMocker.httpServletRequest()

        assertEquals(MISSING_PRICE, t!!.processRequest(request))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectPrice_unParsable() {
        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, "unParsable")
        )

        assertEquals(INCORRECT_PRICE, t!!.processRequest(request))
    }

    @Test(expected = ParameterException::class)
    @Throws(BurstException::class)
    fun processRequest_incorrectPrice_negative() {
        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, -10L)
        )

        t!!.processRequest(request)
    }

    @Test(expected = ParameterException::class)
    @Throws(BurstException::class)
    fun processRequest_incorrectPrice_overMaxBalance() {
        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, MAX_BALANCE_NQT + 1)
        )

        t!!.processRequest(request)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectRecipient_unparsable() {
        val price = 10

        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, price),
                MockParam(RECIPIENT_PARAMETER, "unParsable")
        )

        assertEquals(INCORRECT_RECIPIENT, t!!.processRequest(request))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectRecipient_zero() {
        val price = 10
        val recipientId = 0

        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, price),
                MockParam(RECIPIENT_PARAMETER, recipientId)
        )

        assertEquals(INCORRECT_RECIPIENT, t!!.processRequest(request))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectAliasOwner() {
        val price = 10
        val recipientId = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, price),
                MockParam(RECIPIENT_PARAMETER, recipientId)
        )

        val aliasAccountId = 1L
        val mockAlias = mock<Alias>()
        whenever(mockAlias.accountId).doReturn(aliasAccountId)

        val mockSenderId = 2L
        val mockSender = mock<Account>()
        whenever(mockSender.id).doReturn(mockSenderId)

        whenever(parameterServiceMock!!.getSenderAccount(request)).doReturn(mockSender)
        whenever(parameterServiceMock!!.getAlias(request)).doReturn(mockAlias)

        assertEquals(INCORRECT_ALIAS_OWNER, t!!.processRequest(request))
    }

}
