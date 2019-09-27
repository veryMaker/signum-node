package brs.http

import brs.Account
import brs.Attachment
import brs.Blockchain
import brs.DependencyProvider
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxValues
import brs.http.JSONResponses.INCORRECT_DGS_LISTING_DESCRIPTION
import brs.http.JSONResponses.INCORRECT_DGS_LISTING_NAME
import brs.http.JSONResponses.INCORRECT_DGS_LISTING_TAGS
import brs.http.JSONResponses.MISSING_NAME
import brs.http.common.Parameters.DESCRIPTION_PARAMETER
import brs.http.common.Parameters.NAME_PARAMETER
import brs.http.common.Parameters.PRICE_NQT_PARAMETER
import brs.http.common.Parameters.QUANTITY_PARAMETER
import brs.http.common.Parameters.TAGS_PARAMETER
import brs.services.ParameterService
import brs.transaction.TransactionType
import brs.transaction.digitalGoods.DigitalGoodsListing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import javax.servlet.http.HttpServletRequest

class DGSListingTest : AbstractTransactionTest() {

    private lateinit var t: DGSListing
    private lateinit var dp: DependencyProvider
    private lateinit var mockParameterService: ParameterService
    private lateinit var mockBlockchain: Blockchain
    private lateinit var apiTransactionManagerMock: APITransactionManager

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        mockBlockchain = mock<Blockchain>()
        apiTransactionManagerMock = mock<APITransactionManager>()
        dp = QuickMocker.dependencyProvider(mockParameterService!!, mockBlockchain!!, apiTransactionManagerMock!!)
        t = DGSListing(dp)
    }

    @Test
    fun processRequest() {
        val mockAccount = mock<Account>()

        val dgsName = "dgsName"
        val dgsDescription = "dgsDescription"
        val tags = "tags"
        val priceNqt = 123
        val quantity = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, priceNqt),
                MockParam(QUANTITY_PARAMETER, quantity),
                MockParam(NAME_PARAMETER, dgsName),
                MockParam(DESCRIPTION_PARAMETER, dgsDescription),
                MockParam(TAGS_PARAMETER, tags)
        )

        whenever(mockParameterService!!.getSenderAccount(eq<HttpServletRequest>(request))).doReturn(mockAccount)
        dp.fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(request) }, apiTransactionManagerMock!!) as Attachment.DigitalGoodsListing
        assertNotNull(attachment)

        assertTrue(attachment.transactionType is DigitalGoodsListing)
        assertEquals(dgsName, attachment.name)
        assertEquals(dgsDescription, attachment.description)
        assertEquals(tags, attachment.tags)
        assertEquals(priceNqt.toLong(), attachment.priceNQT)
        assertEquals(quantity.toLong(), attachment.quantity.toLong())
    }

    @Test
    fun processRequest_missingName() {
        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, 123L),
                MockParam(QUANTITY_PARAMETER, 1L)
        )

        assertEquals(MISSING_NAME, t!!.processRequest(request))
    }

    @Test
    fun processRequest_incorrectDGSListingName() {
        var tooLongName = ""

        for (i in 0..100) {
            tooLongName += "a"
        }

        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, 123L),
                MockParam(QUANTITY_PARAMETER, 1L),
                MockParam(NAME_PARAMETER, tooLongName)
        )

        assertEquals(INCORRECT_DGS_LISTING_NAME, t!!.processRequest(request))
    }

    @Test
    fun processRequest_incorrectDgsListingDescription() {
        var tooLongDescription = ""

        for (i in 0..1000) {
            tooLongDescription += "a"
        }

        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, 123L),
                MockParam(QUANTITY_PARAMETER, 1L),
                MockParam(NAME_PARAMETER, "name"),
                MockParam(DESCRIPTION_PARAMETER, tooLongDescription)
        )

        assertEquals(INCORRECT_DGS_LISTING_DESCRIPTION, t!!.processRequest(request))
    }

    @Test
    fun processRequest_incorrectDgsListingTags() {
        var tooLongTags = ""

        for (i in 0..100) {
            tooLongTags += "a"
        }

        val request = QuickMocker.httpServletRequest(
                MockParam(PRICE_NQT_PARAMETER, 123L),
                MockParam(QUANTITY_PARAMETER, 1L),
                MockParam(NAME_PARAMETER, "name"),
                MockParam(DESCRIPTION_PARAMETER, "description"),
                MockParam(TAGS_PARAMETER, tooLongTags)
        )

        assertEquals(INCORRECT_DGS_LISTING_TAGS, t!!.processRequest(request))
    }

}
