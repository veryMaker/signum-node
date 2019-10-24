package brs.grpc.handlers

import brs.DependencyProvider
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.grpc.proto.toByteString

class GetTransactionBytesHandler(private val dp: DependencyProvider) : GrpcApiHandler<BrsApi.BasicTransaction, BrsApi.TransactionBytes> {

    override fun handleRequest(request: BrsApi.BasicTransaction): BrsApi.TransactionBytes {
        return BrsApi.TransactionBytes.newBuilder()
                .setTransactionBytes(ProtoBuilder.parseBasicTransaction(dp, request).bytes.toByteString())
                .build()
    }
}
