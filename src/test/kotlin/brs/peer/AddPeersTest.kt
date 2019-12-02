package brs.peer

import brs.common.QuickMocker
import brs.services.PeerService
import com.nhaarman.mockitokotlin2.mock
import org.junit.Before
import org.junit.Test

class AddPeersTest {
    private lateinit var t: AddPeers
    private lateinit var peerService: PeerService

    @Before
    fun setUp() {
        peerService = mock()
        t = AddPeers(QuickMocker.dependencyProvider(peerService))
    }

    // TODO normal circumstances test

    @Test
    fun test_nothingProvided() {
        PeerApiTestUtils.testWithNothingProvided(t)
    }
}