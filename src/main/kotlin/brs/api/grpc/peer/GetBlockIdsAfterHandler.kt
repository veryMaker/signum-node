package brs.api.grpc.peer

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider

internal class GetBlockIdsAfterHandler(private val dp: DependencyProvider) : GrpcApiHandler<PeerApi.GetBlocksAfterRequest, PeerApi.BlockIds> {
    override fun handleRequest(request: PeerApi.GetBlocksAfterRequest): PeerApi.BlockIds {
        require(request.blockId != 0L)
        return PeerApi.BlockIds.newBuilder()
            .addAllBlockIds(dp.blockchainService.getBlocksAfter(request.blockId, 100).map { it.id })
            .build()
    }
}
