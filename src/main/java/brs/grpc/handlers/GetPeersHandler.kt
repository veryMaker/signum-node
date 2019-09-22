package brs.grpc.handlers

import brs.DependencyProvider
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.peer.Peer

class GetPeersHandler(private val dp: DependencyProvider) : GrpcApiHandler<BrsApi.GetPeersRequest, BrsApi.Peers> {
    override fun handleRequest(request: BrsApi.GetPeersRequest): BrsApi.Peers {
        val active = request.active
        val peerState = request.state
        val peers = BrsApi.Peers.newBuilder()
        for (peer in if (active) dp.peers.activePeers else if (peerState == BrsApi.PeerState.PeerState_UNSET) dp.peers.allPeers else dp.peers.getPeers(Peer.State.fromProtobuf(peerState) ?: throw ApiException("Peer State Invalid"))) {
            peers.addPeerAddresses(peer.announcedAddress)
        }
        return peers.build()
    }
}
