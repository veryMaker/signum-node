package brs.http

import brs.*
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxValues
import brs.services.AccountService
import brs.services.ParameterService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import javax.servlet.http.HttpServletRequest

import brs.transaction.TransactionType.ColoredCoins.ASSET_TRANSFER
import brs.http.JSONResponses.NOT_ENOUGH_ASSETS
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TransferAssetTest : AbstractTransactionTest() {

    private var t: TransferAsset? = null

    private var parameterServiceMock: ParameterService? = null
    private var blockchainMock: Blockchain? = null
    private var transactionProcessorMock: TransactionProcessor? = null
    private var apiTransactionManagerMock: APITransactionManager? = null
    private var accountServiceMock: AccountService? = null

    @Before
    fun setUp() {
        parameterServiceMock = mock()
        blockchainMock = mock<Blockchain>()
        apiTransactionManagerMock = mock<APITransactionManager>()
        transactionProcessorMock = mock<TransactionProcessor>()
        accountServiceMock = mock<AccountService>()

        t = TransferAsset(parameterServiceMock!!, blockchainMock!!, apiTransactionManagerMock!!, accountServiceMock!!)
    }

    @Test
    fun processRequest() {
        val recipientParameter = 34L
        val assetIdParameter = 456L
        val quantityQNTParameter = 56L

        val request = QuickMocker.httpServletRequest(
                MockParam(RECIPIENT_PARAMETER, recipientParameter),
                MockParam(ASSET_PARAMETER, assetIdParameter),
                MockParam(QUANTITY_QNT_PARAMETER, quantityQNTParameter)
        )

        val mockAsset = mock<Asset>()

        whenever(parameterServiceMock!!.getAsset(eq<HttpServletRequest>(request))).doReturn(mockAsset)
        whenever(mockAsset.id).doReturn(assetIdParameter)

        val mockSenderAccount = mock<Account>()
        whenever(accountServiceMock!!.getUnconfirmedAssetBalanceQNT(eq(mockSenderAccount), eq(assetIdParameter))).doReturn(500L)

        whenever(parameterServiceMock!!.getSenderAccount(eq(request))).doReturn(mockSenderAccount)

        QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) }, apiTransactionManagerMock!!) as Attachment.ColoredCoinsAssetTransfer
        assertNotNull(attachment)

        assertEquals(ASSET_TRANSFER, attachment.transactionType)
        assertEquals(assetIdParameter, attachment.assetId)
        assertEquals(quantityQNTParameter, attachment.quantityQNT)
    }

    @Test
    fun processRequest_assetBalanceLowerThanQuantityNQTParameter() {
        val request = QuickMocker.httpServletRequest(
                MockParam(RECIPIENT_PARAMETER, "123"),
                MockParam(ASSET_PARAMETER, "456"),
                MockParam(QUANTITY_QNT_PARAMETER, "5")
        )

        val mockAsset = mock<Asset>()

        whenever(parameterServiceMock!!.getAsset(eq<HttpServletRequest>(request))).doReturn(mockAsset)
        whenever(mockAsset.id).doReturn(456L)

        val mockSenderAccount = mock<Account>()
        whenever(parameterServiceMock!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockSenderAccount)

        whenever(accountServiceMock!!.getUnconfirmedAssetBalanceQNT(eq(mockSenderAccount), any())).doReturn(2L)

        assertEquals(NOT_ENOUGH_ASSETS, t!!.processRequest(request))
    }
}
