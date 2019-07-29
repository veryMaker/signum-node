package brs.http

import brs.*
import brs.Alias.Offer
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.fluxcapacitor.FluxCapacitor
import brs.fluxcapacitor.FluxValues
import brs.services.AliasService
import brs.services.ParameterService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import javax.servlet.http.HttpServletRequest

import brs.TransactionType.Messaging.ALIAS_BUY
import brs.http.JSONResponses.INCORRECT_ALIAS_NOTFORSALE
import brs.http.common.Parameters.AMOUNT_NQT_PARAMETER
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class BuyAliasTest : AbstractTransactionTest() {

    private var t: BuyAlias? = null

    private var parameterServiceMock: ParameterService? = null
    private var blockchain: Blockchain? = null
    private var aliasService: AliasService? = null
    private var apiTransactionManagerMock: APITransactionManager? = null

    @Before
    fun init() {
        parameterServiceMock = mock<ParameterService>()
        blockchain = mock<Blockchain>()
        aliasService = mock<AliasService>()
        apiTransactionManagerMock = mock<APITransactionManager>()

        t = BuyAlias(parameterServiceMock!!, blockchain!!, aliasService!!, apiTransactionManagerMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val req = QuickMocker.httpServletRequestDefaultKeys(MockParam(AMOUNT_NQT_PARAMETER, "" + Constants.ONE_BURST))

        val mockOfferOnAlias = mock<Offer>()

        val mockAliasName = "mockAliasName"
        val mockAlias = mock<Alias>()
        val mockAccount = mock<Account>()
        val mockSellerId = 123L

        whenever(mockAlias.accountId).doReturn(mockSellerId)
        whenever(mockAlias.aliasName).doReturn(mockAliasName)

        whenever(aliasService!!.getOffer(eq(mockAlias))).doReturn(mockOfferOnAlias)

        whenever(parameterServiceMock!!.getAlias(eq(req))).doReturn(mockAlias)
        whenever(parameterServiceMock!!.getSenderAccount(eq(req))).doReturn(mockAccount)

        QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.DIGITAL_GOODS_STORE)

        val attachment = attachmentCreatedTransaction({ t!!.processRequest(req) }, apiTransactionManagerMock!!) as Attachment.MessagingAliasBuy
        assertNotNull(attachment)

        assertEquals(ALIAS_BUY, attachment.transactionType)
        assertEquals(mockAliasName, attachment.aliasName)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_aliasNotForSale() {
        val req = QuickMocker.httpServletRequest(MockParam(AMOUNT_NQT_PARAMETER, "3"))
        val mockAlias = mock<Alias>()

        whenever(parameterServiceMock!!.getAlias(eq<HttpServletRequest>(req))).doReturn(mockAlias)

        whenever(aliasService!!.getOffer(eq(mockAlias))).doReturn(null)

        assertEquals(INCORRECT_ALIAS_NOTFORSALE, t!!.processRequest(req))
    }

}
