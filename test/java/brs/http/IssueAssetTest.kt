package brs.http

import brs.Attachment
import brs.Blockchain
import brs.Burst
import brs.BurstException
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxCapacitor
import brs.fluxcapacitor.FluxValues
import brs.services.ParameterService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import javax.servlet.http.HttpServletRequest

import brs.Constants.*
import brs.TransactionType.ColoredCoins.ASSET_ISSUANCE
import brs.http.JSONResponses.INCORRECT_ASSET_DESCRIPTION
import brs.http.JSONResponses.INCORRECT_ASSET_NAME
import brs.http.JSONResponses.INCORRECT_ASSET_NAME_LENGTH
import brs.http.JSONResponses.INCORRECT_DECIMALS
import brs.http.JSONResponses.MISSING_NAME
import brs.http.common.Parameters.*
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class IssueAssetTest : AbstractTransactionTest() {

    private var t: IssueAsset? = null

    private var mockParameterService: ParameterService? = null
    private var mockBlockchain: Blockchain? = null
    private var apiTransactionManagerMock: APITransactionManager? = null

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        mockBlockchain = mock<Blockchain>()
        apiTransactionManagerMock = mock<APITransactionManager>()

        t = IssueAsset(mockParameterService!!, mockBlockchain!!, apiTransactionManagerMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val nameParameter = stringWithLength(MIN_ASSET_NAME_LENGTH + 1)
        val descriptionParameter = stringWithLength(MAX_ASSET_DESCRIPTION_LENGTH - 1)
        val decimalsParameter = 4
        val quantityQNTParameter = 5

        val req = QuickMocker.httpServletRequest(
                MockParam(NAME_PARAMETER, nameParameter),
                MockParam(DESCRIPTION_PARAMETER, descriptionParameter),
                MockParam(DECIMALS_PARAMETER, decimalsParameter),
                MockParam(QUANTITY_QNT_PARAMETER, quantityQNTParameter)
        )

        QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(req) }, apiTransactionManagerMock!!) as Attachment.ColoredCoinsAssetIssuance
        assertNotNull(attachment)

        assertEquals(ASSET_ISSUANCE, attachment.transactionType)
        assertEquals(nameParameter, attachment.name)
        assertEquals(descriptionParameter, attachment.description)
        assertEquals(decimalsParameter.toLong(), attachment.decimals.toLong())
        assertEquals(descriptionParameter, attachment.description)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_missingName() {
        val req = QuickMocker.httpServletRequest()

        assertEquals(MISSING_NAME, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectAssetNameLength_smallerThanMin() {
        val req = QuickMocker.httpServletRequest(
                MockParam(NAME_PARAMETER, stringWithLength(MIN_ASSET_NAME_LENGTH - 1))
        )

        assertEquals(INCORRECT_ASSET_NAME_LENGTH, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectAssetNameLength_largerThanMax() {
        val req = QuickMocker.httpServletRequest(
                MockParam(NAME_PARAMETER, stringWithLength(MAX_ASSET_NAME_LENGTH + 1))
        )

        assertEquals(INCORRECT_ASSET_NAME_LENGTH, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectAssetName() {
        val req = QuickMocker.httpServletRequest(
                MockParam(NAME_PARAMETER, stringWithLength(MIN_ASSET_NAME_LENGTH + 1) + "[")
        )

        assertEquals(INCORRECT_ASSET_NAME, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectAssetDescription() {
        val req = QuickMocker.httpServletRequest(
                MockParam(NAME_PARAMETER, stringWithLength(MIN_ASSET_NAME_LENGTH + 1)),
                MockParam(DESCRIPTION_PARAMETER, stringWithLength(MAX_ASSET_DESCRIPTION_LENGTH + 1))
        )

        assertEquals(INCORRECT_ASSET_DESCRIPTION, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectDecimals_unParsable() {
        val req = QuickMocker.httpServletRequest(
                MockParam(NAME_PARAMETER, stringWithLength(MIN_ASSET_NAME_LENGTH + 1)),
                MockParam(DESCRIPTION_PARAMETER, stringWithLength(MAX_ASSET_DESCRIPTION_LENGTH - 1)),
                MockParam(DECIMALS_PARAMETER, "unParsable")
        )

        assertEquals(INCORRECT_DECIMALS, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectDecimals_negativeNumber() {
        val req = QuickMocker.httpServletRequest(
                MockParam(NAME_PARAMETER, stringWithLength(MIN_ASSET_NAME_LENGTH + 1)),
                MockParam(DESCRIPTION_PARAMETER, stringWithLength(MAX_ASSET_DESCRIPTION_LENGTH - 1)),
                MockParam(DECIMALS_PARAMETER, -5L)
        )

        assertEquals(INCORRECT_DECIMALS, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectDecimals_moreThan8() {
        val req = QuickMocker.httpServletRequest(
                MockParam(NAME_PARAMETER, stringWithLength(MIN_ASSET_NAME_LENGTH + 1)),
                MockParam(DESCRIPTION_PARAMETER, stringWithLength(MAX_ASSET_DESCRIPTION_LENGTH - 1)),
                MockParam(DECIMALS_PARAMETER, 9L)
        )

        assertEquals(INCORRECT_DECIMALS, t!!.processRequest(req))
    }

}
