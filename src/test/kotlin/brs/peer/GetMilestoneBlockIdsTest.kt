package brs.peer

import brs.services.BlockchainService
import com.nhaarman.mockitokotlin2.mock
import org.junit.Before
import org.junit.Test

class GetMilestoneBlockIdsTest {
    private lateinit var t: GetMilestoneBlockIds
    private lateinit var blockchainService: BlockchainService
    private lateinit var peer: Peer

    @Before
    fun setUp() {
        blockchainService = mock()
        peer = mock()
        t = GetMilestoneBlockIds(blockchainService)
    }

    // TODO normal circumstances test

    @Test
    fun test_nothingProvided() {
        PeerApiTestUtils.testWithNothingProvided(t, "Old getMilestoneBlockIds protocol not supported, please upgrade")
    }
}