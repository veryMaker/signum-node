package brs.grpc.handlers

import brs.Block
import brs.Blockchain
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.BlockService

class GetBlockHandler(private val blockchain: Blockchain, private val blockService: BlockService) : GrpcApiHandler<BrsApi.GetBlockRequest, BrsApi.Block> {

    @Throws(Exception::class)
    override fun handleRequest(request: BrsApi.GetBlockRequest): BrsApi.Block {
        val blockId = request.blockId
        val blockHeight = request.height
        val timestamp = request.timestamp

        val block: Block?
        if (blockId > 0) {
            try {
                block = blockchain.getBlock(blockId)
            } catch (e: RuntimeException) {
                throw ApiException("Incorrect Block ID")
            }

        } else if (blockHeight > 0) {
            try {
                if (blockHeight > blockchain.height) {
                    throw ApiException("Incorrect Block Height")
                }
                block = blockchain.getBlockAtHeight(blockHeight)
            } catch (e: RuntimeException) {
                throw ApiException("Incorrect Block Height")
            }

        } else if (timestamp > 0) {
            try {
                block = blockchain.getLastBlock(timestamp)
            } catch (e: RuntimeException) {
                throw ApiException("Incorrect Timestamp")
            }

        } else {
            block = blockchain.lastBlock
        }

        if (block == null) {
            throw ApiException("Unknown Block")
        }

        val includeTransactions = request.includeTransactions

        return ProtoBuilder.buildBlock(blockchain, blockService, block, includeTransactions)
    }
}
