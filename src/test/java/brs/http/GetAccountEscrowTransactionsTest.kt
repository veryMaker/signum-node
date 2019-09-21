package brs.http

import brs.Account
import brs.BurstException
import brs.Escrow
import brs.Escrow.Decision
import brs.Escrow.DecisionType
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.services.EscrowService
import brs.services.ParameterService
import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest
import java.util.Arrays

import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.ESCROWS_RESPONSE
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAccountEscrowTransactionsTest : AbstractUnitTest() {

    private var parameterServiceMock: ParameterService? = null
    private var escrowServiceMock: EscrowService? = null

    private var t: GetAccountEscrowTransactions? = null

    @Before
    fun setUp() {
        parameterServiceMock = mock<ParameterService>()
        escrowServiceMock = mock<EscrowService>()

        t = GetAccountEscrowTransactions(parameterServiceMock!!, escrowServiceMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val accountId: Long = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, accountId)
        )

        val account = mock<Account>()
        whenever(account.id).doReturn(accountId)
        whenever(parameterServiceMock!!.getAccount(eq<HttpServletRequest>(request))).doReturn(account)

        val escrow = mock<Escrow>()
        whenever(escrow.id).doReturn(1L)
        whenever(escrow.senderId).doReturn(2L)
        whenever(escrow.recipientId).doReturn(3L)
        whenever(escrow.amountNQT).doReturn(4L)
        whenever(escrow.requiredSigners).doReturn(5)
        whenever(escrow.deadlineAction).doReturn(DecisionType.UNDECIDED)

        val decision = mock<Decision>()
        whenever(decision.accountId).doReturn(3L)
        whenever(decision.decision).doReturn(DecisionType.UNDECIDED)

        val skippedDecision = mock<Decision>()
        whenever(skippedDecision.accountId).doReturn(5L)

        val otherSkippedDecision = mock<Decision>()
        whenever(otherSkippedDecision.accountId).doReturn(6L)

        whenever(escrow.recipientId).doReturn(5L)
        whenever(escrow.senderId).doReturn(6L)

        val decisionsIterator = mockCollection<Decision>(decision, skippedDecision, otherSkippedDecision)
        whenever(escrow.decisions).doReturn(decisionsIterator)

        val escrowCollection = listOf(escrow)
        whenever(escrowServiceMock!!.getEscrowTransactionsByParticipant(eq(accountId))).doReturn(escrowCollection)

        val resultOverview = t!!.processRequest(request) as JsonObject
        assertNotNull(resultOverview)

        val resultList = resultOverview.get(ESCROWS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size().toLong())

        val result = resultList.get(0) as JsonObject
        assertNotNull(result)

        assertEquals("" + escrow.id!!, JSON.getAsString(result.get(ID_RESPONSE)))
        assertEquals("" + escrow.senderId!!, JSON.getAsString(result.get(SENDER_RESPONSE)))
        assertEquals("BURST-2228-2222-BMNG-22222", JSON.getAsString(result.get(SENDER_RS_RESPONSE)))
        assertEquals("" + escrow.recipientId!!, JSON.getAsString(result.get(RECIPIENT_RESPONSE)))
        assertEquals("BURST-2227-2222-ZAYB-22222", JSON.getAsString(result.get(RECIPIENT_RS_RESPONSE)))
        assertEquals("" + escrow.amountNQT!!, JSON.getAsString(result.get(AMOUNT_NQT_RESPONSE)))
        assertEquals(escrow.requiredSigners.toLong(), JSON.getAsInt(result.get(REQUIRED_SIGNERS_RESPONSE)).toLong())
        assertEquals(escrow.deadline.toLong(), JSON.getAsInt(result.get(DEADLINE_RESPONSE)).toLong())
        assertEquals("undecided", JSON.getAsString(result.get(DEADLINE_ACTION_RESPONSE)))

        val signersResult = result.get(SIGNERS_RESPONSE) as JsonArray
        assertEquals(1, signersResult.size().toLong())

        val signer = signersResult.get(0) as JsonObject
        assertEquals("" + decision.accountId!!, JSON.getAsString(signer.get(ID_RESPONSE)))
        assertEquals("BURST-2225-2222-QVC9-22222", JSON.getAsString(signer.get(ID_RS_RESPONSE)))
        assertEquals("undecided", JSON.getAsString(signer.get(DECISION_RESPONSE)))
    }
}
