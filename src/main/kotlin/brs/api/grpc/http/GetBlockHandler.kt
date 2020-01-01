package brs.api.grpc.http

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.ApiException
import brs.api.grpc.ProtoBuilder
import brs.entity.Block
import brs.services.BlockService
import brs.services.BlockchainService

class GetBlockHandler(private val blockchainService: BlockchainService, private val blockService: BlockService) :
    GrpcApiHandler<BrsApi.GetBlockRequest, BrsApi.Block> {
    override fun handleRequest(request: BrsApi.GetBlockRequest): BrsApi.Block {
        val blockId = request.blockId
        val blockHeight = request.height
        val timestamp = request.timestamp

        val block: Block?
        when {
            blockId > 0 -> {
                try {
                    block = blockchainService.getBlock(blockId)
                } catch (e: Exception) {
                    throw ApiException("Incorrect Block ID")
                }
            }
            blockHeight > 0 -> {
                try {
                    if (blockHeight > blockchainService.height) {
                        throw ApiException("Incorrect Block Height")
                    }
                    block = blockchainService.getBlockAtHeight(blockHeight)
                } catch (e: Exception) {
                    throw ApiException("Incorrect Block Height")
                }
            }
            timestamp > 0 -> {
                try {
                    block = blockchainService.getLastBlock(timestamp)
                } catch (e: Exception) {
                    throw ApiException("Incorrect Timestamp")
                }
            }
            else -> {
                block = blockchainService.lastBlock
            }
        }

        if (block == null) {
            throw ApiException("Unknown Block")
        }

        val includeTransactions = request.includeTransactions

        return ProtoBuilder.buildBlock(blockchainService, blockService, block, includeTransactions)
    }
}
