package brs.api.grpc.handlers

import brs.services.BlockchainService
import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.ApiException
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.proto.ProtoBuilder
import brs.services.AccountService

class GetAccountTransactionsHandler(private val blockchainService: BlockchainService, private val accountService: AccountService) : GrpcApiHandler<BrsApi.GetAccountTransactionsRequest, BrsApi.Transactions> {

    override fun handleRequest(request: BrsApi.GetAccountTransactionsRequest): BrsApi.Transactions {
        val accountId = request.accountId
        val timestamp = request.timestamp
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex
        val numberOfConfirmations = request.confirmations
        val type = (if (request.filterByType) request.type else -1).toByte()
        val subtype = (if (request.filterByType) request.subtype else -1).toByte()

        val account = accountService.getAccount(accountId) ?: throw ApiException("Could not find account")

        val builder = BrsApi.Transactions.newBuilder()

        val currentHeight = blockchainService.height
        blockchainService.getTransactions(account, numberOfConfirmations, type, subtype, timestamp, firstIndex, lastIndex, true)
                .forEach { transaction -> builder.addTransactions(ProtoBuilder.buildTransaction(transaction, currentHeight)) }

        return builder.build()
    }
}
