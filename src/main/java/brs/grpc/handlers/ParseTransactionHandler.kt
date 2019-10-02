package brs.grpc.handlers

import brs.DependencyProvider
import brs.Transaction
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder

class ParseTransactionHandler(private val dp: DependencyProvider) : GrpcApiHandler<BrsApi.TransactionBytes, BrsApi.BasicTransaction> {

    override suspend fun handleRequest(transactionBytes: BrsApi.TransactionBytes): BrsApi.BasicTransaction {
        return ProtoBuilder.buildBasicTransaction(Transaction.parseTransaction(dp, transactionBytes.transactionBytes.toByteArray()))
    }
}
