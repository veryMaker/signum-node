package brs.grpc.handlers;

import brs.DependencyProvider;
import brs.Transaction;
import brs.grpc.GrpcApiHandler;
import brs.grpc.proto.BrsApi;
import brs.grpc.proto.ProtoBuilder;

public class ParseTransactionHandler implements GrpcApiHandler<BrsApi.TransactionBytes, BrsApi.BasicTransaction> {
    private final DependencyProvider dp;

    public ParseTransactionHandler(DependencyProvider dp) {
        this.dp = dp;
    }

    @Override
    public BrsApi.BasicTransaction handleRequest(BrsApi.TransactionBytes transactionBytes) throws Exception {
        return ProtoBuilder.buildBasicTransaction(Transaction.parseTransaction(dp, transactionBytes.getTransactionBytes().toByteArray()));
    }
}
