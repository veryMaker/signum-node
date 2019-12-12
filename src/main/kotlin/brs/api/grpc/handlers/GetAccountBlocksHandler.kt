package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ApiException
import brs.api.grpc.service.ProtoBuilder
import brs.services.AccountService
import brs.services.BlockService
import brs.services.BlockchainService

class GetAccountBlocksHandler(
    private val blockchainService: BlockchainService,
    private val blockService: BlockService,
    private val accountService: AccountService
) : GrpcApiHandler<BrsApi.GetAccountBlocksRequest, BrsApi.Blocks> {
    override fun handleRequest(request: BrsApi.GetAccountBlocksRequest): BrsApi.Blocks {
        val accountId = request.accountId
        val timestamp = request.timestamp
        val includeTransactions = request.includeTransactions

        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex

        val account = accountService.getAccount(accountId) ?: throw ApiException("Could not find account")

        return BrsApi.Blocks.newBuilder()
            .addAllBlocks(
                blockchainService.getBlocks(
                    account,
                    timestamp,
                    firstIndex,
                    lastIndex
                ).map { block -> ProtoBuilder.buildBlock(blockchainService, blockService, block, includeTransactions) })
            .build()
    }
}
