package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ApiException
import brs.api.grpc.service.ProtoBuilder
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
        if (blockId > 0) {
            try {
                block = blockchainService.getBlock(blockId)
            } catch (e: Exception) {
                throw ApiException("Incorrect Block ID")
            }

        } else if (blockHeight > 0) {
            try {
                if (blockHeight > blockchainService.height) {
                    throw ApiException("Incorrect Block Height")
                }
                block = blockchainService.getBlockAtHeight(blockHeight)
            } catch (e: Exception) {
                throw ApiException("Incorrect Block Height")
            }

        } else if (timestamp > 0) {
            try {
                block = blockchainService.getLastBlock(timestamp)
            } catch (e: Exception) {
                throw ApiException("Incorrect Timestamp")
            }

        } else {
            block = blockchainService.lastBlock
        }

        if (block == null) {
            throw ApiException("Unknown Block")
        }

        val includeTransactions = request.includeTransactions

        return ProtoBuilder.buildBlock(blockchainService, blockService, block, includeTransactions)
    }
}
