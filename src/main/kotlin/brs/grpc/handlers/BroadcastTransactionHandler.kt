package brs.grpc.handlers

import brs.DependencyProvider
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder

class BroadcastTransactionHandler(private val dp: DependencyProvider) : GrpcApiHandler<BrsApi.BasicTransaction, BrsApi.TransactionBroadcastResult> {

    override fun handleRequest(basicTransaction: BrsApi.BasicTransaction): BrsApi.TransactionBroadcastResult {
        return BrsApi.TransactionBroadcastResult.newBuilder()
                .setNumberOfPeersSentTo(dp.transactionProcessor.broadcast(ProtoBuilder.parseBasicTransaction(dp, basicTransaction))!!)
                .build()
    }
}
