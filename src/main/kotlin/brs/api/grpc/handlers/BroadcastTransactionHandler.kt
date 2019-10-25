package brs.api.grpc.handlers

import brs.entity.DependencyProvider
import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ProtoBuilder

class BroadcastTransactionHandler(private val dp: DependencyProvider) : GrpcApiHandler<BrsApi.BasicTransaction, BrsApi.TransactionBroadcastResult> {

    override fun handleRequest(basicTransaction: BrsApi.BasicTransaction): BrsApi.TransactionBroadcastResult {
        return BrsApi.TransactionBroadcastResult.newBuilder()
                .setNumberOfPeersSentTo(dp.transactionProcessorService.broadcast(ProtoBuilder.parseBasicTransaction(dp, basicTransaction))!!)
                .build()
    }
}
