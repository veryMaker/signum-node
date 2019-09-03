package brs.grpc.handlers

import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.peer.Peer
import brs.peer.Peers

class GetPeersHandler : GrpcApiHandler<BrsApi.GetPeersRequest, BrsApi.Peers> {
    @Throws(Exception::class)
    override fun handleRequest(getPeersRequest: BrsApi.GetPeersRequest): BrsApi.Peers {
        val active = getPeersRequest.active
        val peerState = getPeersRequest.state
        val peers = BrsApi.Peers.newBuilder()
        for (peer in if (active) Peers.activePeers else if (peerState == BrsApi.PeerState.PeerState_UNSET) Peers.allPeers else Peers.getPeers(Peer.State.fromProtobuf(peerState))) {
            peers.addPeerAddresses(peer.announcedAddress)
        }
        return peers.build()
    }
}
