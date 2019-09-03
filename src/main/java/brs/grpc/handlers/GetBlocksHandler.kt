package brs.grpc.handlers

import brs.Blockchain
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.BlockService

class GetBlocksHandler(private val blockchain: Blockchain, private val blockService: BlockService) : GrpcApiHandler<BrsApi.GetBlocksRequest, BrsApi.Blocks> {

    @Throws(Exception::class)
    override fun handleRequest(request: BrsApi.GetBlocksRequest): BrsApi.Blocks {
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex
        val includeTransactions = request.includeTransactions
        val builder = BrsApi.Blocks.newBuilder()
        blockchain.getBlocks(firstIndex, lastIndex)
                .forEach { block -> builder.addBlocks(ProtoBuilder.buildBlock(blockchain, blockService, block, includeTransactions)) }
        return builder.build()
    }
}
