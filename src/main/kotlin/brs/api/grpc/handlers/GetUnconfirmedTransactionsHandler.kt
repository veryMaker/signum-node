package brs.api.grpc.handlers

import brs.services.TransactionProcessorService
import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ProtoBuilder
import brs.services.IndirectIncomingService

class GetUnconfirmedTransactionsHandler(
    private val indirectIncomingService: IndirectIncomingService,
    private val transactionProcessorService: TransactionProcessorService
) : GrpcApiHandler<BrsApi.GetAccountRequest, BrsApi.UnconfirmedTransactions> {
    override fun handleRequest(getAccountRequest: BrsApi.GetAccountRequest): BrsApi.UnconfirmedTransactions {
        return BrsApi.UnconfirmedTransactions.newBuilder()
            .addAllUnconfirmedTransactions(transactionProcessorService.allUnconfirmedTransactions
                .asSequence()
                .filter { transaction ->
                    (getAccountRequest.accountId == 0L
                            || getAccountRequest.accountId == transaction.senderId
                            || getAccountRequest.accountId == transaction.recipientId
                            || indirectIncomingService.isIndirectlyReceiving(
                        transaction,
                        getAccountRequest.accountId
                    ))
                }
                .map { ProtoBuilder.buildUnconfirmedTransaction(it) }
                .toList())
            .build()
    }
}
