package brs.http

import brs.*
import brs.Alias.Offer
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxValues
import brs.http.JSONResponses.INCORRECT_ALIAS_NOTFORSALE
import brs.http.common.Parameters.AMOUNT_PLANCK_PARAMETER
import brs.services.AliasService
import brs.services.ParameterService
import brs.transaction.TransactionType
import brs.transaction.messaging.AliasBuy
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import javax.servlet.http.HttpServletRequest

class BuyAliasTest : AbstractTransactionTest() {

    private lateinit var t: BuyAlias
    private lateinit var dp: DependencyProvider
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var blockchain: Blockchain
    private lateinit var aliasService: AliasService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun init() {
        parameterServiceMock = mock()
        blockchain = mock()
        aliasService = mock()
        apiTransactionManagerMock = mock()
        dp = QuickMocker.dependencyProvider(parameterServiceMock, blockchain, aliasService, apiTransactionManagerMock)
        t = BuyAlias(dp)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequestDefaultKeys(MockParam(AMOUNT_PLANCK_PARAMETER, "" + Constants.ONE_BURST))

        val mockOfferOnAlias = mock<Offer>()

        val mockAliasName = "mockAliasName"
        val mockAlias = mock<Alias>()
        val mockAccount = mock<Account>()
        val mockSellerId = 123L

        whenever(mockAlias.accountId).doReturn(mockSellerId)
        whenever(mockAlias.aliasName).doReturn(mockAliasName)

        whenever(aliasService.getOffer(eq(mockAlias))).doReturn(mockOfferOnAlias)

        whenever(parameterServiceMock.getAlias(eq(request))).doReturn(mockAlias)
        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(mockAccount)
        dp.fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.MessagingAliasBuy
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is AliasBuy)
        assertEquals(mockAliasName, attachment.aliasName)
    }

    @Test
    fun processRequest_aliasNotForSale() {
        val request = QuickMocker.httpServletRequest(MockParam(AMOUNT_PLANCK_PARAMETER, "3"))
        val mockAlias = mock<Alias>()

        whenever(parameterServiceMock.getAlias(eq(request))).doReturn(mockAlias)

        whenever(aliasService.getOffer(eq(mockAlias))).doReturn(null)

        assertEquals(INCORRECT_ALIAS_NOTFORSALE, t.processRequest(request))
    }

}
