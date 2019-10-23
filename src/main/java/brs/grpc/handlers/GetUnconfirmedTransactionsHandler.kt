package brs.grpc.handlers

import brs.TransactionProcessor
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.IndirectIncomingService

class GetUnconfirmedTransactionsHandler(private val indirectIncomingService: IndirectIncomingService, private val transactionProcessor: TransactionProcessor) : GrpcApiHandler<BrsApi.GetAccountRequest, BrsApi.UnconfirmedTransactions> {
    override suspend fun handleRequest(getAccountRequest: BrsApi.GetAccountRequest): BrsApi.UnconfirmedTransactions {
        return BrsApi.UnconfirmedTransactions.newBuilder()
            .addAllUnconfirmedTransactions(transactionProcessor.allUnconfirmedTransactions
                .asSequence()
                .filter { transaction ->
                    (getAccountRequest.accountId == 0L
                            || getAccountRequest.accountId == transaction.senderId
                            || getAccountRequest.accountId == transaction.recipientId
                            || indirectIncomingService.isIndirectlyReceiving(transaction, getAccountRequest.accountId))
                }
                .map { ProtoBuilder.buildUnconfirmedTransaction(it) }
                .toList())
            .build()
    }
}
