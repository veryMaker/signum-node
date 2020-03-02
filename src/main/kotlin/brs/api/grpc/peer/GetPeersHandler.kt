package brs.api.grpc.peer

import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider
import brs.peer.Peer
import com.google.protobuf.Empty

internal class GetPeersHandler(private val dp: DependencyProvider) : GrpcPeerApiHandler<Empty, PeerApi.Peers>(dp) {
    override fun handleRequest(peer: Peer, request: Empty): PeerApi.Peers {
        return PeerApi.Peers.newBuilder()
            .addAllAddresses(dp.peerService.allPeers
                .filter { it != peer && !it.isBlacklisted && it.isConnected && it.shareAddress }
                .map { it.address.toString() })
            .build()
    }
}
