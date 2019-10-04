package brs.grpc.handlers

import brs.DependencyProvider
import brs.Transaction
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi

class BroadcastTransactionBytesHandler(private val dp: DependencyProvider) : GrpcApiHandler<BrsApi.TransactionBytes, BrsApi.TransactionBroadcastResult> {
    override suspend fun handleRequest(transactionBytes: BrsApi.TransactionBytes): BrsApi.TransactionBroadcastResult {
        return BrsApi.TransactionBroadcastResult.newBuilder()
                .setNumberOfPeersSentTo(dp.transactionProcessor.broadcast(Transaction.parseTransaction(dp, transactionBytes.transactionBytes.toByteArray()))!!)
                .build()
    }
}
