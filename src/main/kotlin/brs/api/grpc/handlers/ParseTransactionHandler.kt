package brs.api.grpc.handlers

import brs.entity.DependencyProvider
import brs.entity.Transaction
import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ProtoBuilder

class ParseTransactionHandler(private val dp: DependencyProvider) :
    GrpcApiHandler<BrsApi.TransactionBytes, BrsApi.BasicTransaction> {

    override fun handleRequest(transactionBytes: BrsApi.TransactionBytes): BrsApi.BasicTransaction {
        return ProtoBuilder.buildBasicTransaction(
            Transaction.parseTransaction(
                dp,
                transactionBytes.transactionBytes.toByteArray()
            )
        )
    }
}
