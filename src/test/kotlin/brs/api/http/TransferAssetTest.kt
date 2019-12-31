package brs.api.http

import brs.api.http.common.JSONResponses.NOT_ENOUGH_ASSETS
import brs.api.http.common.Parameters.ASSET_PARAMETER
import brs.api.http.common.Parameters.QUANTITY_QNT_PARAMETER
import brs.api.http.common.Parameters.RECIPIENT_PARAMETER
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.entity.Account
import brs.entity.Asset
import brs.entity.DependencyProvider
import brs.objects.FluxValues
import brs.services.AccountService
import brs.services.BlockchainService
import brs.services.ParameterService
import brs.services.TransactionProcessorService
import brs.transaction.appendix.Attachment
import brs.transaction.type.TransactionType
import brs.transaction.type.coloredCoins.AssetTransfer
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TransferAssetTest : AbstractTransactionTest() {

    private lateinit var t: TransferAsset
    private lateinit var dp: DependencyProvider
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var blockchainServiceMock: BlockchainService
    private lateinit var transactionProcessorServiceMock: TransactionProcessorService
    private lateinit var apiTransactionManagerMock: APITransactionManager
    private lateinit var accountServiceMock: AccountService

    @Before
    fun setUp() {
        parameterServiceMock = mockk(relaxed = true)
        blockchainServiceMock = mockk(relaxed = true)
        apiTransactionManagerMock = mockk(relaxed = true)
        transactionProcessorServiceMock = mockk(relaxed = true)
        accountServiceMock = mockk(relaxed = true)
        dp = QuickMocker.dependencyProvider(
            parameterServiceMock,
            blockchainServiceMock,
            apiTransactionManagerMock,
            accountServiceMock
        )
        t = TransferAsset(dp)
    }

    @Test
    fun processRequest() {
        val recipientParameter = 34L
        val assetIdParameter = 456L
        val quantityParameter = 56L

        val request = QuickMocker.httpServletRequest(
                MockParam(RECIPIENT_PARAMETER, recipientParameter),
                MockParam(ASSET_PARAMETER, assetIdParameter),
                MockParam(QUANTITY_QNT_PARAMETER, quantityParameter)
        )

        val mockAsset = mockk<Asset>(relaxed = true)

        every { parameterServiceMock.getAsset(eq(request)) } returns mockAsset
        every { mockAsset.id } returns assetIdParameter

        val mockSenderAccount = mockk<Account>(relaxed = true)
        every { accountServiceMock.getUnconfirmedAssetBalanceQuantity(eq(mockSenderAccount), eq(assetIdParameter)) } returns 500L

        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockSenderAccount

        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.ColoredCoinsAssetTransfer
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is AssetTransfer)
        assertEquals(assetIdParameter, attachment.assetId)
        assertEquals(quantityParameter, attachment.quantity)
    }

    @Test
    fun processRequest_assetBalanceLowerThanQuantityPlanckParameter() {
        val request = QuickMocker.httpServletRequest(
                MockParam(RECIPIENT_PARAMETER, "123"),
                MockParam(ASSET_PARAMETER, "456"),
                MockParam(QUANTITY_QNT_PARAMETER, "5")
        )

        val mockAsset = mockk<Asset>(relaxed = true)

        every { parameterServiceMock.getAsset(eq(request)) } returns mockAsset
        every { mockAsset.id } returns 456L

        val mockSenderAccount = mockk<Account>(relaxed = true)
        every { parameterServiceMock.getSenderAccount(eq(request)) } returns mockSenderAccount

        every { accountServiceMock.getUnconfirmedAssetBalanceQuantity(eq(mockSenderAccount), any()) } returns 2L

        assertEquals(NOT_ENOUGH_ASSETS, t.processRequest(request))
    }
}
