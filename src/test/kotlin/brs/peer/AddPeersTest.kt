package brs.peer

import brs.common.QuickMocker
import brs.services.PeerService
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class AddPeersTest {
    private lateinit var t: AddPeers
    private lateinit var peerService: PeerService

    @Before
    fun setUp() {
        peerService = mockk(relaxed = true)
        t = AddPeers(QuickMocker.dependencyProvider(peerService))
    }

    // TODO normal circumstances test

    @Test
    fun test_nothingProvided() {
        PeerApiTestUtils.testWithNothingProvided(t)
    }
}