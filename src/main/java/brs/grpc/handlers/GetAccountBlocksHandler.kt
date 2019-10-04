package brs.grpc.handlers

import brs.Blockchain
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.AccountService
import brs.services.BlockService

class GetAccountBlocksHandler(private val blockchain: Blockchain, private val blockService: BlockService, private val accountService: AccountService) : GrpcApiHandler<BrsApi.GetAccountBlocksRequest, BrsApi.Blocks> {
    override suspend fun handleRequest(getAccountRequest: BrsApi.GetAccountBlocksRequest): BrsApi.Blocks {
        val accountId = getAccountRequest.accountId
        val timestamp = getAccountRequest.timestamp
        val includeTransactions = getAccountRequest.includeTransactions

        val indexRange = ProtoBuilder.sanitizeIndexRange(getAccountRequest.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex

        val account = accountService.getAccount(accountId) ?: throw ApiException("Could not find account")

        return BrsApi.Blocks.newBuilder()
                .addAllBlocks(blockchain.getBlocks(account, timestamp, firstIndex, lastIndex).map { block -> ProtoBuilder.buildBlock(blockchain, blockService, block, includeTransactions) })
                .build()
    }
}
