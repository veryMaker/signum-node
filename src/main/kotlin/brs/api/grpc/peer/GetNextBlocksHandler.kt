package brs.api.grpc.peer

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.ProtoBuilder
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider

internal class GetNextBlocksHandler(private val dp: DependencyProvider) : GrpcApiHandler<PeerApi.GetBlocksAfterRequest, PeerApi.RawBlocks> {
    override fun handleRequest(request: PeerApi.GetBlocksAfterRequest): PeerApi.RawBlocks {
        require(request.blockId != 0L)
        return PeerApi.RawBlocks.newBuilder()
            .addAllBlocks(dp.blockchainService.getBlocksAfter(request.blockId, 100).map { ProtoBuilder.buidRawBlock(it) })
            .build()
    }
}
