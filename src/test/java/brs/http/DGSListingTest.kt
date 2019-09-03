package brs.http

import brs.*
import brs.TransactionType.DigitalGoods
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxCapacitor
import brs.fluxcapacitor.FluxValues
import brs.http.JSONResponses.INCORRECT_DGS_LISTING_DESCRIPTION
import brs.http.JSONResponses.INCORRECT_DGS_LISTING_NAME
import brs.http.JSONResponses.INCORRECT_DGS_LISTING_TAGS
import brs.http.JSONResponses.MISSING_NAME
import brs.services.ParameterService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import javax.servlet.http.HttpServletRequest
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class DGSListingTest : AbstractTransactionTest() {

    private var t: DGSListing? = null

    private var mockParameterService: ParameterService? = null
    private var mockBlockchain: Blockchain? = null
    private var apiTransactionManagerMock: APITransactionManager? = null

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        mockBlockchain = mock<Blockchain>()
        apiTransactionManagerMock = mock<APITransactionManager>()

        t = DGSListing(mockParameterService!!, mockBlockchain!!, apiTransactionManagerMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val mockAccount = mock<Account>()

        val dgsName = "dgsName"
        val dgsDescription = "dgsDescription"
        val tags = "tags"
        val priceNqt = 123
        val quantity = 5

        val req = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, priceNqt),
                MockParam(QUANTITY_PARAMETER, quantity),
                MockParam(NAME_PARAMETER, dgsName),
                MockParam(DESCRIPTION_PARAMETER, dgsDescription),
                MockParam(TAGS_PARAMETER, tags)
        )

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(req))).doReturn(mockAccount)

        QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(req) }, apiTransactionManagerMock!!) as Attachment.DigitalGoodsListing
        assertNotNull(attachment)

        assertEquals(DigitalGoods.LISTING, attachment.transactionType)
        assertEquals(dgsName, attachment.name)
        assertEquals(dgsDescription, attachment.description)
        assertEquals(tags, attachment.tags)
        assertEquals(priceNqt.toLong(), attachment.priceNQT)
        assertEquals(quantity.toLong(), attachment.quantity.toLong())
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_missingName() {
        val req = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, 123L),
                MockParam(QUANTITY_PARAMETER, 1L)
        )

        assertEquals(MISSING_NAME, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectDGSListingName() {
        var tooLongName = ""

        for (i in 0..100) {
            tooLongName += "a"
        }

        val req = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, 123L),
                MockParam(QUANTITY_PARAMETER, 1L),
                MockParam(NAME_PARAMETER, tooLongName)
        )

        assertEquals(INCORRECT_DGS_LISTING_NAME, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectDgsListingDescription() {
        var tooLongDescription = ""

        for (i in 0..1000) {
            tooLongDescription += "a"
        }

        val req = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, 123L),
                MockParam(QUANTITY_PARAMETER, 1L),
                MockParam(NAME_PARAMETER, "name"),
                MockParam(DESCRIPTION_PARAMETER, tooLongDescription)
        )

        assertEquals(INCORRECT_DGS_LISTING_DESCRIPTION, t!!.processRequest(req))
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_incorrectDgsListingTags() {
        var tooLongTags = ""

        for (i in 0..100) {
            tooLongTags += "a"
        }

        val req = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, 123L),
                MockParam(QUANTITY_PARAMETER, 1L),
                MockParam(NAME_PARAMETER, "name"),
                MockParam(DESCRIPTION_PARAMETER, "description"),
                MockParam(TAGS_PARAMETER, tooLongTags)
        )

        assertEquals(INCORRECT_DGS_LISTING_TAGS, t!!.processRequest(req))
    }

}
