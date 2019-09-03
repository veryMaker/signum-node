package brs.grpc.handlers

import brs.Blockchain
import brs.DependencyProvider
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import com.google.protobuf.ByteString

class GetTransactionBytesHandler(private val dp: DependencyProvider) : GrpcApiHandler<BrsApi.BasicTransaction, BrsApi.TransactionBytes> {

    @Throws(Exception::class)
    override fun handleRequest(basicTransaction: BrsApi.BasicTransaction): BrsApi.TransactionBytes {
        return BrsApi.TransactionBytes.newBuilder()
                .setTransactionBytes(ByteString.copyFrom(ProtoBuilder.parseBasicTransaction(dp, basicTransaction).bytes))
                .build()
    }
}
