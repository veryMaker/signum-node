package brs.api.grpc.peer

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider

internal class GetBlocksAfterHandler(private val dp: DependencyProvider) : GrpcApiHandler<PeerApi.GetBlocksAfterRequest, PeerApi.RawBlocks> {
    override fun handleRequest(request: PeerApi.GetBlocksAfterRequest): PeerApi.RawBlocks {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
