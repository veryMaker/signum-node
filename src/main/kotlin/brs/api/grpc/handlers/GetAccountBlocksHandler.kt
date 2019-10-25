package brs.api.grpc.handlers

import brs.services.BlockchainService
import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.ApiException
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.proto.ProtoBuilder
import brs.services.AccountService
import brs.services.BlockService

class GetAccountBlocksHandler(private val blockchainService: BlockchainService, private val blockService: BlockService, private val accountService: AccountService) : GrpcApiHandler<BrsApi.GetAccountBlocksRequest, BrsApi.Blocks> {
    override fun handleRequest(getAccountRequest: BrsApi.GetAccountBlocksRequest): BrsApi.Blocks {
        val accountId = getAccountRequest.accountId
        val timestamp = getAccountRequest.timestamp
        val includeTransactions = getAccountRequest.includeTransactions

        val indexRange = ProtoBuilder.sanitizeIndexRange(getAccountRequest.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex

        val account = accountService.getAccount(accountId) ?: throw ApiException("Could not find account")

        return BrsApi.Blocks.newBuilder()
                .addAllBlocks(blockchainService.getBlocks(account, timestamp, firstIndex, lastIndex).map { block -> ProtoBuilder.buildBlock(blockchainService, blockService, block, includeTransactions) })
                .build()
    }
}
