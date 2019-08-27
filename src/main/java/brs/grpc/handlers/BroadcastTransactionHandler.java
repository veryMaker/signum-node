package brs.grpc.handlers;

import brs.Blockchain;
import brs.DependencyProvider;
import brs.TransactionProcessor;
import brs.grpc.GrpcApiHandler;
import brs.grpc.proto.BrsApi;
import brs.grpc.proto.ProtoBuilder;

public class BroadcastTransactionHandler implements GrpcApiHandler<BrsApi.BasicTransaction, BrsApi.TransactionBroadcastResult> {

    private final DependencyProvider dp;

    public BroadcastTransactionHandler(DependencyProvider dp) {
        this.dp = dp;
    }

    @Override
    public BrsApi.TransactionBroadcastResult handleRequest(BrsApi.BasicTransaction basicTransaction) throws Exception {
        return BrsApi.TransactionBroadcastResult.newBuilder()
                .setNumberOfPeersSentTo(dp.transactionProcessor.broadcast(ProtoBuilder.parseBasicTransaction(dp, basicTransaction)))
                .build();
    }
}
