package brs.grpc.handlers;

import brs.DependencyProvider;
import brs.Transaction;
import brs.TransactionProcessor;
import brs.grpc.GrpcApiHandler;
import brs.grpc.proto.BrsApi;

public class BroadcastTransactionBytesHandler implements GrpcApiHandler<BrsApi.TransactionBytes, BrsApi.TransactionBroadcastResult> {

    private final DependencyProvider dp;

    public BroadcastTransactionBytesHandler(DependencyProvider dp) {
        this.dp = dp;
    }

    @Override
    public BrsApi.TransactionBroadcastResult handleRequest(BrsApi.TransactionBytes transactionBytes) throws Exception {
        return BrsApi.TransactionBroadcastResult.newBuilder()
                .setNumberOfPeersSentTo(dp.transactionProcessor.broadcast(Transaction.parseTransaction(dp, transactionBytes.getTransactionBytes().toByteArray())))
                .build();
    }
}
