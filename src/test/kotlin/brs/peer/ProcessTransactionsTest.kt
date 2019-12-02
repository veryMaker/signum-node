package brs.peer

import brs.services.TransactionProcessorService
import com.nhaarman.mockitokotlin2.mock
import org.junit.Before
import org.junit.Test

class ProcessTransactionsTest {
    private lateinit var t: ProcessTransactions
    private lateinit var transactionProcessorService: TransactionProcessorService
    private lateinit var peer: Peer

    @Before
    fun setUp() {
        transactionProcessorService = mock()
        peer = mock()
        t = ProcessTransactions(transactionProcessorService)
    }

    // TODO normal circumstances test

    @Test
    fun test_nothingProvided() {
        PeerApiTestUtils.testWithNothingProvided(t)
    }
}