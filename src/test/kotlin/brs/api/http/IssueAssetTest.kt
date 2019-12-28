package brs.api.http

import brs.api.http.JSONResponses.INCORRECT_ASSET_DESCRIPTION
import brs.api.http.JSONResponses.INCORRECT_ASSET_NAME
import brs.api.http.JSONResponses.INCORRECT_ASSET_NAME_LENGTH
import brs.api.http.JSONResponses.INCORRECT_DECIMALS
import brs.api.http.JSONResponses.MISSING_NAME
import brs.api.http.common.Parameters.DECIMALS_PARAMETER
import brs.api.http.common.Parameters.DESCRIPTION_PARAMETER
import brs.api.http.common.Parameters.NAME_PARAMETER
import brs.api.http.common.Parameters.QUANTITY_QNT_PARAMETER
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.entity.DependencyProvider
import brs.objects.Constants.MAX_ASSET_DESCRIPTION_LENGTH
import brs.objects.Constants.MAX_ASSET_NAME_LENGTH
import brs.objects.Constants.MIN_ASSET_NAME_LENGTH
import brs.objects.FluxValues
import brs.services.BlockchainService
import brs.services.ParameterService
import brs.transaction.appendix.Attachment
import brs.transaction.type.TransactionType
import brs.transaction.type.coloredCoins.AssetIssuance
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class IssueAssetTest : AbstractTransactionTest() {

    private lateinit var t: IssueAsset
    private lateinit var dp: DependencyProvider
    private lateinit var mockParameterService: ParameterService
    private lateinit var mockBlockchainService: BlockchainService
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        mockParameterService = mockk()
        every { mockParameterService.getSenderAccount(any()) } returns mockk()
        mockBlockchainService = mockk()
        every { mockBlockchainService.height } returns 0
        apiTransactionManagerMock = mockk()
        dp = QuickMocker.dependencyProvider(mockParameterService, mockBlockchainService, apiTransactionManagerMock)
        t = IssueAsset(dp)
    }

    @Test
    fun processRequest() {
        val nameParameter = stringWithLength(MIN_ASSET_NAME_LENGTH + 1)
        val descriptionParameter = stringWithLength(MAX_ASSET_DESCRIPTION_LENGTH - 1)
        val decimalsParameter = 4
        val quantityParameter = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(NAME_PARAMETER, nameParameter),
                MockParam(DESCRIPTION_PARAMETER, descriptionParameter),
                MockParam(DECIMALS_PARAMETER, decimalsParameter),
                MockParam(QUANTITY_QNT_PARAMETER, quantityParameter)
        )

        dp.fluxCapacitorService = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t.processRequest(request) }, apiTransactionManagerMock) as Attachment.ColoredCoinsAssetIssuance
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is AssetIssuance)
        assertEquals(nameParameter, attachment.name)
        assertEquals(descriptionParameter, attachment.description)
        assertEquals(decimalsParameter.toLong(), attachment.decimals.toLong())
        assertEquals(descriptionParameter, attachment.description)
    }

    @Test
    fun processRequest_missingName() {
        val request = QuickMocker.httpServletRequest()

        assertEquals(MISSING_NAME, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectAssetNameLength_smallerThanMin() {
        val request = QuickMocker.httpServletRequest(
                MockParam(NAME_PARAMETER, stringWithLength(MIN_ASSET_NAME_LENGTH - 1))
        )

        assertEquals(INCORRECT_ASSET_NAME_LENGTH, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectAssetNameLength_largerThanMax() {
        val request = QuickMocker.httpServletRequest(
                MockParam(NAME_PARAMETER, stringWithLength(MAX_ASSET_NAME_LENGTH + 1))
        )

        assertEquals(INCORRECT_ASSET_NAME_LENGTH, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectAssetName() {
        val request = QuickMocker.httpServletRequest(
                MockParam(NAME_PARAMETER, stringWithLength(MIN_ASSET_NAME_LENGTH + 1) + "[")
        )

        assertEquals(INCORRECT_ASSET_NAME, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectAssetDescription() {
        val request = QuickMocker.httpServletRequest(
                MockParam(NAME_PARAMETER, stringWithLength(MIN_ASSET_NAME_LENGTH + 1)),
                MockParam(DESCRIPTION_PARAMETER, stringWithLength(MAX_ASSET_DESCRIPTION_LENGTH + 1))
        )

        assertEquals(INCORRECT_ASSET_DESCRIPTION, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectDecimals_unParsable() {
        val request = QuickMocker.httpServletRequest(
                MockParam(NAME_PARAMETER, stringWithLength(MIN_ASSET_NAME_LENGTH + 1)),
                MockParam(DESCRIPTION_PARAMETER, stringWithLength(MAX_ASSET_DESCRIPTION_LENGTH - 1)),
                MockParam(DECIMALS_PARAMETER, "unParsable")
        )

        assertEquals(INCORRECT_DECIMALS, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectDecimals_negativeNumber() {
        val request = QuickMocker.httpServletRequest(
                MockParam(NAME_PARAMETER, stringWithLength(MIN_ASSET_NAME_LENGTH + 1)),
                MockParam(DESCRIPTION_PARAMETER, stringWithLength(MAX_ASSET_DESCRIPTION_LENGTH - 1)),
                MockParam(DECIMALS_PARAMETER, -5L)
        )

        assertEquals(INCORRECT_DECIMALS, t.processRequest(request))
    }

    @Test
    fun processRequest_incorrectDecimals_moreThan8() {
        val request = QuickMocker.httpServletRequest(
                MockParam(NAME_PARAMETER, stringWithLength(MIN_ASSET_NAME_LENGTH + 1)),
                MockParam(DESCRIPTION_PARAMETER, stringWithLength(MAX_ASSET_DESCRIPTION_LENGTH - 1)),
                MockParam(DECIMALS_PARAMETER, 9L)
        )

        assertEquals(INCORRECT_DECIMALS, t.processRequest(request))
    }
}
