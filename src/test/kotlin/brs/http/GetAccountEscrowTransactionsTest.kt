package brs.http

import brs.Account
import brs.Escrow
import brs.Escrow.Decision
import brs.Escrow.DecisionType
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.ESCROWS_RESPONSE
import brs.http.common.ResultFields.AMOUNT_PLANCK_RESPONSE
import brs.http.common.ResultFields.DEADLINE_ACTION_RESPONSE
import brs.http.common.ResultFields.DEADLINE_RESPONSE
import brs.http.common.ResultFields.DECISION_RESPONSE
import brs.http.common.ResultFields.ID_RESPONSE
import brs.http.common.ResultFields.ID_RS_RESPONSE
import brs.http.common.ResultFields.RECIPIENT_RESPONSE
import brs.http.common.ResultFields.RECIPIENT_RS_RESPONSE
import brs.http.common.ResultFields.REQUIRED_SIGNERS_RESPONSE
import brs.http.common.ResultFields.SENDER_RESPONSE
import brs.http.common.ResultFields.SENDER_RS_RESPONSE
import brs.http.common.ResultFields.SIGNERS_RESPONSE
import brs.services.EscrowService
import brs.services.ParameterService
import brs.util.safeGetAsLong
import brs.util.safeGetAsString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAccountEscrowTransactionsTest : AbstractUnitTest() {

    private lateinit var parameterServiceMock: ParameterService
    private lateinit var escrowServiceMock: EscrowService

    private lateinit var t: GetAccountEscrowTransactions

    @Before
    fun setUp() {
        parameterServiceMock = mock()
        escrowServiceMock = mock()

        t = GetAccountEscrowTransactions(parameterServiceMock, escrowServiceMock)
    }

    @Test
    fun processRequest() {
        val accountId: Long = 5

        val request = QuickMocker.httpServletRequest(
                MockParam(ACCOUNT_PARAMETER, accountId)
        )

        val account = mock<Account>()
        whenever(account.id).doReturn(accountId)
        whenever(parameterServiceMock.getAccount(eq(request))).doReturn(account)

        val escrow = mock<Escrow>()
        whenever(escrow.id).doReturn(1L)
        whenever(escrow.senderId).doReturn(2L)
        whenever(escrow.recipientId).doReturn(3L)
        whenever(escrow.amountPlanck).doReturn(4L)
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

        val decisionsIterator = mockCollection(decision, skippedDecision, otherSkippedDecision)
        whenever(escrow.decisions).doReturn(decisionsIterator)

        val escrowCollection = listOf(escrow)
        whenever(escrowServiceMock.getEscrowTransactionsByParticipant(eq(accountId))).doReturn(escrowCollection)

        val resultOverview = t.processRequest(request) as JsonObject
        assertNotNull(resultOverview)

        val resultList = resultOverview.get(ESCROWS_RESPONSE) as JsonArray
        assertNotNull(resultList)
        assertEquals(1, resultList.size().toLong())

        val result = resultList.get(0) as JsonObject
        assertNotNull(result)

        assertEquals("" + escrow.id, result.get(ID_RESPONSE).safeGetAsString())
        assertEquals("" + escrow.senderId, result.get(SENDER_RESPONSE).safeGetAsString())
        assertEquals("BURST-2228-2222-BMNG-22222", result.get(SENDER_RS_RESPONSE).safeGetAsString())
        assertEquals("" + escrow.recipientId, result.get(RECIPIENT_RESPONSE).safeGetAsString())
        assertEquals("BURST-2227-2222-ZAYB-22222", result.get(RECIPIENT_RS_RESPONSE).safeGetAsString())
        assertEquals("" + escrow.amountPlanck, result.get(AMOUNT_PLANCK_RESPONSE).safeGetAsString())
        assertEquals(escrow.requiredSigners.toLong(), result.get(REQUIRED_SIGNERS_RESPONSE).safeGetAsLong())
        assertEquals(escrow.deadline.toLong(), result.get(DEADLINE_RESPONSE).safeGetAsLong())
        assertEquals("undecided", result.get(DEADLINE_ACTION_RESPONSE).safeGetAsString())

        val signersResult = result.get(SIGNERS_RESPONSE) as JsonArray
        assertEquals(1, signersResult.size().toLong())

        val signer = signersResult.get(0) as JsonObject
        assertEquals("" + decision.accountId!!, signer.get(ID_RESPONSE).safeGetAsString())
        assertEquals("BURST-2225-2222-QVC9-22222", signer.get(ID_RS_RESPONSE).safeGetAsString())
        assertEquals("undecided", signer.get(DECISION_RESPONSE).safeGetAsString())
    }
}
