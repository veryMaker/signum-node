package brs.api.grpc.handlers

import brs.services.BlockchainService
import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ProtoBuilder
import brs.services.BlockService

class GetBlocksHandler(private val blockchainService: BlockchainService, private val blockService: BlockService) : GrpcApiHandler<BrsApi.GetBlocksRequest, BrsApi.Blocks> {

    override fun handleRequest(request: BrsApi.GetBlocksRequest): BrsApi.Blocks {
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex
        val includeTransactions = request.includeTransactions
        val builder = BrsApi.Blocks.newBuilder()
        blockchainService.getBlocks(firstIndex, lastIndex)
                .forEach { block -> builder.addBlocks(ProtoBuilder.buildBlock(blockchainService, blockService, block, includeTransactions)) }
        return builder.build()
    }
}
