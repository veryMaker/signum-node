package brs.http

import brs.*
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxValues
import brs.http.JSONResponses.NOT_ENOUGH_ASSETS
import brs.http.common.Parameters.ASSET_PARAMETER
import brs.http.common.Parameters.QUANTITY_QNT_PARAMETER
import brs.http.common.Parameters.RECIPIENT_PARAMETER
import brs.services.AccountService
import brs.services.ParameterService
import brs.transaction.TransactionType
import brs.transaction.coloredCoins.AssetTransfer
import com.nhaarman.mockitokotlin2.*
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
    private lateinit var blockchainMock: Blockchain
    private lateinit var transactionProcessorMock: TransactionProcessor
    private lateinit var apiTransactionManagerMock: APITransactionManager
    private lateinit var accountServiceMock: AccountService

    @Before
    fun setUp() {
        parameterServiceMock = mock()
        blockchainMock = mock()
        apiTransactionManagerMock = mock()
        transactionProcessorMock = mock()
        accountServiceMock = mock()
        dp = QuickMocker.dependencyProvider(
            parameterServiceMock,
            blockchainMock,
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

        val mockAsset = mock<Asset>()

        whenever(parameterServiceMock.getAsset(eq(request))).doReturn(mockAsset)
        whenever(mockAsset.id).doReturn(assetIdParameter)

        val mockSenderAccount = mock<Account>()
        whenever(accountServiceMock.getUnconfirmedAssetBalanceQuantity(eq(mockSenderAccount), eq(assetIdParameter))).doReturn(500L)

        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(mockSenderAccount)

        dp.fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
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

        val mockAsset = mock<Asset>()

        whenever(parameterServiceMock.getAsset(eq(request))).doReturn(mockAsset)
        whenever(mockAsset.id).doReturn(456L)

        val mockSenderAccount = mock<Account>()
        whenever(parameterServiceMock.getSenderAccount(eq(request))).doReturn(mockSenderAccount)

        whenever(accountServiceMock.getUnconfirmedAssetBalanceQuantity(eq(mockSenderAccount), any())).doReturn(2L)

        assertEquals(NOT_ENOUGH_ASSETS, t.processRequest(request))
    }
}
