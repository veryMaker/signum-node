package brs.api.grpc.http

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.ProtoBuilder
import brs.entity.DependencyProvider

class GetUnconfirmedTransactionsHandler(private val dp: DependencyProvider) :
    GrpcApiHandler<BrsApi.GetAccountRequest, BrsApi.UnconfirmedTransactions> {
    override fun handleRequest(request: BrsApi.GetAccountRequest): BrsApi.UnconfirmedTransactions {
        return BrsApi.UnconfirmedTransactions.newBuilder()
            .addAllUnconfirmedTransactions(dp.unconfirmedTransactionService.all
                .asSequence()
                .filter { transaction ->
                    (request.accountId == 0L
                            || request.accountId == transaction.senderId
                            || request.accountId == transaction.recipientId
                            || dp.indirectIncomingService.isIndirectlyReceiving(
                        transaction,
                        request.accountId
                    ))
                }
                .map { ProtoBuilder.buildUnconfirmedTransaction(it) }
                .toList())
            .build()
    }
}
