package brs.api.grpc.peer

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider
import brs.peer.Peer
import com.google.protobuf.Empty

internal class GetPeersHandler(private val dp: DependencyProvider) : GrpcApiHandler<Empty, PeerApi.Peers> {
    override fun handleRequest(request: Empty): PeerApi.Peers {
        return PeerApi.Peers.newBuilder()
            .addAllAnnouncedAddresses(dp.peerService.allPeers
                .filter { !it.isBlacklisted && it.state == Peer.State.CONNECTED && it.shareAddress }
                .map { it.address.toString() })
            .build()
    }
}
