package brs.api.grpc.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.26.0)",
    comments = "Source: brsApi.proto")
public final class BrsApiServiceGrpc {

  private BrsApiServiceGrpc() {}

  public static final String SERVICE_NAME = "brs.api.BrsApiService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.BasicTransaction,
      brs.api.grpc.proto.BrsApi.TransactionBroadcastResult> getBroadcastTransactionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BroadcastTransaction",
      requestType = brs.api.grpc.proto.BrsApi.BasicTransaction.class,
      responseType = brs.api.grpc.proto.BrsApi.TransactionBroadcastResult.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.BasicTransaction,
      brs.api.grpc.proto.BrsApi.TransactionBroadcastResult> getBroadcastTransactionMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.BasicTransaction, brs.api.grpc.proto.BrsApi.TransactionBroadcastResult> getBroadcastTransactionMethod;
    if ((getBroadcastTransactionMethod = BrsApiServiceGrpc.getBroadcastTransactionMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getBroadcastTransactionMethod = BrsApiServiceGrpc.getBroadcastTransactionMethod) == null) {
          BrsApiServiceGrpc.getBroadcastTransactionMethod = getBroadcastTransactionMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.BasicTransaction, brs.api.grpc.proto.BrsApi.TransactionBroadcastResult>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BroadcastTransaction"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.BasicTransaction.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.TransactionBroadcastResult.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("BroadcastTransaction"))
              .build();
        }
      }
    }
    return getBroadcastTransactionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.TransactionBytes,
      brs.api.grpc.proto.BrsApi.TransactionBroadcastResult> getBroadcastTransactionBytesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BroadcastTransactionBytes",
      requestType = brs.api.grpc.proto.BrsApi.TransactionBytes.class,
      responseType = brs.api.grpc.proto.BrsApi.TransactionBroadcastResult.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.TransactionBytes,
      brs.api.grpc.proto.BrsApi.TransactionBroadcastResult> getBroadcastTransactionBytesMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.TransactionBytes, brs.api.grpc.proto.BrsApi.TransactionBroadcastResult> getBroadcastTransactionBytesMethod;
    if ((getBroadcastTransactionBytesMethod = BrsApiServiceGrpc.getBroadcastTransactionBytesMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getBroadcastTransactionBytesMethod = BrsApiServiceGrpc.getBroadcastTransactionBytesMethod) == null) {
          BrsApiServiceGrpc.getBroadcastTransactionBytesMethod = getBroadcastTransactionBytesMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.TransactionBytes, brs.api.grpc.proto.BrsApi.TransactionBroadcastResult>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BroadcastTransactionBytes"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.TransactionBytes.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.TransactionBroadcastResult.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("BroadcastTransactionBytes"))
              .build();
        }
      }
    }
    return getBroadcastTransactionBytesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.BasicTransaction,
      brs.api.grpc.proto.BrsApi.BasicTransaction> getCompleteBasicTransactionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CompleteBasicTransaction",
      requestType = brs.api.grpc.proto.BrsApi.BasicTransaction.class,
      responseType = brs.api.grpc.proto.BrsApi.BasicTransaction.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.BasicTransaction,
      brs.api.grpc.proto.BrsApi.BasicTransaction> getCompleteBasicTransactionMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.BasicTransaction, brs.api.grpc.proto.BrsApi.BasicTransaction> getCompleteBasicTransactionMethod;
    if ((getCompleteBasicTransactionMethod = BrsApiServiceGrpc.getCompleteBasicTransactionMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getCompleteBasicTransactionMethod = BrsApiServiceGrpc.getCompleteBasicTransactionMethod) == null) {
          BrsApiServiceGrpc.getCompleteBasicTransactionMethod = getCompleteBasicTransactionMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.BasicTransaction, brs.api.grpc.proto.BrsApi.BasicTransaction>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CompleteBasicTransaction"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.BasicTransaction.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.BasicTransaction.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("CompleteBasicTransaction"))
              .build();
        }
      }
    }
    return getCompleteBasicTransactionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest,
      brs.api.grpc.proto.BrsApi.Account> getGetAccountMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAccount",
      requestType = brs.api.grpc.proto.BrsApi.GetAccountRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Account.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest,
      brs.api.grpc.proto.BrsApi.Account> getGetAccountMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest, brs.api.grpc.proto.BrsApi.Account> getGetAccountMethod;
    if ((getGetAccountMethod = BrsApiServiceGrpc.getGetAccountMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetAccountMethod = BrsApiServiceGrpc.getGetAccountMethod) == null) {
          BrsApiServiceGrpc.getGetAccountMethod = getGetAccountMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetAccountRequest, brs.api.grpc.proto.BrsApi.Account>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAccount"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetAccountRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Account.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetAccount"))
              .build();
        }
      }
    }
    return getGetAccountMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest,
      brs.api.grpc.proto.BrsApi.AccountATs> getGetAccountATsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAccountATs",
      requestType = brs.api.grpc.proto.BrsApi.GetAccountRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.AccountATs.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest,
      brs.api.grpc.proto.BrsApi.AccountATs> getGetAccountATsMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest, brs.api.grpc.proto.BrsApi.AccountATs> getGetAccountATsMethod;
    if ((getGetAccountATsMethod = BrsApiServiceGrpc.getGetAccountATsMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetAccountATsMethod = BrsApiServiceGrpc.getGetAccountATsMethod) == null) {
          BrsApiServiceGrpc.getGetAccountATsMethod = getGetAccountATsMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetAccountRequest, brs.api.grpc.proto.BrsApi.AccountATs>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAccountATs"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetAccountRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.AccountATs.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetAccountATs"))
              .build();
        }
      }
    }
    return getGetAccountATsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountBlocksRequest,
      brs.api.grpc.proto.BrsApi.Blocks> getGetAccountBlocksMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAccountBlocks",
      requestType = brs.api.grpc.proto.BrsApi.GetAccountBlocksRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Blocks.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountBlocksRequest,
      brs.api.grpc.proto.BrsApi.Blocks> getGetAccountBlocksMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountBlocksRequest, brs.api.grpc.proto.BrsApi.Blocks> getGetAccountBlocksMethod;
    if ((getGetAccountBlocksMethod = BrsApiServiceGrpc.getGetAccountBlocksMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetAccountBlocksMethod = BrsApiServiceGrpc.getGetAccountBlocksMethod) == null) {
          BrsApiServiceGrpc.getGetAccountBlocksMethod = getGetAccountBlocksMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetAccountBlocksRequest, brs.api.grpc.proto.BrsApi.Blocks>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAccountBlocks"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetAccountBlocksRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Blocks.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetAccountBlocks"))
              .build();
        }
      }
    }
    return getGetAccountBlocksMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountOrdersRequest,
      brs.api.grpc.proto.BrsApi.Orders> getGetAccountCurrentOrdersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAccountCurrentOrders",
      requestType = brs.api.grpc.proto.BrsApi.GetAccountOrdersRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Orders.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountOrdersRequest,
      brs.api.grpc.proto.BrsApi.Orders> getGetAccountCurrentOrdersMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountOrdersRequest, brs.api.grpc.proto.BrsApi.Orders> getGetAccountCurrentOrdersMethod;
    if ((getGetAccountCurrentOrdersMethod = BrsApiServiceGrpc.getGetAccountCurrentOrdersMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetAccountCurrentOrdersMethod = BrsApiServiceGrpc.getGetAccountCurrentOrdersMethod) == null) {
          BrsApiServiceGrpc.getGetAccountCurrentOrdersMethod = getGetAccountCurrentOrdersMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetAccountOrdersRequest, brs.api.grpc.proto.BrsApi.Orders>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAccountCurrentOrders"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetAccountOrdersRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Orders.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetAccountCurrentOrders"))
              .build();
        }
      }
    }
    return getGetAccountCurrentOrdersMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest,
      brs.api.grpc.proto.BrsApi.EscrowTransactions> getGetAccountEscrowTransactionsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAccountEscrowTransactions",
      requestType = brs.api.grpc.proto.BrsApi.GetAccountRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.EscrowTransactions.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest,
      brs.api.grpc.proto.BrsApi.EscrowTransactions> getGetAccountEscrowTransactionsMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest, brs.api.grpc.proto.BrsApi.EscrowTransactions> getGetAccountEscrowTransactionsMethod;
    if ((getGetAccountEscrowTransactionsMethod = BrsApiServiceGrpc.getGetAccountEscrowTransactionsMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetAccountEscrowTransactionsMethod = BrsApiServiceGrpc.getGetAccountEscrowTransactionsMethod) == null) {
          BrsApiServiceGrpc.getGetAccountEscrowTransactionsMethod = getGetAccountEscrowTransactionsMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetAccountRequest, brs.api.grpc.proto.BrsApi.EscrowTransactions>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAccountEscrowTransactions"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetAccountRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.EscrowTransactions.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetAccountEscrowTransactions"))
              .build();
        }
      }
    }
    return getGetAccountEscrowTransactionsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountsRequest,
      brs.api.grpc.proto.BrsApi.Accounts> getGetAccountsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAccounts",
      requestType = brs.api.grpc.proto.BrsApi.GetAccountsRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Accounts.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountsRequest,
      brs.api.grpc.proto.BrsApi.Accounts> getGetAccountsMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountsRequest, brs.api.grpc.proto.BrsApi.Accounts> getGetAccountsMethod;
    if ((getGetAccountsMethod = BrsApiServiceGrpc.getGetAccountsMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetAccountsMethod = BrsApiServiceGrpc.getGetAccountsMethod) == null) {
          BrsApiServiceGrpc.getGetAccountsMethod = getGetAccountsMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetAccountsRequest, brs.api.grpc.proto.BrsApi.Accounts>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAccounts"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetAccountsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Accounts.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetAccounts"))
              .build();
        }
      }
    }
    return getGetAccountsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest,
      brs.api.grpc.proto.BrsApi.Subscriptions> getGetAccountSubscriptionsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAccountSubscriptions",
      requestType = brs.api.grpc.proto.BrsApi.GetAccountRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Subscriptions.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest,
      brs.api.grpc.proto.BrsApi.Subscriptions> getGetAccountSubscriptionsMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest, brs.api.grpc.proto.BrsApi.Subscriptions> getGetAccountSubscriptionsMethod;
    if ((getGetAccountSubscriptionsMethod = BrsApiServiceGrpc.getGetAccountSubscriptionsMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetAccountSubscriptionsMethod = BrsApiServiceGrpc.getGetAccountSubscriptionsMethod) == null) {
          BrsApiServiceGrpc.getGetAccountSubscriptionsMethod = getGetAccountSubscriptionsMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetAccountRequest, brs.api.grpc.proto.BrsApi.Subscriptions>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAccountSubscriptions"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetAccountRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Subscriptions.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetAccountSubscriptions"))
              .build();
        }
      }
    }
    return getGetAccountSubscriptionsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountTransactionsRequest,
      brs.api.grpc.proto.BrsApi.Transactions> getGetAccountTransactionsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAccountTransactions",
      requestType = brs.api.grpc.proto.BrsApi.GetAccountTransactionsRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Transactions.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountTransactionsRequest,
      brs.api.grpc.proto.BrsApi.Transactions> getGetAccountTransactionsMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountTransactionsRequest, brs.api.grpc.proto.BrsApi.Transactions> getGetAccountTransactionsMethod;
    if ((getGetAccountTransactionsMethod = BrsApiServiceGrpc.getGetAccountTransactionsMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetAccountTransactionsMethod = BrsApiServiceGrpc.getGetAccountTransactionsMethod) == null) {
          BrsApiServiceGrpc.getGetAccountTransactionsMethod = getGetAccountTransactionsMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetAccountTransactionsRequest, brs.api.grpc.proto.BrsApi.Transactions>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAccountTransactions"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetAccountTransactionsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Transactions.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetAccountTransactions"))
              .build();
        }
      }
    }
    return getGetAccountTransactionsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAliasRequest,
      brs.api.grpc.proto.BrsApi.Alias> getGetAliasMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAlias",
      requestType = brs.api.grpc.proto.BrsApi.GetAliasRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Alias.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAliasRequest,
      brs.api.grpc.proto.BrsApi.Alias> getGetAliasMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAliasRequest, brs.api.grpc.proto.BrsApi.Alias> getGetAliasMethod;
    if ((getGetAliasMethod = BrsApiServiceGrpc.getGetAliasMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetAliasMethod = BrsApiServiceGrpc.getGetAliasMethod) == null) {
          BrsApiServiceGrpc.getGetAliasMethod = getGetAliasMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetAliasRequest, brs.api.grpc.proto.BrsApi.Alias>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAlias"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetAliasRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Alias.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetAlias"))
              .build();
        }
      }
    }
    return getGetAliasMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAliasesRequest,
      brs.api.grpc.proto.BrsApi.Aliases> getGetAliasesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAliases",
      requestType = brs.api.grpc.proto.BrsApi.GetAliasesRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Aliases.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAliasesRequest,
      brs.api.grpc.proto.BrsApi.Aliases> getGetAliasesMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAliasesRequest, brs.api.grpc.proto.BrsApi.Aliases> getGetAliasesMethod;
    if ((getGetAliasesMethod = BrsApiServiceGrpc.getGetAliasesMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetAliasesMethod = BrsApiServiceGrpc.getGetAliasesMethod) == null) {
          BrsApiServiceGrpc.getGetAliasesMethod = getGetAliasesMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetAliasesRequest, brs.api.grpc.proto.BrsApi.Aliases>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAliases"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetAliasesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Aliases.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetAliases"))
              .build();
        }
      }
    }
    return getGetAliasesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetByIdRequest,
      brs.api.grpc.proto.BrsApi.Asset> getGetAssetMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAsset",
      requestType = brs.api.grpc.proto.BrsApi.GetByIdRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Asset.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetByIdRequest,
      brs.api.grpc.proto.BrsApi.Asset> getGetAssetMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetByIdRequest, brs.api.grpc.proto.BrsApi.Asset> getGetAssetMethod;
    if ((getGetAssetMethod = BrsApiServiceGrpc.getGetAssetMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetAssetMethod = BrsApiServiceGrpc.getGetAssetMethod) == null) {
          BrsApiServiceGrpc.getGetAssetMethod = getGetAssetMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetByIdRequest, brs.api.grpc.proto.BrsApi.Asset>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAsset"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetByIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Asset.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetAsset"))
              .build();
        }
      }
    }
    return getGetAssetMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAssetBalancesRequest,
      brs.api.grpc.proto.BrsApi.AssetBalances> getGetAssetBalancesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAssetBalances",
      requestType = brs.api.grpc.proto.BrsApi.GetAssetBalancesRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.AssetBalances.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAssetBalancesRequest,
      brs.api.grpc.proto.BrsApi.AssetBalances> getGetAssetBalancesMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAssetBalancesRequest, brs.api.grpc.proto.BrsApi.AssetBalances> getGetAssetBalancesMethod;
    if ((getGetAssetBalancesMethod = BrsApiServiceGrpc.getGetAssetBalancesMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetAssetBalancesMethod = BrsApiServiceGrpc.getGetAssetBalancesMethod) == null) {
          BrsApiServiceGrpc.getGetAssetBalancesMethod = getGetAssetBalancesMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetAssetBalancesRequest, brs.api.grpc.proto.BrsApi.AssetBalances>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAssetBalances"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetAssetBalancesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.AssetBalances.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetAssetBalances"))
              .build();
        }
      }
    }
    return getGetAssetBalancesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAssetsRequest,
      brs.api.grpc.proto.BrsApi.Assets> getGetAssetsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAssets",
      requestType = brs.api.grpc.proto.BrsApi.GetAssetsRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Assets.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAssetsRequest,
      brs.api.grpc.proto.BrsApi.Assets> getGetAssetsMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAssetsRequest, brs.api.grpc.proto.BrsApi.Assets> getGetAssetsMethod;
    if ((getGetAssetsMethod = BrsApiServiceGrpc.getGetAssetsMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetAssetsMethod = BrsApiServiceGrpc.getGetAssetsMethod) == null) {
          BrsApiServiceGrpc.getGetAssetsMethod = getGetAssetsMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetAssetsRequest, brs.api.grpc.proto.BrsApi.Assets>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAssets"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetAssetsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Assets.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetAssets"))
              .build();
        }
      }
    }
    return getGetAssetsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest,
      brs.api.grpc.proto.BrsApi.Assets> getGetAssetsByIssuerMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAssetsByIssuer",
      requestType = brs.api.grpc.proto.BrsApi.GetAccountRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Assets.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest,
      brs.api.grpc.proto.BrsApi.Assets> getGetAssetsByIssuerMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest, brs.api.grpc.proto.BrsApi.Assets> getGetAssetsByIssuerMethod;
    if ((getGetAssetsByIssuerMethod = BrsApiServiceGrpc.getGetAssetsByIssuerMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetAssetsByIssuerMethod = BrsApiServiceGrpc.getGetAssetsByIssuerMethod) == null) {
          BrsApiServiceGrpc.getGetAssetsByIssuerMethod = getGetAssetsByIssuerMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetAccountRequest, brs.api.grpc.proto.BrsApi.Assets>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAssetsByIssuer"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetAccountRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Assets.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetAssetsByIssuer"))
              .build();
        }
      }
    }
    return getGetAssetsByIssuerMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest,
      brs.api.grpc.proto.BrsApi.AssetTrades> getGetAssetTradesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAssetTrades",
      requestType = brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.AssetTrades.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest,
      brs.api.grpc.proto.BrsApi.AssetTrades> getGetAssetTradesMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest, brs.api.grpc.proto.BrsApi.AssetTrades> getGetAssetTradesMethod;
    if ((getGetAssetTradesMethod = BrsApiServiceGrpc.getGetAssetTradesMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetAssetTradesMethod = BrsApiServiceGrpc.getGetAssetTradesMethod) == null) {
          BrsApiServiceGrpc.getGetAssetTradesMethod = getGetAssetTradesMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest, brs.api.grpc.proto.BrsApi.AssetTrades>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAssetTrades"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.AssetTrades.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetAssetTrades"))
              .build();
        }
      }
    }
    return getGetAssetTradesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest,
      brs.api.grpc.proto.BrsApi.AssetTransfers> getGetAssetTransfersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAssetTransfers",
      requestType = brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.AssetTransfers.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest,
      brs.api.grpc.proto.BrsApi.AssetTransfers> getGetAssetTransfersMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest, brs.api.grpc.proto.BrsApi.AssetTransfers> getGetAssetTransfersMethod;
    if ((getGetAssetTransfersMethod = BrsApiServiceGrpc.getGetAssetTransfersMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetAssetTransfersMethod = BrsApiServiceGrpc.getGetAssetTransfersMethod) == null) {
          BrsApiServiceGrpc.getGetAssetTransfersMethod = getGetAssetTransfersMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest, brs.api.grpc.proto.BrsApi.AssetTransfers>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAssetTransfers"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.AssetTransfers.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetAssetTransfers"))
              .build();
        }
      }
    }
    return getGetAssetTransfersMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetByIdRequest,
      brs.api.grpc.proto.BrsApi.AT> getGetATMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAT",
      requestType = brs.api.grpc.proto.BrsApi.GetByIdRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.AT.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetByIdRequest,
      brs.api.grpc.proto.BrsApi.AT> getGetATMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetByIdRequest, brs.api.grpc.proto.BrsApi.AT> getGetATMethod;
    if ((getGetATMethod = BrsApiServiceGrpc.getGetATMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetATMethod = BrsApiServiceGrpc.getGetATMethod) == null) {
          BrsApiServiceGrpc.getGetATMethod = getGetATMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetByIdRequest, brs.api.grpc.proto.BrsApi.AT>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAT"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetByIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.AT.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetAT"))
              .build();
        }
      }
    }
    return getGetATMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.BrsApi.ATIds> getGetATIdsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetATIds",
      requestType = com.google.protobuf.Empty.class,
      responseType = brs.api.grpc.proto.BrsApi.ATIds.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.BrsApi.ATIds> getGetATIdsMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, brs.api.grpc.proto.BrsApi.ATIds> getGetATIdsMethod;
    if ((getGetATIdsMethod = BrsApiServiceGrpc.getGetATIdsMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetATIdsMethod = BrsApiServiceGrpc.getGetATIdsMethod) == null) {
          BrsApiServiceGrpc.getGetATIdsMethod = getGetATIdsMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, brs.api.grpc.proto.BrsApi.ATIds>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetATIds"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.ATIds.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetATIds"))
              .build();
        }
      }
    }
    return getGetATIdsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetBlockRequest,
      brs.api.grpc.proto.BrsApi.Block> getGetBlockMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetBlock",
      requestType = brs.api.grpc.proto.BrsApi.GetBlockRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Block.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetBlockRequest,
      brs.api.grpc.proto.BrsApi.Block> getGetBlockMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetBlockRequest, brs.api.grpc.proto.BrsApi.Block> getGetBlockMethod;
    if ((getGetBlockMethod = BrsApiServiceGrpc.getGetBlockMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetBlockMethod = BrsApiServiceGrpc.getGetBlockMethod) == null) {
          BrsApiServiceGrpc.getGetBlockMethod = getGetBlockMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetBlockRequest, brs.api.grpc.proto.BrsApi.Block>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetBlock"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetBlockRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Block.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetBlock"))
              .build();
        }
      }
    }
    return getGetBlockMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetBlocksRequest,
      brs.api.grpc.proto.BrsApi.Blocks> getGetBlocksMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetBlocks",
      requestType = brs.api.grpc.proto.BrsApi.GetBlocksRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Blocks.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetBlocksRequest,
      brs.api.grpc.proto.BrsApi.Blocks> getGetBlocksMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetBlocksRequest, brs.api.grpc.proto.BrsApi.Blocks> getGetBlocksMethod;
    if ((getGetBlocksMethod = BrsApiServiceGrpc.getGetBlocksMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetBlocksMethod = BrsApiServiceGrpc.getGetBlocksMethod) == null) {
          BrsApiServiceGrpc.getGetBlocksMethod = getGetBlocksMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetBlocksRequest, brs.api.grpc.proto.BrsApi.Blocks>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetBlocks"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetBlocksRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Blocks.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetBlocks"))
              .build();
        }
      }
    }
    return getGetBlocksMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.BrsApi.Constants> getGetConstantsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetConstants",
      requestType = com.google.protobuf.Empty.class,
      responseType = brs.api.grpc.proto.BrsApi.Constants.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.BrsApi.Constants> getGetConstantsMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, brs.api.grpc.proto.BrsApi.Constants> getGetConstantsMethod;
    if ((getGetConstantsMethod = BrsApiServiceGrpc.getGetConstantsMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetConstantsMethod = BrsApiServiceGrpc.getGetConstantsMethod) == null) {
          BrsApiServiceGrpc.getGetConstantsMethod = getGetConstantsMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, brs.api.grpc.proto.BrsApi.Constants>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetConstants"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Constants.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetConstants"))
              .build();
        }
      }
    }
    return getGetConstantsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.BrsApi.Counts> getGetCountsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetCounts",
      requestType = com.google.protobuf.Empty.class,
      responseType = brs.api.grpc.proto.BrsApi.Counts.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.BrsApi.Counts> getGetCountsMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, brs.api.grpc.proto.BrsApi.Counts> getGetCountsMethod;
    if ((getGetCountsMethod = BrsApiServiceGrpc.getGetCountsMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetCountsMethod = BrsApiServiceGrpc.getGetCountsMethod) == null) {
          BrsApiServiceGrpc.getGetCountsMethod = getGetCountsMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, brs.api.grpc.proto.BrsApi.Counts>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetCounts"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Counts.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetCounts"))
              .build();
        }
      }
    }
    return getGetCountsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.BrsApi.Time> getGetCurrentTimeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetCurrentTime",
      requestType = com.google.protobuf.Empty.class,
      responseType = brs.api.grpc.proto.BrsApi.Time.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.BrsApi.Time> getGetCurrentTimeMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, brs.api.grpc.proto.BrsApi.Time> getGetCurrentTimeMethod;
    if ((getGetCurrentTimeMethod = BrsApiServiceGrpc.getGetCurrentTimeMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetCurrentTimeMethod = BrsApiServiceGrpc.getGetCurrentTimeMethod) == null) {
          BrsApiServiceGrpc.getGetCurrentTimeMethod = getGetCurrentTimeMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, brs.api.grpc.proto.BrsApi.Time>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetCurrentTime"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Time.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetCurrentTime"))
              .build();
        }
      }
    }
    return getGetCurrentTimeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetByIdRequest,
      brs.api.grpc.proto.BrsApi.DgsGood> getGetDgsGoodMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetDgsGood",
      requestType = brs.api.grpc.proto.BrsApi.GetByIdRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.DgsGood.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetByIdRequest,
      brs.api.grpc.proto.BrsApi.DgsGood> getGetDgsGoodMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetByIdRequest, brs.api.grpc.proto.BrsApi.DgsGood> getGetDgsGoodMethod;
    if ((getGetDgsGoodMethod = BrsApiServiceGrpc.getGetDgsGoodMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetDgsGoodMethod = BrsApiServiceGrpc.getGetDgsGoodMethod) == null) {
          BrsApiServiceGrpc.getGetDgsGoodMethod = getGetDgsGoodMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetByIdRequest, brs.api.grpc.proto.BrsApi.DgsGood>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetDgsGood"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetByIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.DgsGood.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetDgsGood"))
              .build();
        }
      }
    }
    return getGetDgsGoodMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetDgsGoodsRequest,
      brs.api.grpc.proto.BrsApi.DgsGoods> getGetDgsGoodsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetDgsGoods",
      requestType = brs.api.grpc.proto.BrsApi.GetDgsGoodsRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.DgsGoods.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetDgsGoodsRequest,
      brs.api.grpc.proto.BrsApi.DgsGoods> getGetDgsGoodsMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetDgsGoodsRequest, brs.api.grpc.proto.BrsApi.DgsGoods> getGetDgsGoodsMethod;
    if ((getGetDgsGoodsMethod = BrsApiServiceGrpc.getGetDgsGoodsMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetDgsGoodsMethod = BrsApiServiceGrpc.getGetDgsGoodsMethod) == null) {
          BrsApiServiceGrpc.getGetDgsGoodsMethod = getGetDgsGoodsMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetDgsGoodsRequest, brs.api.grpc.proto.BrsApi.DgsGoods>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetDgsGoods"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetDgsGoodsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.DgsGoods.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetDgsGoods"))
              .build();
        }
      }
    }
    return getGetDgsGoodsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetDgsPendingPurchasesRequest,
      brs.api.grpc.proto.BrsApi.DgsPurchases> getGetDgsPendingPurchasesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetDgsPendingPurchases",
      requestType = brs.api.grpc.proto.BrsApi.GetDgsPendingPurchasesRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.DgsPurchases.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetDgsPendingPurchasesRequest,
      brs.api.grpc.proto.BrsApi.DgsPurchases> getGetDgsPendingPurchasesMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetDgsPendingPurchasesRequest, brs.api.grpc.proto.BrsApi.DgsPurchases> getGetDgsPendingPurchasesMethod;
    if ((getGetDgsPendingPurchasesMethod = BrsApiServiceGrpc.getGetDgsPendingPurchasesMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetDgsPendingPurchasesMethod = BrsApiServiceGrpc.getGetDgsPendingPurchasesMethod) == null) {
          BrsApiServiceGrpc.getGetDgsPendingPurchasesMethod = getGetDgsPendingPurchasesMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetDgsPendingPurchasesRequest, brs.api.grpc.proto.BrsApi.DgsPurchases>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetDgsPendingPurchases"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetDgsPendingPurchasesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.DgsPurchases.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetDgsPendingPurchases"))
              .build();
        }
      }
    }
    return getGetDgsPendingPurchasesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetByIdRequest,
      brs.api.grpc.proto.BrsApi.DgsPurchase> getGetDgsPurchaseMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetDgsPurchase",
      requestType = brs.api.grpc.proto.BrsApi.GetByIdRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.DgsPurchase.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetByIdRequest,
      brs.api.grpc.proto.BrsApi.DgsPurchase> getGetDgsPurchaseMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetByIdRequest, brs.api.grpc.proto.BrsApi.DgsPurchase> getGetDgsPurchaseMethod;
    if ((getGetDgsPurchaseMethod = BrsApiServiceGrpc.getGetDgsPurchaseMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetDgsPurchaseMethod = BrsApiServiceGrpc.getGetDgsPurchaseMethod) == null) {
          BrsApiServiceGrpc.getGetDgsPurchaseMethod = getGetDgsPurchaseMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetByIdRequest, brs.api.grpc.proto.BrsApi.DgsPurchase>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetDgsPurchase"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetByIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.DgsPurchase.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetDgsPurchase"))
              .build();
        }
      }
    }
    return getGetDgsPurchaseMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetDgsPurchasesRequest,
      brs.api.grpc.proto.BrsApi.DgsPurchases> getGetDgsPurchasesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetDgsPurchases",
      requestType = brs.api.grpc.proto.BrsApi.GetDgsPurchasesRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.DgsPurchases.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetDgsPurchasesRequest,
      brs.api.grpc.proto.BrsApi.DgsPurchases> getGetDgsPurchasesMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetDgsPurchasesRequest, brs.api.grpc.proto.BrsApi.DgsPurchases> getGetDgsPurchasesMethod;
    if ((getGetDgsPurchasesMethod = BrsApiServiceGrpc.getGetDgsPurchasesMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetDgsPurchasesMethod = BrsApiServiceGrpc.getGetDgsPurchasesMethod) == null) {
          BrsApiServiceGrpc.getGetDgsPurchasesMethod = getGetDgsPurchasesMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetDgsPurchasesRequest, brs.api.grpc.proto.BrsApi.DgsPurchases>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetDgsPurchases"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetDgsPurchasesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.DgsPurchases.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetDgsPurchases"))
              .build();
        }
      }
    }
    return getGetDgsPurchasesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetByIdRequest,
      brs.api.grpc.proto.BrsApi.EscrowTransaction> getGetEscrowTransactionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetEscrowTransaction",
      requestType = brs.api.grpc.proto.BrsApi.GetByIdRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.EscrowTransaction.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetByIdRequest,
      brs.api.grpc.proto.BrsApi.EscrowTransaction> getGetEscrowTransactionMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetByIdRequest, brs.api.grpc.proto.BrsApi.EscrowTransaction> getGetEscrowTransactionMethod;
    if ((getGetEscrowTransactionMethod = BrsApiServiceGrpc.getGetEscrowTransactionMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetEscrowTransactionMethod = BrsApiServiceGrpc.getGetEscrowTransactionMethod) == null) {
          BrsApiServiceGrpc.getGetEscrowTransactionMethod = getGetEscrowTransactionMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetByIdRequest, brs.api.grpc.proto.BrsApi.EscrowTransaction>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetEscrowTransaction"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetByIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.EscrowTransaction.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetEscrowTransaction"))
              .build();
        }
      }
    }
    return getGetEscrowTransactionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.BrsApi.MiningInfo> getGetMiningInfoMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetMiningInfo",
      requestType = com.google.protobuf.Empty.class,
      responseType = brs.api.grpc.proto.BrsApi.MiningInfo.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.BrsApi.MiningInfo> getGetMiningInfoMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, brs.api.grpc.proto.BrsApi.MiningInfo> getGetMiningInfoMethod;
    if ((getGetMiningInfoMethod = BrsApiServiceGrpc.getGetMiningInfoMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetMiningInfoMethod = BrsApiServiceGrpc.getGetMiningInfoMethod) == null) {
          BrsApiServiceGrpc.getGetMiningInfoMethod = getGetMiningInfoMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, brs.api.grpc.proto.BrsApi.MiningInfo>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetMiningInfo"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.MiningInfo.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetMiningInfo"))
              .build();
        }
      }
    }
    return getGetMiningInfoMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetOrderRequest,
      brs.api.grpc.proto.BrsApi.Order> getGetOrderMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetOrder",
      requestType = brs.api.grpc.proto.BrsApi.GetOrderRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Order.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetOrderRequest,
      brs.api.grpc.proto.BrsApi.Order> getGetOrderMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetOrderRequest, brs.api.grpc.proto.BrsApi.Order> getGetOrderMethod;
    if ((getGetOrderMethod = BrsApiServiceGrpc.getGetOrderMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetOrderMethod = BrsApiServiceGrpc.getGetOrderMethod) == null) {
          BrsApiServiceGrpc.getGetOrderMethod = getGetOrderMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetOrderRequest, brs.api.grpc.proto.BrsApi.Order>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetOrder"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetOrderRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Order.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetOrder"))
              .build();
        }
      }
    }
    return getGetOrderMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetOrdersRequest,
      brs.api.grpc.proto.BrsApi.Orders> getGetOrdersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetOrders",
      requestType = brs.api.grpc.proto.BrsApi.GetOrdersRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Orders.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetOrdersRequest,
      brs.api.grpc.proto.BrsApi.Orders> getGetOrdersMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetOrdersRequest, brs.api.grpc.proto.BrsApi.Orders> getGetOrdersMethod;
    if ((getGetOrdersMethod = BrsApiServiceGrpc.getGetOrdersMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetOrdersMethod = BrsApiServiceGrpc.getGetOrdersMethod) == null) {
          BrsApiServiceGrpc.getGetOrdersMethod = getGetOrdersMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetOrdersRequest, brs.api.grpc.proto.BrsApi.Orders>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetOrders"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetOrdersRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Orders.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetOrders"))
              .build();
        }
      }
    }
    return getGetOrdersMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetPeerRequest,
      brs.api.grpc.proto.BrsApi.Peer> getGetPeerMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPeer",
      requestType = brs.api.grpc.proto.BrsApi.GetPeerRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Peer.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetPeerRequest,
      brs.api.grpc.proto.BrsApi.Peer> getGetPeerMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetPeerRequest, brs.api.grpc.proto.BrsApi.Peer> getGetPeerMethod;
    if ((getGetPeerMethod = BrsApiServiceGrpc.getGetPeerMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetPeerMethod = BrsApiServiceGrpc.getGetPeerMethod) == null) {
          BrsApiServiceGrpc.getGetPeerMethod = getGetPeerMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetPeerRequest, brs.api.grpc.proto.BrsApi.Peer>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetPeer"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetPeerRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Peer.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetPeer"))
              .build();
        }
      }
    }
    return getGetPeerMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetPeersRequest,
      brs.api.grpc.proto.BrsApi.Peers> getGetPeersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPeers",
      requestType = brs.api.grpc.proto.BrsApi.GetPeersRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Peers.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetPeersRequest,
      brs.api.grpc.proto.BrsApi.Peers> getGetPeersMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetPeersRequest, brs.api.grpc.proto.BrsApi.Peers> getGetPeersMethod;
    if ((getGetPeersMethod = BrsApiServiceGrpc.getGetPeersMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetPeersMethod = BrsApiServiceGrpc.getGetPeersMethod) == null) {
          BrsApiServiceGrpc.getGetPeersMethod = getGetPeersMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetPeersRequest, brs.api.grpc.proto.BrsApi.Peers>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetPeers"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetPeersRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Peers.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetPeers"))
              .build();
        }
      }
    }
    return getGetPeersMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.BrsApi.State> getGetStateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetState",
      requestType = com.google.protobuf.Empty.class,
      responseType = brs.api.grpc.proto.BrsApi.State.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.BrsApi.State> getGetStateMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, brs.api.grpc.proto.BrsApi.State> getGetStateMethod;
    if ((getGetStateMethod = BrsApiServiceGrpc.getGetStateMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetStateMethod = BrsApiServiceGrpc.getGetStateMethod) == null) {
          BrsApiServiceGrpc.getGetStateMethod = getGetStateMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, brs.api.grpc.proto.BrsApi.State>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetState"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.State.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetState"))
              .build();
        }
      }
    }
    return getGetStateMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetByIdRequest,
      brs.api.grpc.proto.BrsApi.Subscription> getGetSubscriptionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetSubscription",
      requestType = brs.api.grpc.proto.BrsApi.GetByIdRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Subscription.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetByIdRequest,
      brs.api.grpc.proto.BrsApi.Subscription> getGetSubscriptionMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetByIdRequest, brs.api.grpc.proto.BrsApi.Subscription> getGetSubscriptionMethod;
    if ((getGetSubscriptionMethod = BrsApiServiceGrpc.getGetSubscriptionMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetSubscriptionMethod = BrsApiServiceGrpc.getGetSubscriptionMethod) == null) {
          BrsApiServiceGrpc.getGetSubscriptionMethod = getGetSubscriptionMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetByIdRequest, brs.api.grpc.proto.BrsApi.Subscription>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetSubscription"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetByIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Subscription.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetSubscription"))
              .build();
        }
      }
    }
    return getGetSubscriptionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest,
      brs.api.grpc.proto.BrsApi.Subscriptions> getGetSubscriptionsToAccountMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetSubscriptionsToAccount",
      requestType = brs.api.grpc.proto.BrsApi.GetAccountRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Subscriptions.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest,
      brs.api.grpc.proto.BrsApi.Subscriptions> getGetSubscriptionsToAccountMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest, brs.api.grpc.proto.BrsApi.Subscriptions> getGetSubscriptionsToAccountMethod;
    if ((getGetSubscriptionsToAccountMethod = BrsApiServiceGrpc.getGetSubscriptionsToAccountMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetSubscriptionsToAccountMethod = BrsApiServiceGrpc.getGetSubscriptionsToAccountMethod) == null) {
          BrsApiServiceGrpc.getGetSubscriptionsToAccountMethod = getGetSubscriptionsToAccountMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetAccountRequest, brs.api.grpc.proto.BrsApi.Subscriptions>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetSubscriptionsToAccount"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetAccountRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Subscriptions.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetSubscriptionsToAccount"))
              .build();
        }
      }
    }
    return getGetSubscriptionsToAccountMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetTransactionRequest,
      brs.api.grpc.proto.BrsApi.Transaction> getGetTransactionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTransaction",
      requestType = brs.api.grpc.proto.BrsApi.GetTransactionRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.Transaction.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetTransactionRequest,
      brs.api.grpc.proto.BrsApi.Transaction> getGetTransactionMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetTransactionRequest, brs.api.grpc.proto.BrsApi.Transaction> getGetTransactionMethod;
    if ((getGetTransactionMethod = BrsApiServiceGrpc.getGetTransactionMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetTransactionMethod = BrsApiServiceGrpc.getGetTransactionMethod) == null) {
          BrsApiServiceGrpc.getGetTransactionMethod = getGetTransactionMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetTransactionRequest, brs.api.grpc.proto.BrsApi.Transaction>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTransaction"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetTransactionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.Transaction.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetTransaction"))
              .build();
        }
      }
    }
    return getGetTransactionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.BasicTransaction,
      brs.api.grpc.proto.BrsApi.TransactionBytes> getGetTransactionBytesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTransactionBytes",
      requestType = brs.api.grpc.proto.BrsApi.BasicTransaction.class,
      responseType = brs.api.grpc.proto.BrsApi.TransactionBytes.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.BasicTransaction,
      brs.api.grpc.proto.BrsApi.TransactionBytes> getGetTransactionBytesMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.BasicTransaction, brs.api.grpc.proto.BrsApi.TransactionBytes> getGetTransactionBytesMethod;
    if ((getGetTransactionBytesMethod = BrsApiServiceGrpc.getGetTransactionBytesMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetTransactionBytesMethod = BrsApiServiceGrpc.getGetTransactionBytesMethod) == null) {
          BrsApiServiceGrpc.getGetTransactionBytesMethod = getGetTransactionBytesMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.BasicTransaction, brs.api.grpc.proto.BrsApi.TransactionBytes>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTransactionBytes"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.BasicTransaction.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.TransactionBytes.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetTransactionBytes"))
              .build();
        }
      }
    }
    return getGetTransactionBytesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest,
      brs.api.grpc.proto.BrsApi.UnconfirmedTransactions> getGetUnconfirmedTransactionsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetUnconfirmedTransactions",
      requestType = brs.api.grpc.proto.BrsApi.GetAccountRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.UnconfirmedTransactions.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest,
      brs.api.grpc.proto.BrsApi.UnconfirmedTransactions> getGetUnconfirmedTransactionsMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.GetAccountRequest, brs.api.grpc.proto.BrsApi.UnconfirmedTransactions> getGetUnconfirmedTransactionsMethod;
    if ((getGetUnconfirmedTransactionsMethod = BrsApiServiceGrpc.getGetUnconfirmedTransactionsMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getGetUnconfirmedTransactionsMethod = BrsApiServiceGrpc.getGetUnconfirmedTransactionsMethod) == null) {
          BrsApiServiceGrpc.getGetUnconfirmedTransactionsMethod = getGetUnconfirmedTransactionsMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.GetAccountRequest, brs.api.grpc.proto.BrsApi.UnconfirmedTransactions>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetUnconfirmedTransactions"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.GetAccountRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.UnconfirmedTransactions.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("GetUnconfirmedTransactions"))
              .build();
        }
      }
    }
    return getGetUnconfirmedTransactionsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.TransactionBytes,
      brs.api.grpc.proto.BrsApi.BasicTransaction> getParseTransactionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ParseTransaction",
      requestType = brs.api.grpc.proto.BrsApi.TransactionBytes.class,
      responseType = brs.api.grpc.proto.BrsApi.BasicTransaction.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.TransactionBytes,
      brs.api.grpc.proto.BrsApi.BasicTransaction> getParseTransactionMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.TransactionBytes, brs.api.grpc.proto.BrsApi.BasicTransaction> getParseTransactionMethod;
    if ((getParseTransactionMethod = BrsApiServiceGrpc.getParseTransactionMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getParseTransactionMethod = BrsApiServiceGrpc.getParseTransactionMethod) == null) {
          BrsApiServiceGrpc.getParseTransactionMethod = getParseTransactionMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.TransactionBytes, brs.api.grpc.proto.BrsApi.BasicTransaction>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ParseTransaction"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.TransactionBytes.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.BasicTransaction.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("ParseTransaction"))
              .build();
        }
      }
    }
    return getParseTransactionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.SubmitNonceRequest,
      brs.api.grpc.proto.BrsApi.SubmitNonceResponse> getSubmitNonceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SubmitNonce",
      requestType = brs.api.grpc.proto.BrsApi.SubmitNonceRequest.class,
      responseType = brs.api.grpc.proto.BrsApi.SubmitNonceResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.SubmitNonceRequest,
      brs.api.grpc.proto.BrsApi.SubmitNonceResponse> getSubmitNonceMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.BrsApi.SubmitNonceRequest, brs.api.grpc.proto.BrsApi.SubmitNonceResponse> getSubmitNonceMethod;
    if ((getSubmitNonceMethod = BrsApiServiceGrpc.getSubmitNonceMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getSubmitNonceMethod = BrsApiServiceGrpc.getSubmitNonceMethod) == null) {
          BrsApiServiceGrpc.getSubmitNonceMethod = getSubmitNonceMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.BrsApi.SubmitNonceRequest, brs.api.grpc.proto.BrsApi.SubmitNonceResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SubmitNonce"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.SubmitNonceRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.SubmitNonceResponse.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("SubmitNonce"))
              .build();
        }
      }
    }
    return getSubmitNonceMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.BrsApi.FeeSuggestion> getSuggestFeeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SuggestFee",
      requestType = com.google.protobuf.Empty.class,
      responseType = brs.api.grpc.proto.BrsApi.FeeSuggestion.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.BrsApi.FeeSuggestion> getSuggestFeeMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, brs.api.grpc.proto.BrsApi.FeeSuggestion> getSuggestFeeMethod;
    if ((getSuggestFeeMethod = BrsApiServiceGrpc.getSuggestFeeMethod) == null) {
      synchronized (BrsApiServiceGrpc.class) {
        if ((getSuggestFeeMethod = BrsApiServiceGrpc.getSuggestFeeMethod) == null) {
          BrsApiServiceGrpc.getSuggestFeeMethod = getSuggestFeeMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, brs.api.grpc.proto.BrsApi.FeeSuggestion>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SuggestFee"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.BrsApi.FeeSuggestion.getDefaultInstance()))
              .setSchemaDescriptor(new BrsApiServiceMethodDescriptorSupplier("SuggestFee"))
              .build();
        }
      }
    }
    return getSuggestFeeMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static BrsApiServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BrsApiServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BrsApiServiceStub>() {
        @java.lang.Override
        public BrsApiServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BrsApiServiceStub(channel, callOptions);
        }
      };
    return BrsApiServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static BrsApiServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BrsApiServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BrsApiServiceBlockingStub>() {
        @java.lang.Override
        public BrsApiServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BrsApiServiceBlockingStub(channel, callOptions);
        }
      };
    return BrsApiServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static BrsApiServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BrsApiServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BrsApiServiceFutureStub>() {
        @java.lang.Override
        public BrsApiServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BrsApiServiceFutureStub(channel, callOptions);
        }
      };
    return BrsApiServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class BrsApiServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Broadcast a transaction to the network.
     * </pre>
     */
    public void broadcastTransaction(brs.api.grpc.proto.BrsApi.BasicTransaction request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.TransactionBroadcastResult> responseObserver) {
      asyncUnimplementedUnaryCall(getBroadcastTransactionMethod(), responseObserver);
    }

    /**
     * <pre>
     * Broadcast a transaction to the network. Takes transaction bytes instead of a BasicTransaction
     * </pre>
     */
    public void broadcastTransactionBytes(brs.api.grpc.proto.BrsApi.TransactionBytes request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.TransactionBroadcastResult> responseObserver) {
      asyncUnimplementedUnaryCall(getBroadcastTransactionBytesMethod(), responseObserver);
    }

    /**
     * <pre>
     * Automatically fills in the following fields: Version (based on current transaction version), type and subtype (based on specified attachment), timestamp (current time). Additionally sets attachment to ordinary payment if it was not set
     * </pre>
     */
    public void completeBasicTransaction(brs.api.grpc.proto.BrsApi.BasicTransaction request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.BasicTransaction> responseObserver) {
      asyncUnimplementedUnaryCall(getCompleteBasicTransactionMethod(), responseObserver);
    }

    /**
     */
    public void getAccount(brs.api.grpc.proto.BrsApi.GetAccountRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Account> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAccountMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get the ATs that an account is the creator of, by the creator's account ID
     * </pre>
     */
    public void getAccountATs(brs.api.grpc.proto.BrsApi.GetAccountRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.AccountATs> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAccountATsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get the blocks that an account has forged, by the forger's ID
     * </pre>
     */
    public void getAccountBlocks(brs.api.grpc.proto.BrsApi.GetAccountBlocksRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Blocks> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAccountBlocksMethod(), responseObserver);
    }

    /**
     */
    public void getAccountCurrentOrders(brs.api.grpc.proto.BrsApi.GetAccountOrdersRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Orders> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAccountCurrentOrdersMethod(), responseObserver);
    }

    /**
     */
    public void getAccountEscrowTransactions(brs.api.grpc.proto.BrsApi.GetAccountRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.EscrowTransactions> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAccountEscrowTransactionsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get multiple accounts based on the criteria described in GetAccountsRequest. The criteria is an OR selection - I.E. if you specified a reward recipient and a name it would include accounts that have that recipient and that name, including duplicates. Therefore it is recommended to only select one criteria
     * </pre>
     */
    public void getAccounts(brs.api.grpc.proto.BrsApi.GetAccountsRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Accounts> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAccountsMethod(), responseObserver);
    }

    /**
     */
    public void getAccountSubscriptions(brs.api.grpc.proto.BrsApi.GetAccountRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Subscriptions> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAccountSubscriptionsMethod(), responseObserver);
    }

    /**
     */
    public void getAccountTransactions(brs.api.grpc.proto.BrsApi.GetAccountTransactionsRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Transactions> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAccountTransactionsMethod(), responseObserver);
    }

    /**
     */
    public void getAlias(brs.api.grpc.proto.BrsApi.GetAliasRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Alias> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAliasMethod(), responseObserver);
    }

    /**
     */
    public void getAliases(brs.api.grpc.proto.BrsApi.GetAliasesRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Aliases> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAliasesMethod(), responseObserver);
    }

    /**
     */
    public void getAsset(brs.api.grpc.proto.BrsApi.GetByIdRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Asset> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAssetMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get an asset's holders and their balances
     * </pre>
     */
    public void getAssetBalances(brs.api.grpc.proto.BrsApi.GetAssetBalancesRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.AssetBalances> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAssetBalancesMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get multiple assets in one go.
     * </pre>
     */
    public void getAssets(brs.api.grpc.proto.BrsApi.GetAssetsRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Assets> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAssetsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get all assets issued by an account
     * </pre>
     */
    public void getAssetsByIssuer(brs.api.grpc.proto.BrsApi.GetAccountRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Assets> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAssetsByIssuerMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get all asset trades made by an account
     * </pre>
     */
    public void getAssetTrades(brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.AssetTrades> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAssetTradesMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get all asset transfers made by an account
     * </pre>
     */
    public void getAssetTransfers(brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.AssetTransfers> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAssetTransfersMethod(), responseObserver);
    }

    /**
     */
    public void getAT(brs.api.grpc.proto.BrsApi.GetByIdRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.AT> responseObserver) {
      asyncUnimplementedUnaryCall(getGetATMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get all active AT IDs
     * </pre>
     */
    public void getATIds(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.ATIds> responseObserver) {
      asyncUnimplementedUnaryCall(getGetATIdsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get a block by ID, height or timestamp
     * </pre>
     */
    public void getBlock(brs.api.grpc.proto.BrsApi.GetBlockRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Block> responseObserver) {
      asyncUnimplementedUnaryCall(getGetBlockMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get the most recent blocks
     * </pre>
     */
    public void getBlocks(brs.api.grpc.proto.BrsApi.GetBlocksRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Blocks> responseObserver) {
      asyncUnimplementedUnaryCall(getGetBlocksMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get the current blockchain constants
     * </pre>
     */
    public void getConstants(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Constants> responseObserver) {
      asyncUnimplementedUnaryCall(getGetConstantsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get the counts of different blockchain entities
     * </pre>
     */
    public void getCounts(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Counts> responseObserver) {
      asyncUnimplementedUnaryCall(getGetCountsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get the current Burst time (number of seconds since Burst epoch)
     * </pre>
     */
    public void getCurrentTime(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Time> responseObserver) {
      asyncUnimplementedUnaryCall(getGetCurrentTimeMethod(), responseObserver);
    }

    /**
     */
    public void getDgsGood(brs.api.grpc.proto.BrsApi.GetByIdRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.DgsGood> responseObserver) {
      asyncUnimplementedUnaryCall(getGetDgsGoodMethod(), responseObserver);
    }

    /**
     */
    public void getDgsGoods(brs.api.grpc.proto.BrsApi.GetDgsGoodsRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.DgsGoods> responseObserver) {
      asyncUnimplementedUnaryCall(getGetDgsGoodsMethod(), responseObserver);
    }

    /**
     */
    public void getDgsPendingPurchases(brs.api.grpc.proto.BrsApi.GetDgsPendingPurchasesRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.DgsPurchases> responseObserver) {
      asyncUnimplementedUnaryCall(getGetDgsPendingPurchasesMethod(), responseObserver);
    }

    /**
     */
    public void getDgsPurchase(brs.api.grpc.proto.BrsApi.GetByIdRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.DgsPurchase> responseObserver) {
      asyncUnimplementedUnaryCall(getGetDgsPurchaseMethod(), responseObserver);
    }

    /**
     */
    public void getDgsPurchases(brs.api.grpc.proto.BrsApi.GetDgsPurchasesRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.DgsPurchases> responseObserver) {
      asyncUnimplementedUnaryCall(getGetDgsPurchasesMethod(), responseObserver);
    }

    /**
     */
    public void getEscrowTransaction(brs.api.grpc.proto.BrsApi.GetByIdRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.EscrowTransaction> responseObserver) {
      asyncUnimplementedUnaryCall(getGetEscrowTransactionMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get the mining info for the next block. This is needed to mine.
     * </pre>
     */
    public void getMiningInfo(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.MiningInfo> responseObserver) {
      asyncUnimplementedUnaryCall(getGetMiningInfoMethod(), responseObserver);
    }

    /**
     */
    public void getOrder(brs.api.grpc.proto.BrsApi.GetOrderRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Order> responseObserver) {
      asyncUnimplementedUnaryCall(getGetOrderMethod(), responseObserver);
    }

    /**
     */
    public void getOrders(brs.api.grpc.proto.BrsApi.GetOrdersRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Orders> responseObserver) {
      asyncUnimplementedUnaryCall(getGetOrdersMethod(), responseObserver);
    }

    /**
     */
    public void getPeer(brs.api.grpc.proto.BrsApi.GetPeerRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Peer> responseObserver) {
      asyncUnimplementedUnaryCall(getGetPeerMethod(), responseObserver);
    }

    /**
     */
    public void getPeers(brs.api.grpc.proto.BrsApi.GetPeersRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Peers> responseObserver) {
      asyncUnimplementedUnaryCall(getGetPeersMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get the current server state
     * </pre>
     */
    public void getState(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.State> responseObserver) {
      asyncUnimplementedUnaryCall(getGetStateMethod(), responseObserver);
    }

    /**
     */
    public void getSubscription(brs.api.grpc.proto.BrsApi.GetByIdRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Subscription> responseObserver) {
      asyncUnimplementedUnaryCall(getGetSubscriptionMethod(), responseObserver);
    }

    /**
     */
    public void getSubscriptionsToAccount(brs.api.grpc.proto.BrsApi.GetAccountRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Subscriptions> responseObserver) {
      asyncUnimplementedUnaryCall(getGetSubscriptionsToAccountMethod(), responseObserver);
    }

    /**
     */
    public void getTransaction(brs.api.grpc.proto.BrsApi.GetTransactionRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Transaction> responseObserver) {
      asyncUnimplementedUnaryCall(getGetTransactionMethod(), responseObserver);
    }

    /**
     * <pre>
     * Convert a BasicTranscation into its transaction bytes, to be signed. This theoretically can be done offline so will be removed in the future.
     * </pre>
     */
    public void getTransactionBytes(brs.api.grpc.proto.BrsApi.BasicTransaction request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.TransactionBytes> responseObserver) {
      asyncUnimplementedUnaryCall(getGetTransactionBytesMethod(), responseObserver);
    }

    /**
     */
    public void getUnconfirmedTransactions(brs.api.grpc.proto.BrsApi.GetAccountRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.UnconfirmedTransactions> responseObserver) {
      asyncUnimplementedUnaryCall(getGetUnconfirmedTransactionsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Convert TransactionBytes into a BasicTransaction. This theoretically can be done offline so will be removed in the future.
     * </pre>
     */
    public void parseTransaction(brs.api.grpc.proto.BrsApi.TransactionBytes request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.BasicTransaction> responseObserver) {
      asyncUnimplementedUnaryCall(getParseTransactionMethod(), responseObserver);
    }

    /**
     * <pre>
     * Submit a nonce to try to forge a block. This requires the passphrase to be sent to the server so should only be performed on local nodes.
     * </pre>
     */
    public void submitNonce(brs.api.grpc.proto.BrsApi.SubmitNonceRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.SubmitNonceResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getSubmitNonceMethod(), responseObserver);
    }

    /**
     * <pre>
     * Suggest a fee to use for a transaction
     * </pre>
     */
    public void suggestFee(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.FeeSuggestion> responseObserver) {
      asyncUnimplementedUnaryCall(getSuggestFeeMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getBroadcastTransactionMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.BasicTransaction,
                brs.api.grpc.proto.BrsApi.TransactionBroadcastResult>(
                  this, METHODID_BROADCAST_TRANSACTION)))
          .addMethod(
            getBroadcastTransactionBytesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.TransactionBytes,
                brs.api.grpc.proto.BrsApi.TransactionBroadcastResult>(
                  this, METHODID_BROADCAST_TRANSACTION_BYTES)))
          .addMethod(
            getCompleteBasicTransactionMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.BasicTransaction,
                brs.api.grpc.proto.BrsApi.BasicTransaction>(
                  this, METHODID_COMPLETE_BASIC_TRANSACTION)))
          .addMethod(
            getGetAccountMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetAccountRequest,
                brs.api.grpc.proto.BrsApi.Account>(
                  this, METHODID_GET_ACCOUNT)))
          .addMethod(
            getGetAccountATsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetAccountRequest,
                brs.api.grpc.proto.BrsApi.AccountATs>(
                  this, METHODID_GET_ACCOUNT_ATS)))
          .addMethod(
            getGetAccountBlocksMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetAccountBlocksRequest,
                brs.api.grpc.proto.BrsApi.Blocks>(
                  this, METHODID_GET_ACCOUNT_BLOCKS)))
          .addMethod(
            getGetAccountCurrentOrdersMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetAccountOrdersRequest,
                brs.api.grpc.proto.BrsApi.Orders>(
                  this, METHODID_GET_ACCOUNT_CURRENT_ORDERS)))
          .addMethod(
            getGetAccountEscrowTransactionsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetAccountRequest,
                brs.api.grpc.proto.BrsApi.EscrowTransactions>(
                  this, METHODID_GET_ACCOUNT_ESCROW_TRANSACTIONS)))
          .addMethod(
            getGetAccountsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetAccountsRequest,
                brs.api.grpc.proto.BrsApi.Accounts>(
                  this, METHODID_GET_ACCOUNTS)))
          .addMethod(
            getGetAccountSubscriptionsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetAccountRequest,
                brs.api.grpc.proto.BrsApi.Subscriptions>(
                  this, METHODID_GET_ACCOUNT_SUBSCRIPTIONS)))
          .addMethod(
            getGetAccountTransactionsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetAccountTransactionsRequest,
                brs.api.grpc.proto.BrsApi.Transactions>(
                  this, METHODID_GET_ACCOUNT_TRANSACTIONS)))
          .addMethod(
            getGetAliasMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetAliasRequest,
                brs.api.grpc.proto.BrsApi.Alias>(
                  this, METHODID_GET_ALIAS)))
          .addMethod(
            getGetAliasesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetAliasesRequest,
                brs.api.grpc.proto.BrsApi.Aliases>(
                  this, METHODID_GET_ALIASES)))
          .addMethod(
            getGetAssetMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetByIdRequest,
                brs.api.grpc.proto.BrsApi.Asset>(
                  this, METHODID_GET_ASSET)))
          .addMethod(
            getGetAssetBalancesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetAssetBalancesRequest,
                brs.api.grpc.proto.BrsApi.AssetBalances>(
                  this, METHODID_GET_ASSET_BALANCES)))
          .addMethod(
            getGetAssetsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetAssetsRequest,
                brs.api.grpc.proto.BrsApi.Assets>(
                  this, METHODID_GET_ASSETS)))
          .addMethod(
            getGetAssetsByIssuerMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetAccountRequest,
                brs.api.grpc.proto.BrsApi.Assets>(
                  this, METHODID_GET_ASSETS_BY_ISSUER)))
          .addMethod(
            getGetAssetTradesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest,
                brs.api.grpc.proto.BrsApi.AssetTrades>(
                  this, METHODID_GET_ASSET_TRADES)))
          .addMethod(
            getGetAssetTransfersMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest,
                brs.api.grpc.proto.BrsApi.AssetTransfers>(
                  this, METHODID_GET_ASSET_TRANSFERS)))
          .addMethod(
            getGetATMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetByIdRequest,
                brs.api.grpc.proto.BrsApi.AT>(
                  this, METHODID_GET_AT)))
          .addMethod(
            getGetATIdsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                brs.api.grpc.proto.BrsApi.ATIds>(
                  this, METHODID_GET_ATIDS)))
          .addMethod(
            getGetBlockMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetBlockRequest,
                brs.api.grpc.proto.BrsApi.Block>(
                  this, METHODID_GET_BLOCK)))
          .addMethod(
            getGetBlocksMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetBlocksRequest,
                brs.api.grpc.proto.BrsApi.Blocks>(
                  this, METHODID_GET_BLOCKS)))
          .addMethod(
            getGetConstantsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                brs.api.grpc.proto.BrsApi.Constants>(
                  this, METHODID_GET_CONSTANTS)))
          .addMethod(
            getGetCountsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                brs.api.grpc.proto.BrsApi.Counts>(
                  this, METHODID_GET_COUNTS)))
          .addMethod(
            getGetCurrentTimeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                brs.api.grpc.proto.BrsApi.Time>(
                  this, METHODID_GET_CURRENT_TIME)))
          .addMethod(
            getGetDgsGoodMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetByIdRequest,
                brs.api.grpc.proto.BrsApi.DgsGood>(
                  this, METHODID_GET_DGS_GOOD)))
          .addMethod(
            getGetDgsGoodsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetDgsGoodsRequest,
                brs.api.grpc.proto.BrsApi.DgsGoods>(
                  this, METHODID_GET_DGS_GOODS)))
          .addMethod(
            getGetDgsPendingPurchasesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetDgsPendingPurchasesRequest,
                brs.api.grpc.proto.BrsApi.DgsPurchases>(
                  this, METHODID_GET_DGS_PENDING_PURCHASES)))
          .addMethod(
            getGetDgsPurchaseMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetByIdRequest,
                brs.api.grpc.proto.BrsApi.DgsPurchase>(
                  this, METHODID_GET_DGS_PURCHASE)))
          .addMethod(
            getGetDgsPurchasesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetDgsPurchasesRequest,
                brs.api.grpc.proto.BrsApi.DgsPurchases>(
                  this, METHODID_GET_DGS_PURCHASES)))
          .addMethod(
            getGetEscrowTransactionMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetByIdRequest,
                brs.api.grpc.proto.BrsApi.EscrowTransaction>(
                  this, METHODID_GET_ESCROW_TRANSACTION)))
          .addMethod(
            getGetMiningInfoMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                brs.api.grpc.proto.BrsApi.MiningInfo>(
                  this, METHODID_GET_MINING_INFO)))
          .addMethod(
            getGetOrderMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetOrderRequest,
                brs.api.grpc.proto.BrsApi.Order>(
                  this, METHODID_GET_ORDER)))
          .addMethod(
            getGetOrdersMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetOrdersRequest,
                brs.api.grpc.proto.BrsApi.Orders>(
                  this, METHODID_GET_ORDERS)))
          .addMethod(
            getGetPeerMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetPeerRequest,
                brs.api.grpc.proto.BrsApi.Peer>(
                  this, METHODID_GET_PEER)))
          .addMethod(
            getGetPeersMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetPeersRequest,
                brs.api.grpc.proto.BrsApi.Peers>(
                  this, METHODID_GET_PEERS)))
          .addMethod(
            getGetStateMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                brs.api.grpc.proto.BrsApi.State>(
                  this, METHODID_GET_STATE)))
          .addMethod(
            getGetSubscriptionMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetByIdRequest,
                brs.api.grpc.proto.BrsApi.Subscription>(
                  this, METHODID_GET_SUBSCRIPTION)))
          .addMethod(
            getGetSubscriptionsToAccountMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetAccountRequest,
                brs.api.grpc.proto.BrsApi.Subscriptions>(
                  this, METHODID_GET_SUBSCRIPTIONS_TO_ACCOUNT)))
          .addMethod(
            getGetTransactionMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetTransactionRequest,
                brs.api.grpc.proto.BrsApi.Transaction>(
                  this, METHODID_GET_TRANSACTION)))
          .addMethod(
            getGetTransactionBytesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.BasicTransaction,
                brs.api.grpc.proto.BrsApi.TransactionBytes>(
                  this, METHODID_GET_TRANSACTION_BYTES)))
          .addMethod(
            getGetUnconfirmedTransactionsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.GetAccountRequest,
                brs.api.grpc.proto.BrsApi.UnconfirmedTransactions>(
                  this, METHODID_GET_UNCONFIRMED_TRANSACTIONS)))
          .addMethod(
            getParseTransactionMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.TransactionBytes,
                brs.api.grpc.proto.BrsApi.BasicTransaction>(
                  this, METHODID_PARSE_TRANSACTION)))
          .addMethod(
            getSubmitNonceMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.BrsApi.SubmitNonceRequest,
                brs.api.grpc.proto.BrsApi.SubmitNonceResponse>(
                  this, METHODID_SUBMIT_NONCE)))
          .addMethod(
            getSuggestFeeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                brs.api.grpc.proto.BrsApi.FeeSuggestion>(
                  this, METHODID_SUGGEST_FEE)))
          .build();
    }
  }

  /**
   */
  public static final class BrsApiServiceStub extends io.grpc.stub.AbstractAsyncStub<BrsApiServiceStub> {
    private BrsApiServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BrsApiServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BrsApiServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Broadcast a transaction to the network.
     * </pre>
     */
    public void broadcastTransaction(brs.api.grpc.proto.BrsApi.BasicTransaction request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.TransactionBroadcastResult> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getBroadcastTransactionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Broadcast a transaction to the network. Takes transaction bytes instead of a BasicTransaction
     * </pre>
     */
    public void broadcastTransactionBytes(brs.api.grpc.proto.BrsApi.TransactionBytes request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.TransactionBroadcastResult> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getBroadcastTransactionBytesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Automatically fills in the following fields: Version (based on current transaction version), type and subtype (based on specified attachment), timestamp (current time). Additionally sets attachment to ordinary payment if it was not set
     * </pre>
     */
    public void completeBasicTransaction(brs.api.grpc.proto.BrsApi.BasicTransaction request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.BasicTransaction> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCompleteBasicTransactionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getAccount(brs.api.grpc.proto.BrsApi.GetAccountRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Account> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetAccountMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get the ATs that an account is the creator of, by the creator's account ID
     * </pre>
     */
    public void getAccountATs(brs.api.grpc.proto.BrsApi.GetAccountRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.AccountATs> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetAccountATsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get the blocks that an account has forged, by the forger's ID
     * </pre>
     */
    public void getAccountBlocks(brs.api.grpc.proto.BrsApi.GetAccountBlocksRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Blocks> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetAccountBlocksMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getAccountCurrentOrders(brs.api.grpc.proto.BrsApi.GetAccountOrdersRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Orders> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetAccountCurrentOrdersMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getAccountEscrowTransactions(brs.api.grpc.proto.BrsApi.GetAccountRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.EscrowTransactions> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetAccountEscrowTransactionsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get multiple accounts based on the criteria described in GetAccountsRequest. The criteria is an OR selection - I.E. if you specified a reward recipient and a name it would include accounts that have that recipient and that name, including duplicates. Therefore it is recommended to only select one criteria
     * </pre>
     */
    public void getAccounts(brs.api.grpc.proto.BrsApi.GetAccountsRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Accounts> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetAccountsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getAccountSubscriptions(brs.api.grpc.proto.BrsApi.GetAccountRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Subscriptions> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetAccountSubscriptionsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getAccountTransactions(brs.api.grpc.proto.BrsApi.GetAccountTransactionsRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Transactions> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetAccountTransactionsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getAlias(brs.api.grpc.proto.BrsApi.GetAliasRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Alias> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetAliasMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getAliases(brs.api.grpc.proto.BrsApi.GetAliasesRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Aliases> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetAliasesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getAsset(brs.api.grpc.proto.BrsApi.GetByIdRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Asset> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetAssetMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get an asset's holders and their balances
     * </pre>
     */
    public void getAssetBalances(brs.api.grpc.proto.BrsApi.GetAssetBalancesRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.AssetBalances> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetAssetBalancesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get multiple assets in one go.
     * </pre>
     */
    public void getAssets(brs.api.grpc.proto.BrsApi.GetAssetsRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Assets> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetAssetsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get all assets issued by an account
     * </pre>
     */
    public void getAssetsByIssuer(brs.api.grpc.proto.BrsApi.GetAccountRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Assets> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetAssetsByIssuerMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get all asset trades made by an account
     * </pre>
     */
    public void getAssetTrades(brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.AssetTrades> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetAssetTradesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get all asset transfers made by an account
     * </pre>
     */
    public void getAssetTransfers(brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.AssetTransfers> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetAssetTransfersMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getAT(brs.api.grpc.proto.BrsApi.GetByIdRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.AT> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetATMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get all active AT IDs
     * </pre>
     */
    public void getATIds(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.ATIds> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetATIdsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get a block by ID, height or timestamp
     * </pre>
     */
    public void getBlock(brs.api.grpc.proto.BrsApi.GetBlockRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Block> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetBlockMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get the most recent blocks
     * </pre>
     */
    public void getBlocks(brs.api.grpc.proto.BrsApi.GetBlocksRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Blocks> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetBlocksMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get the current blockchain constants
     * </pre>
     */
    public void getConstants(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Constants> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetConstantsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get the counts of different blockchain entities
     * </pre>
     */
    public void getCounts(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Counts> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetCountsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get the current Burst time (number of seconds since Burst epoch)
     * </pre>
     */
    public void getCurrentTime(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Time> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetCurrentTimeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getDgsGood(brs.api.grpc.proto.BrsApi.GetByIdRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.DgsGood> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetDgsGoodMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getDgsGoods(brs.api.grpc.proto.BrsApi.GetDgsGoodsRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.DgsGoods> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetDgsGoodsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getDgsPendingPurchases(brs.api.grpc.proto.BrsApi.GetDgsPendingPurchasesRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.DgsPurchases> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetDgsPendingPurchasesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getDgsPurchase(brs.api.grpc.proto.BrsApi.GetByIdRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.DgsPurchase> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetDgsPurchaseMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getDgsPurchases(brs.api.grpc.proto.BrsApi.GetDgsPurchasesRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.DgsPurchases> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetDgsPurchasesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getEscrowTransaction(brs.api.grpc.proto.BrsApi.GetByIdRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.EscrowTransaction> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetEscrowTransactionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get the mining info for the next block. This is needed to mine.
     * </pre>
     */
    public void getMiningInfo(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.MiningInfo> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetMiningInfoMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getOrder(brs.api.grpc.proto.BrsApi.GetOrderRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Order> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetOrderMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getOrders(brs.api.grpc.proto.BrsApi.GetOrdersRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Orders> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetOrdersMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getPeer(brs.api.grpc.proto.BrsApi.GetPeerRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Peer> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetPeerMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getPeers(brs.api.grpc.proto.BrsApi.GetPeersRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Peers> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetPeersMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get the current server state
     * </pre>
     */
    public void getState(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.State> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetStateMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getSubscription(brs.api.grpc.proto.BrsApi.GetByIdRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Subscription> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetSubscriptionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getSubscriptionsToAccount(brs.api.grpc.proto.BrsApi.GetAccountRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Subscriptions> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetSubscriptionsToAccountMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getTransaction(brs.api.grpc.proto.BrsApi.GetTransactionRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Transaction> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetTransactionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Convert a BasicTranscation into its transaction bytes, to be signed. This theoretically can be done offline so will be removed in the future.
     * </pre>
     */
    public void getTransactionBytes(brs.api.grpc.proto.BrsApi.BasicTransaction request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.TransactionBytes> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetTransactionBytesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getUnconfirmedTransactions(brs.api.grpc.proto.BrsApi.GetAccountRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.UnconfirmedTransactions> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetUnconfirmedTransactionsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Convert TransactionBytes into a BasicTransaction. This theoretically can be done offline so will be removed in the future.
     * </pre>
     */
    public void parseTransaction(brs.api.grpc.proto.BrsApi.TransactionBytes request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.BasicTransaction> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getParseTransactionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Submit a nonce to try to forge a block. This requires the passphrase to be sent to the server so should only be performed on local nodes.
     * </pre>
     */
    public void submitNonce(brs.api.grpc.proto.BrsApi.SubmitNonceRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.SubmitNonceResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSubmitNonceMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Suggest a fee to use for a transaction
     * </pre>
     */
    public void suggestFee(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.FeeSuggestion> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSuggestFeeMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class BrsApiServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<BrsApiServiceBlockingStub> {
    private BrsApiServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BrsApiServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BrsApiServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Broadcast a transaction to the network.
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.TransactionBroadcastResult broadcastTransaction(brs.api.grpc.proto.BrsApi.BasicTransaction request) {
      return blockingUnaryCall(
          getChannel(), getBroadcastTransactionMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Broadcast a transaction to the network. Takes transaction bytes instead of a BasicTransaction
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.TransactionBroadcastResult broadcastTransactionBytes(brs.api.grpc.proto.BrsApi.TransactionBytes request) {
      return blockingUnaryCall(
          getChannel(), getBroadcastTransactionBytesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Automatically fills in the following fields: Version (based on current transaction version), type and subtype (based on specified attachment), timestamp (current time). Additionally sets attachment to ordinary payment if it was not set
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.BasicTransaction completeBasicTransaction(brs.api.grpc.proto.BrsApi.BasicTransaction request) {
      return blockingUnaryCall(
          getChannel(), getCompleteBasicTransactionMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.Account getAccount(brs.api.grpc.proto.BrsApi.GetAccountRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetAccountMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get the ATs that an account is the creator of, by the creator's account ID
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.AccountATs getAccountATs(brs.api.grpc.proto.BrsApi.GetAccountRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetAccountATsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get the blocks that an account has forged, by the forger's ID
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.Blocks getAccountBlocks(brs.api.grpc.proto.BrsApi.GetAccountBlocksRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetAccountBlocksMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.Orders getAccountCurrentOrders(brs.api.grpc.proto.BrsApi.GetAccountOrdersRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetAccountCurrentOrdersMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.EscrowTransactions getAccountEscrowTransactions(brs.api.grpc.proto.BrsApi.GetAccountRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetAccountEscrowTransactionsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get multiple accounts based on the criteria described in GetAccountsRequest. The criteria is an OR selection - I.E. if you specified a reward recipient and a name it would include accounts that have that recipient and that name, including duplicates. Therefore it is recommended to only select one criteria
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.Accounts getAccounts(brs.api.grpc.proto.BrsApi.GetAccountsRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetAccountsMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.Subscriptions getAccountSubscriptions(brs.api.grpc.proto.BrsApi.GetAccountRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetAccountSubscriptionsMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.Transactions getAccountTransactions(brs.api.grpc.proto.BrsApi.GetAccountTransactionsRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetAccountTransactionsMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.Alias getAlias(brs.api.grpc.proto.BrsApi.GetAliasRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetAliasMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.Aliases getAliases(brs.api.grpc.proto.BrsApi.GetAliasesRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetAliasesMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.Asset getAsset(brs.api.grpc.proto.BrsApi.GetByIdRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetAssetMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get an asset's holders and their balances
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.AssetBalances getAssetBalances(brs.api.grpc.proto.BrsApi.GetAssetBalancesRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetAssetBalancesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get multiple assets in one go.
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.Assets getAssets(brs.api.grpc.proto.BrsApi.GetAssetsRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetAssetsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get all assets issued by an account
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.Assets getAssetsByIssuer(brs.api.grpc.proto.BrsApi.GetAccountRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetAssetsByIssuerMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get all asset trades made by an account
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.AssetTrades getAssetTrades(brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetAssetTradesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get all asset transfers made by an account
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.AssetTransfers getAssetTransfers(brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetAssetTransfersMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.AT getAT(brs.api.grpc.proto.BrsApi.GetByIdRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetATMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get all active AT IDs
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.ATIds getATIds(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), getGetATIdsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get a block by ID, height or timestamp
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.Block getBlock(brs.api.grpc.proto.BrsApi.GetBlockRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetBlockMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get the most recent blocks
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.Blocks getBlocks(brs.api.grpc.proto.BrsApi.GetBlocksRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetBlocksMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get the current blockchain constants
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.Constants getConstants(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), getGetConstantsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get the counts of different blockchain entities
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.Counts getCounts(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), getGetCountsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get the current Burst time (number of seconds since Burst epoch)
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.Time getCurrentTime(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), getGetCurrentTimeMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.DgsGood getDgsGood(brs.api.grpc.proto.BrsApi.GetByIdRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetDgsGoodMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.DgsGoods getDgsGoods(brs.api.grpc.proto.BrsApi.GetDgsGoodsRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetDgsGoodsMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.DgsPurchases getDgsPendingPurchases(brs.api.grpc.proto.BrsApi.GetDgsPendingPurchasesRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetDgsPendingPurchasesMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.DgsPurchase getDgsPurchase(brs.api.grpc.proto.BrsApi.GetByIdRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetDgsPurchaseMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.DgsPurchases getDgsPurchases(brs.api.grpc.proto.BrsApi.GetDgsPurchasesRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetDgsPurchasesMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.EscrowTransaction getEscrowTransaction(brs.api.grpc.proto.BrsApi.GetByIdRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetEscrowTransactionMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get the mining info for the next block. This is needed to mine.
     * </pre>
     */
    public java.util.Iterator<brs.api.grpc.proto.BrsApi.MiningInfo> getMiningInfo(
        com.google.protobuf.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getGetMiningInfoMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.Order getOrder(brs.api.grpc.proto.BrsApi.GetOrderRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetOrderMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.Orders getOrders(brs.api.grpc.proto.BrsApi.GetOrdersRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetOrdersMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.Peer getPeer(brs.api.grpc.proto.BrsApi.GetPeerRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetPeerMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.Peers getPeers(brs.api.grpc.proto.BrsApi.GetPeersRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetPeersMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get the current server state
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.State getState(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), getGetStateMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.Subscription getSubscription(brs.api.grpc.proto.BrsApi.GetByIdRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetSubscriptionMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.Subscriptions getSubscriptionsToAccount(brs.api.grpc.proto.BrsApi.GetAccountRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetSubscriptionsToAccountMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.Transaction getTransaction(brs.api.grpc.proto.BrsApi.GetTransactionRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetTransactionMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Convert a BasicTranscation into its transaction bytes, to be signed. This theoretically can be done offline so will be removed in the future.
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.TransactionBytes getTransactionBytes(brs.api.grpc.proto.BrsApi.BasicTransaction request) {
      return blockingUnaryCall(
          getChannel(), getGetTransactionBytesMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.BrsApi.UnconfirmedTransactions getUnconfirmedTransactions(brs.api.grpc.proto.BrsApi.GetAccountRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetUnconfirmedTransactionsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Convert TransactionBytes into a BasicTransaction. This theoretically can be done offline so will be removed in the future.
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.BasicTransaction parseTransaction(brs.api.grpc.proto.BrsApi.TransactionBytes request) {
      return blockingUnaryCall(
          getChannel(), getParseTransactionMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Submit a nonce to try to forge a block. This requires the passphrase to be sent to the server so should only be performed on local nodes.
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.SubmitNonceResponse submitNonce(brs.api.grpc.proto.BrsApi.SubmitNonceRequest request) {
      return blockingUnaryCall(
          getChannel(), getSubmitNonceMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Suggest a fee to use for a transaction
     * </pre>
     */
    public brs.api.grpc.proto.BrsApi.FeeSuggestion suggestFee(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), getSuggestFeeMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class BrsApiServiceFutureStub extends io.grpc.stub.AbstractFutureStub<BrsApiServiceFutureStub> {
    private BrsApiServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BrsApiServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BrsApiServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Broadcast a transaction to the network.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.TransactionBroadcastResult> broadcastTransaction(
        brs.api.grpc.proto.BrsApi.BasicTransaction request) {
      return futureUnaryCall(
          getChannel().newCall(getBroadcastTransactionMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Broadcast a transaction to the network. Takes transaction bytes instead of a BasicTransaction
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.TransactionBroadcastResult> broadcastTransactionBytes(
        brs.api.grpc.proto.BrsApi.TransactionBytes request) {
      return futureUnaryCall(
          getChannel().newCall(getBroadcastTransactionBytesMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Automatically fills in the following fields: Version (based on current transaction version), type and subtype (based on specified attachment), timestamp (current time). Additionally sets attachment to ordinary payment if it was not set
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.BasicTransaction> completeBasicTransaction(
        brs.api.grpc.proto.BrsApi.BasicTransaction request) {
      return futureUnaryCall(
          getChannel().newCall(getCompleteBasicTransactionMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Account> getAccount(
        brs.api.grpc.proto.BrsApi.GetAccountRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetAccountMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get the ATs that an account is the creator of, by the creator's account ID
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.AccountATs> getAccountATs(
        brs.api.grpc.proto.BrsApi.GetAccountRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetAccountATsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get the blocks that an account has forged, by the forger's ID
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Blocks> getAccountBlocks(
        brs.api.grpc.proto.BrsApi.GetAccountBlocksRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetAccountBlocksMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Orders> getAccountCurrentOrders(
        brs.api.grpc.proto.BrsApi.GetAccountOrdersRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetAccountCurrentOrdersMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.EscrowTransactions> getAccountEscrowTransactions(
        brs.api.grpc.proto.BrsApi.GetAccountRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetAccountEscrowTransactionsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get multiple accounts based on the criteria described in GetAccountsRequest. The criteria is an OR selection - I.E. if you specified a reward recipient and a name it would include accounts that have that recipient and that name, including duplicates. Therefore it is recommended to only select one criteria
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Accounts> getAccounts(
        brs.api.grpc.proto.BrsApi.GetAccountsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetAccountsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Subscriptions> getAccountSubscriptions(
        brs.api.grpc.proto.BrsApi.GetAccountRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetAccountSubscriptionsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Transactions> getAccountTransactions(
        brs.api.grpc.proto.BrsApi.GetAccountTransactionsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetAccountTransactionsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Alias> getAlias(
        brs.api.grpc.proto.BrsApi.GetAliasRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetAliasMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Aliases> getAliases(
        brs.api.grpc.proto.BrsApi.GetAliasesRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetAliasesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Asset> getAsset(
        brs.api.grpc.proto.BrsApi.GetByIdRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetAssetMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get an asset's holders and their balances
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.AssetBalances> getAssetBalances(
        brs.api.grpc.proto.BrsApi.GetAssetBalancesRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetAssetBalancesMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get multiple assets in one go.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Assets> getAssets(
        brs.api.grpc.proto.BrsApi.GetAssetsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetAssetsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get all assets issued by an account
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Assets> getAssetsByIssuer(
        brs.api.grpc.proto.BrsApi.GetAccountRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetAssetsByIssuerMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get all asset trades made by an account
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.AssetTrades> getAssetTrades(
        brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetAssetTradesMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get all asset transfers made by an account
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.AssetTransfers> getAssetTransfers(
        brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetAssetTransfersMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.AT> getAT(
        brs.api.grpc.proto.BrsApi.GetByIdRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetATMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get all active AT IDs
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.ATIds> getATIds(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getGetATIdsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get a block by ID, height or timestamp
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Block> getBlock(
        brs.api.grpc.proto.BrsApi.GetBlockRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetBlockMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get the most recent blocks
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Blocks> getBlocks(
        brs.api.grpc.proto.BrsApi.GetBlocksRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetBlocksMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get the current blockchain constants
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Constants> getConstants(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getGetConstantsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get the counts of different blockchain entities
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Counts> getCounts(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getGetCountsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get the current Burst time (number of seconds since Burst epoch)
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Time> getCurrentTime(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getGetCurrentTimeMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.DgsGood> getDgsGood(
        brs.api.grpc.proto.BrsApi.GetByIdRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetDgsGoodMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.DgsGoods> getDgsGoods(
        brs.api.grpc.proto.BrsApi.GetDgsGoodsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetDgsGoodsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.DgsPurchases> getDgsPendingPurchases(
        brs.api.grpc.proto.BrsApi.GetDgsPendingPurchasesRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetDgsPendingPurchasesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.DgsPurchase> getDgsPurchase(
        brs.api.grpc.proto.BrsApi.GetByIdRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetDgsPurchaseMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.DgsPurchases> getDgsPurchases(
        brs.api.grpc.proto.BrsApi.GetDgsPurchasesRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetDgsPurchasesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.EscrowTransaction> getEscrowTransaction(
        brs.api.grpc.proto.BrsApi.GetByIdRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetEscrowTransactionMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Order> getOrder(
        brs.api.grpc.proto.BrsApi.GetOrderRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetOrderMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Orders> getOrders(
        brs.api.grpc.proto.BrsApi.GetOrdersRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetOrdersMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Peer> getPeer(
        brs.api.grpc.proto.BrsApi.GetPeerRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetPeerMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Peers> getPeers(
        brs.api.grpc.proto.BrsApi.GetPeersRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetPeersMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get the current server state
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.State> getState(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getGetStateMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Subscription> getSubscription(
        brs.api.grpc.proto.BrsApi.GetByIdRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetSubscriptionMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Subscriptions> getSubscriptionsToAccount(
        brs.api.grpc.proto.BrsApi.GetAccountRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetSubscriptionsToAccountMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.Transaction> getTransaction(
        brs.api.grpc.proto.BrsApi.GetTransactionRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetTransactionMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Convert a BasicTranscation into its transaction bytes, to be signed. This theoretically can be done offline so will be removed in the future.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.TransactionBytes> getTransactionBytes(
        brs.api.grpc.proto.BrsApi.BasicTransaction request) {
      return futureUnaryCall(
          getChannel().newCall(getGetTransactionBytesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.UnconfirmedTransactions> getUnconfirmedTransactions(
        brs.api.grpc.proto.BrsApi.GetAccountRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetUnconfirmedTransactionsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Convert TransactionBytes into a BasicTransaction. This theoretically can be done offline so will be removed in the future.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.BasicTransaction> parseTransaction(
        brs.api.grpc.proto.BrsApi.TransactionBytes request) {
      return futureUnaryCall(
          getChannel().newCall(getParseTransactionMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Submit a nonce to try to forge a block. This requires the passphrase to be sent to the server so should only be performed on local nodes.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.SubmitNonceResponse> submitNonce(
        brs.api.grpc.proto.BrsApi.SubmitNonceRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getSubmitNonceMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Suggest a fee to use for a transaction
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.BrsApi.FeeSuggestion> suggestFee(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getSuggestFeeMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_BROADCAST_TRANSACTION = 0;
  private static final int METHODID_BROADCAST_TRANSACTION_BYTES = 1;
  private static final int METHODID_COMPLETE_BASIC_TRANSACTION = 2;
  private static final int METHODID_GET_ACCOUNT = 3;
  private static final int METHODID_GET_ACCOUNT_ATS = 4;
  private static final int METHODID_GET_ACCOUNT_BLOCKS = 5;
  private static final int METHODID_GET_ACCOUNT_CURRENT_ORDERS = 6;
  private static final int METHODID_GET_ACCOUNT_ESCROW_TRANSACTIONS = 7;
  private static final int METHODID_GET_ACCOUNTS = 8;
  private static final int METHODID_GET_ACCOUNT_SUBSCRIPTIONS = 9;
  private static final int METHODID_GET_ACCOUNT_TRANSACTIONS = 10;
  private static final int METHODID_GET_ALIAS = 11;
  private static final int METHODID_GET_ALIASES = 12;
  private static final int METHODID_GET_ASSET = 13;
  private static final int METHODID_GET_ASSET_BALANCES = 14;
  private static final int METHODID_GET_ASSETS = 15;
  private static final int METHODID_GET_ASSETS_BY_ISSUER = 16;
  private static final int METHODID_GET_ASSET_TRADES = 17;
  private static final int METHODID_GET_ASSET_TRANSFERS = 18;
  private static final int METHODID_GET_AT = 19;
  private static final int METHODID_GET_ATIDS = 20;
  private static final int METHODID_GET_BLOCK = 21;
  private static final int METHODID_GET_BLOCKS = 22;
  private static final int METHODID_GET_CONSTANTS = 23;
  private static final int METHODID_GET_COUNTS = 24;
  private static final int METHODID_GET_CURRENT_TIME = 25;
  private static final int METHODID_GET_DGS_GOOD = 26;
  private static final int METHODID_GET_DGS_GOODS = 27;
  private static final int METHODID_GET_DGS_PENDING_PURCHASES = 28;
  private static final int METHODID_GET_DGS_PURCHASE = 29;
  private static final int METHODID_GET_DGS_PURCHASES = 30;
  private static final int METHODID_GET_ESCROW_TRANSACTION = 31;
  private static final int METHODID_GET_MINING_INFO = 32;
  private static final int METHODID_GET_ORDER = 33;
  private static final int METHODID_GET_ORDERS = 34;
  private static final int METHODID_GET_PEER = 35;
  private static final int METHODID_GET_PEERS = 36;
  private static final int METHODID_GET_STATE = 37;
  private static final int METHODID_GET_SUBSCRIPTION = 38;
  private static final int METHODID_GET_SUBSCRIPTIONS_TO_ACCOUNT = 39;
  private static final int METHODID_GET_TRANSACTION = 40;
  private static final int METHODID_GET_TRANSACTION_BYTES = 41;
  private static final int METHODID_GET_UNCONFIRMED_TRANSACTIONS = 42;
  private static final int METHODID_PARSE_TRANSACTION = 43;
  private static final int METHODID_SUBMIT_NONCE = 44;
  private static final int METHODID_SUGGEST_FEE = 45;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final BrsApiServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(BrsApiServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_BROADCAST_TRANSACTION:
          serviceImpl.broadcastTransaction((brs.api.grpc.proto.BrsApi.BasicTransaction) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.TransactionBroadcastResult>) responseObserver);
          break;
        case METHODID_BROADCAST_TRANSACTION_BYTES:
          serviceImpl.broadcastTransactionBytes((brs.api.grpc.proto.BrsApi.TransactionBytes) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.TransactionBroadcastResult>) responseObserver);
          break;
        case METHODID_COMPLETE_BASIC_TRANSACTION:
          serviceImpl.completeBasicTransaction((brs.api.grpc.proto.BrsApi.BasicTransaction) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.BasicTransaction>) responseObserver);
          break;
        case METHODID_GET_ACCOUNT:
          serviceImpl.getAccount((brs.api.grpc.proto.BrsApi.GetAccountRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Account>) responseObserver);
          break;
        case METHODID_GET_ACCOUNT_ATS:
          serviceImpl.getAccountATs((brs.api.grpc.proto.BrsApi.GetAccountRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.AccountATs>) responseObserver);
          break;
        case METHODID_GET_ACCOUNT_BLOCKS:
          serviceImpl.getAccountBlocks((brs.api.grpc.proto.BrsApi.GetAccountBlocksRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Blocks>) responseObserver);
          break;
        case METHODID_GET_ACCOUNT_CURRENT_ORDERS:
          serviceImpl.getAccountCurrentOrders((brs.api.grpc.proto.BrsApi.GetAccountOrdersRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Orders>) responseObserver);
          break;
        case METHODID_GET_ACCOUNT_ESCROW_TRANSACTIONS:
          serviceImpl.getAccountEscrowTransactions((brs.api.grpc.proto.BrsApi.GetAccountRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.EscrowTransactions>) responseObserver);
          break;
        case METHODID_GET_ACCOUNTS:
          serviceImpl.getAccounts((brs.api.grpc.proto.BrsApi.GetAccountsRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Accounts>) responseObserver);
          break;
        case METHODID_GET_ACCOUNT_SUBSCRIPTIONS:
          serviceImpl.getAccountSubscriptions((brs.api.grpc.proto.BrsApi.GetAccountRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Subscriptions>) responseObserver);
          break;
        case METHODID_GET_ACCOUNT_TRANSACTIONS:
          serviceImpl.getAccountTransactions((brs.api.grpc.proto.BrsApi.GetAccountTransactionsRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Transactions>) responseObserver);
          break;
        case METHODID_GET_ALIAS:
          serviceImpl.getAlias((brs.api.grpc.proto.BrsApi.GetAliasRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Alias>) responseObserver);
          break;
        case METHODID_GET_ALIASES:
          serviceImpl.getAliases((brs.api.grpc.proto.BrsApi.GetAliasesRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Aliases>) responseObserver);
          break;
        case METHODID_GET_ASSET:
          serviceImpl.getAsset((brs.api.grpc.proto.BrsApi.GetByIdRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Asset>) responseObserver);
          break;
        case METHODID_GET_ASSET_BALANCES:
          serviceImpl.getAssetBalances((brs.api.grpc.proto.BrsApi.GetAssetBalancesRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.AssetBalances>) responseObserver);
          break;
        case METHODID_GET_ASSETS:
          serviceImpl.getAssets((brs.api.grpc.proto.BrsApi.GetAssetsRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Assets>) responseObserver);
          break;
        case METHODID_GET_ASSETS_BY_ISSUER:
          serviceImpl.getAssetsByIssuer((brs.api.grpc.proto.BrsApi.GetAccountRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Assets>) responseObserver);
          break;
        case METHODID_GET_ASSET_TRADES:
          serviceImpl.getAssetTrades((brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.AssetTrades>) responseObserver);
          break;
        case METHODID_GET_ASSET_TRANSFERS:
          serviceImpl.getAssetTransfers((brs.api.grpc.proto.BrsApi.GetAssetTransfersRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.AssetTransfers>) responseObserver);
          break;
        case METHODID_GET_AT:
          serviceImpl.getAT((brs.api.grpc.proto.BrsApi.GetByIdRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.AT>) responseObserver);
          break;
        case METHODID_GET_ATIDS:
          serviceImpl.getATIds((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.ATIds>) responseObserver);
          break;
        case METHODID_GET_BLOCK:
          serviceImpl.getBlock((brs.api.grpc.proto.BrsApi.GetBlockRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Block>) responseObserver);
          break;
        case METHODID_GET_BLOCKS:
          serviceImpl.getBlocks((brs.api.grpc.proto.BrsApi.GetBlocksRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Blocks>) responseObserver);
          break;
        case METHODID_GET_CONSTANTS:
          serviceImpl.getConstants((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Constants>) responseObserver);
          break;
        case METHODID_GET_COUNTS:
          serviceImpl.getCounts((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Counts>) responseObserver);
          break;
        case METHODID_GET_CURRENT_TIME:
          serviceImpl.getCurrentTime((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Time>) responseObserver);
          break;
        case METHODID_GET_DGS_GOOD:
          serviceImpl.getDgsGood((brs.api.grpc.proto.BrsApi.GetByIdRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.DgsGood>) responseObserver);
          break;
        case METHODID_GET_DGS_GOODS:
          serviceImpl.getDgsGoods((brs.api.grpc.proto.BrsApi.GetDgsGoodsRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.DgsGoods>) responseObserver);
          break;
        case METHODID_GET_DGS_PENDING_PURCHASES:
          serviceImpl.getDgsPendingPurchases((brs.api.grpc.proto.BrsApi.GetDgsPendingPurchasesRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.DgsPurchases>) responseObserver);
          break;
        case METHODID_GET_DGS_PURCHASE:
          serviceImpl.getDgsPurchase((brs.api.grpc.proto.BrsApi.GetByIdRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.DgsPurchase>) responseObserver);
          break;
        case METHODID_GET_DGS_PURCHASES:
          serviceImpl.getDgsPurchases((brs.api.grpc.proto.BrsApi.GetDgsPurchasesRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.DgsPurchases>) responseObserver);
          break;
        case METHODID_GET_ESCROW_TRANSACTION:
          serviceImpl.getEscrowTransaction((brs.api.grpc.proto.BrsApi.GetByIdRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.EscrowTransaction>) responseObserver);
          break;
        case METHODID_GET_MINING_INFO:
          serviceImpl.getMiningInfo((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.MiningInfo>) responseObserver);
          break;
        case METHODID_GET_ORDER:
          serviceImpl.getOrder((brs.api.grpc.proto.BrsApi.GetOrderRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Order>) responseObserver);
          break;
        case METHODID_GET_ORDERS:
          serviceImpl.getOrders((brs.api.grpc.proto.BrsApi.GetOrdersRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Orders>) responseObserver);
          break;
        case METHODID_GET_PEER:
          serviceImpl.getPeer((brs.api.grpc.proto.BrsApi.GetPeerRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Peer>) responseObserver);
          break;
        case METHODID_GET_PEERS:
          serviceImpl.getPeers((brs.api.grpc.proto.BrsApi.GetPeersRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Peers>) responseObserver);
          break;
        case METHODID_GET_STATE:
          serviceImpl.getState((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.State>) responseObserver);
          break;
        case METHODID_GET_SUBSCRIPTION:
          serviceImpl.getSubscription((brs.api.grpc.proto.BrsApi.GetByIdRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Subscription>) responseObserver);
          break;
        case METHODID_GET_SUBSCRIPTIONS_TO_ACCOUNT:
          serviceImpl.getSubscriptionsToAccount((brs.api.grpc.proto.BrsApi.GetAccountRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Subscriptions>) responseObserver);
          break;
        case METHODID_GET_TRANSACTION:
          serviceImpl.getTransaction((brs.api.grpc.proto.BrsApi.GetTransactionRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.Transaction>) responseObserver);
          break;
        case METHODID_GET_TRANSACTION_BYTES:
          serviceImpl.getTransactionBytes((brs.api.grpc.proto.BrsApi.BasicTransaction) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.TransactionBytes>) responseObserver);
          break;
        case METHODID_GET_UNCONFIRMED_TRANSACTIONS:
          serviceImpl.getUnconfirmedTransactions((brs.api.grpc.proto.BrsApi.GetAccountRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.UnconfirmedTransactions>) responseObserver);
          break;
        case METHODID_PARSE_TRANSACTION:
          serviceImpl.parseTransaction((brs.api.grpc.proto.BrsApi.TransactionBytes) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.BasicTransaction>) responseObserver);
          break;
        case METHODID_SUBMIT_NONCE:
          serviceImpl.submitNonce((brs.api.grpc.proto.BrsApi.SubmitNonceRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.SubmitNonceResponse>) responseObserver);
          break;
        case METHODID_SUGGEST_FEE:
          serviceImpl.suggestFee((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.BrsApi.FeeSuggestion>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class BrsApiServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    BrsApiServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return brs.api.grpc.proto.BrsApi.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("BrsApiService");
    }
  }

  private static final class BrsApiServiceFileDescriptorSupplier
      extends BrsApiServiceBaseDescriptorSupplier {
    BrsApiServiceFileDescriptorSupplier() {}
  }

  private static final class BrsApiServiceMethodDescriptorSupplier
      extends BrsApiServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    BrsApiServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (BrsApiServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new BrsApiServiceFileDescriptorSupplier())
              .addMethod(getBroadcastTransactionMethod())
              .addMethod(getBroadcastTransactionBytesMethod())
              .addMethod(getCompleteBasicTransactionMethod())
              .addMethod(getGetAccountMethod())
              .addMethod(getGetAccountATsMethod())
              .addMethod(getGetAccountBlocksMethod())
              .addMethod(getGetAccountCurrentOrdersMethod())
              .addMethod(getGetAccountEscrowTransactionsMethod())
              .addMethod(getGetAccountsMethod())
              .addMethod(getGetAccountSubscriptionsMethod())
              .addMethod(getGetAccountTransactionsMethod())
              .addMethod(getGetAliasMethod())
              .addMethod(getGetAliasesMethod())
              .addMethod(getGetAssetMethod())
              .addMethod(getGetAssetBalancesMethod())
              .addMethod(getGetAssetsMethod())
              .addMethod(getGetAssetsByIssuerMethod())
              .addMethod(getGetAssetTradesMethod())
              .addMethod(getGetAssetTransfersMethod())
              .addMethod(getGetATMethod())
              .addMethod(getGetATIdsMethod())
              .addMethod(getGetBlockMethod())
              .addMethod(getGetBlocksMethod())
              .addMethod(getGetConstantsMethod())
              .addMethod(getGetCountsMethod())
              .addMethod(getGetCurrentTimeMethod())
              .addMethod(getGetDgsGoodMethod())
              .addMethod(getGetDgsGoodsMethod())
              .addMethod(getGetDgsPendingPurchasesMethod())
              .addMethod(getGetDgsPurchaseMethod())
              .addMethod(getGetDgsPurchasesMethod())
              .addMethod(getGetEscrowTransactionMethod())
              .addMethod(getGetMiningInfoMethod())
              .addMethod(getGetOrderMethod())
              .addMethod(getGetOrdersMethod())
              .addMethod(getGetPeerMethod())
              .addMethod(getGetPeersMethod())
              .addMethod(getGetStateMethod())
              .addMethod(getGetSubscriptionMethod())
              .addMethod(getGetSubscriptionsToAccountMethod())
              .addMethod(getGetTransactionMethod())
              .addMethod(getGetTransactionBytesMethod())
              .addMethod(getGetUnconfirmedTransactionsMethod())
              .addMethod(getParseTransactionMethod())
              .addMethod(getSubmitNonceMethod())
              .addMethod(getSuggestFeeMethod())
              .build();
        }
      }
    }
    return result;
  }
}
