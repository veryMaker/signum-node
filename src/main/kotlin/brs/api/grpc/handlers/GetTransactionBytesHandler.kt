package brs.api.grpc.handlers

import brs.entity.DependencyProvider
import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ProtoBuilder
import brs.api.grpc.service.toByteString

class GetTransactionBytesHandler(private val dp: DependencyProvider) :
    GrpcApiHandler<BrsApi.BasicTransaction, BrsApi.TransactionBytes> {

    override fun handleRequest(request: BrsApi.BasicTransaction): BrsApi.TransactionBytes {
        return BrsApi.TransactionBytes.newBuilder()
            .setTransactionBytes(ProtoBuilder.parseBasicTransaction(dp, request).toBytes().toByteString())
            .build()
    }
}
