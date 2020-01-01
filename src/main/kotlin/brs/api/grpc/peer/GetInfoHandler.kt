package brs.api.grpc.peer

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider

internal class GetInfoHandler(private val dp: DependencyProvider) : GrpcApiHandler<PeerApi.PeerInfo, PeerApi.PeerInfo> {
    override fun handleRequest(request: PeerApi.PeerInfo): PeerApi.PeerInfo {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
