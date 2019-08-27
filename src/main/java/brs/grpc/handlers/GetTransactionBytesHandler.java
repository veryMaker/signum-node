package brs.grpc.handlers;

import brs.Blockchain;
import brs.DependencyProvider;
import brs.grpc.GrpcApiHandler;
import brs.grpc.proto.BrsApi;
import brs.grpc.proto.ProtoBuilder;
import com.google.protobuf.ByteString;

public class GetTransactionBytesHandler implements GrpcApiHandler<BrsApi.BasicTransaction, BrsApi.TransactionBytes> {

    private final DependencyProvider dp;

    public GetTransactionBytesHandler(DependencyProvider dp) {
        this.dp = dp;
    }

    @Override
    public BrsApi.TransactionBytes handleRequest(BrsApi.BasicTransaction basicTransaction) throws Exception {
        return BrsApi.TransactionBytes.newBuilder()
                .setTransactionBytes(ByteString.copyFrom(ProtoBuilder.parseBasicTransaction(dp, basicTransaction).getBytes()))
                .build();
    }
}
