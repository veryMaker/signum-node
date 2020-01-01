package brs.api.grpc.peer

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider
import com.google.protobuf.Empty

internal class AddPeersHandler(private val dp: DependencyProvider) : GrpcApiHandler<PeerApi.Peers, Empty> {
    override fun handleRequest(request: PeerApi.Peers): Empty {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
