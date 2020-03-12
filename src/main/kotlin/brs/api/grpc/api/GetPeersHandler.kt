package brs.api.grpc.api

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.entity.DependencyProvider

class GetPeersHandler(private val dp: DependencyProvider) : GrpcApiHandler<BrsApi.GetPeersRequest, BrsApi.Peers> {
    override fun handleRequest(request: BrsApi.GetPeersRequest): BrsApi.Peers {
        val peers = BrsApi.Peers.newBuilder()
        for (peer in if (request.allPeers) dp.peerService.allPeers else dp.peerService.getPeers(request.isConnected)) {
            peers.addPeerAddresses(peer.announcedAddress.toString())
        }
        return peers.build()
    }
}
