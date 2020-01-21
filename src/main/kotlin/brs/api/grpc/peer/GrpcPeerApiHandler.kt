package brs.api.grpc.peer

import brs.api.grpc.GrpcApiHandler
import brs.entity.DependencyProvider
import brs.peer.Peer
import com.google.protobuf.Message

internal abstract class GrpcPeerApiHandler<R : Message, S : Message>(private val dp: DependencyProvider) : GrpcApiHandler<R, S> {
    final override fun handleRequest(request: R): S {
        val peer = dp.peerService.getOrAddPeer("grpc://${PeerApiContextKeys.REMOTE_ADDRESS.get()}")
        return handleRequest(peer, request)
    }

    internal abstract fun handleRequest(peer: Peer, request: R): S
}
