package brs.peer

import brs.common.QuickMocker
import brs.services.PeerService
import brs.services.UnconfirmedTransactionService
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class GetUnconfirmedTransactionsTest {
    private lateinit var t: GetUnconfirmedTransactions
    private lateinit var peerService: PeerService
    private lateinit var unconfirmedTransactionService: UnconfirmedTransactionService

    @Before
    fun setUp() {
        peerService = mockk(relaxed = true)
        unconfirmedTransactionService = mockk(relaxed = true)
        t = GetUnconfirmedTransactions(QuickMocker.dependencyProvider(peerService, unconfirmedTransactionService))
    }

    // TODO normal circumstances test

    @Test
    fun test_nothingProvided() {
        PeerApiTestUtils.testWithNothingProvided(t)
    }
}
