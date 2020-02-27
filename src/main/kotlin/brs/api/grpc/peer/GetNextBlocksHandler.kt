package brs.api.grpc.peer

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.ProtoBuilder
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider
import brs.objects.Constants

internal class GetNextBlocksHandler(private val dp: DependencyProvider) : GrpcApiHandler<PeerApi.GetBlocksAfterRequest, PeerApi.RawBlocks> {
    override fun handleRequest(request: PeerApi.GetBlocksAfterRequest): PeerApi.RawBlocks {
        require(request.blockId != 0L)
        var size = 0L
        return PeerApi.RawBlocks.newBuilder()
            .addAllBlocks(dp.blockchainService.getBlocksAfter(request.blockId, 100)
                .filter {
                    size += Constants.BLOCK_HEADER_LENGTH + it.payloadLength
                    size <= 2097152
                }
                .map { ProtoBuilder.buildRawBlock(it) })
            .build()
    }
}
