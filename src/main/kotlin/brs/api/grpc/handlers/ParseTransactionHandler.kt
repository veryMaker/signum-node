package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ProtoBuilder
import brs.entity.DependencyProvider
import brs.entity.Transaction

class ParseTransactionHandler(private val dp: DependencyProvider) :
    GrpcApiHandler<BrsApi.TransactionBytes, BrsApi.BasicTransaction> {
    override fun handleRequest(request: BrsApi.TransactionBytes): BrsApi.BasicTransaction {
        return ProtoBuilder.buildBasicTransaction(
            Transaction.parseTransaction(
                dp,
                request.transactionBytes.toByteArray()
            )
        )
    }
}
