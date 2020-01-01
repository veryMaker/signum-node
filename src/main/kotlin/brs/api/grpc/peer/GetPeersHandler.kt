package brs.api.grpc.peer

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider
import com.google.protobuf.Empty

internal class GetPeersHandler(private val dp: DependencyProvider) : GrpcApiHandler<Empty, PeerApi.Peers> {
    override fun handleRequest(request: Empty): PeerApi.Peers {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
