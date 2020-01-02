package brs.api.grpc.api

import brs.api.grpc.ApiException
import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.entity.DependencyProvider
import brs.peer.Peer

class GetPeersHandler(private val dp: DependencyProvider) : GrpcApiHandler<BrsApi.GetPeersRequest, BrsApi.Peers> {
    override fun handleRequest(request: BrsApi.GetPeersRequest): BrsApi.Peers {
        val active = request.active
        val peerState = request.state
        val peers = BrsApi.Peers.newBuilder()
        val peerList = when {
            active -> dp.peerService.activePeers
            peerState == BrsApi.PeerState.PeerState_UNSET -> dp.peerService.allPeers
            else -> dp.peerService.getPeers(
                Peer.State.fromProtobuf(peerState) ?: throw ApiException(
                    "Peer State Invalid"
                )
            )
        }
        for (peer in peerList) {
            peers.addPeerAddresses(peer.announcedAddress)
        }
        return peers.build()
    }
}
