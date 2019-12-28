package brs.peer

import brs.services.TransactionProcessorService
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class ProcessTransactionsTest {
    private lateinit var t: ProcessTransactions
    private lateinit var transactionProcessorService: TransactionProcessorService
    private lateinit var peer: Peer

    @Before
    fun setUp() {
        transactionProcessorService = mockk(relaxed = true)
        peer = mockk(relaxed = true)
        t = ProcessTransactions(transactionProcessorService)
    }

    // TODO normal circumstances test

    @Test
    fun test_nothingProvided() {
        PeerApiTestUtils.testWithNothingProvided(t)
    }
}