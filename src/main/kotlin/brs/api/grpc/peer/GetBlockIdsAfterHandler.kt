package brs.api.grpc.peer

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider

internal class GetBlockIdsAfterHandler(private val dp: DependencyProvider) : GrpcApiHandler<PeerApi.GetBlocksAfterRequest, PeerApi.BlockIds> {
    override fun handleRequest(request: PeerApi.GetBlocksAfterRequest): PeerApi.BlockIds {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
