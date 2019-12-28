package brs.api.http

import brs.entity.Alias.Offer
import brs.entity.Account
import brs.services.BlockchainService
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.objects.FluxValues
import brs.api.http.JSONResponses.INCORRECT_ALIAS_NOTFORSALE
import brs.api.http.common.Parameters.AMOUNT_PLANCK_PARAMETER
import brs.entity.Alias
import brs.entity.DependencyProvider
import brs.objects.Constants
import brs.services.AliasService
import brs.services.ParameterService
import brs.transaction.appendix.Attachment
import brs.transaction.type.TransactionType
import brs.transaction.type.messaging.AliasBuy
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BuyAliasTest : AbstractTransactionTest() {

    private lateinit var t: BuyAlias
    private lateinit var dp: DependencyProvider
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var blockchainService: BlockchainService
    private lateinit var aliasService: AliasService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun init() {
        parameterServiceMock = mockk(relaxed = true)
        blockchainService = mockk(relaxed = true)
        aliasService = mockk(relaxed = true)
        apiTransactionManagerMock = mockk(relaxed = true)
        dp = QuickMocker.dependencyProvider(parameterServiceMock, blockchainService, aliasService, apiTransactionManagerMock)
        t = BuyAlias(dp)
    }

    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequestDefaultKeys(MockParam(AMOUNT_PLANCK_PARAMETER, Constants.ONE_BURST.toString()))

        val mockOfferOnAlias = mockk<Offer>(relaxed = true)

        val mockAliasName = "mockAliasName"
        val mockAlias = mockk<Alias>(relaxed = true)
        val mockAccount = mockk<Account>(relaxed = true)
        val mockSellerId = 123L

        every { mockAlias.accountId } returns mockSellerId
        every { mockAlias.aliasName } returns mockAliasName

        every { aliasService.getOffer(eq(mockAlias)) } returns mockOfferOnAlias

        every { parameterServiceMock.getAlias(eq(request)) } returns mockAlias
        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockAccount
        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.MessagingAliasBuy
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is AliasBuy)
        assertEquals(mockAliasName, attachment.aliasName)
    }

    @Test
    fun processRequest_aliasNotForSale() {
        val request = QuickMocker.httpServletRequest(MockParam(AMOUNT_PLANCK_PARAMETER, "3"))
        val mockAlias = mockk<Alias>(relaxed = true)

        every { parameterServiceMock.getAlias(eq(request)) } returns mockAlias

        every { aliasService.getOffer(eq(mockAlias)) } returns null

        assertEquals(INCORRECT_ALIAS_NOTFORSALE, t.processRequest(request))
    }

}
