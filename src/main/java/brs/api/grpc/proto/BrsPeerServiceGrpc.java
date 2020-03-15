package brs.api.grpc.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.28.0)",
    comments = "Source: peerApi.proto")
public final class BrsPeerServiceGrpc {

  private BrsPeerServiceGrpc() {}

  public static final String SERVICE_NAME = "brs.peer.BrsPeerService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.PeerInfo,
      brs.api.grpc.proto.PeerApi.PeerInfo> getExchangeInfoMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ExchangeInfo",
      requestType = brs.api.grpc.proto.PeerApi.PeerInfo.class,
      responseType = brs.api.grpc.proto.PeerApi.PeerInfo.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.PeerInfo,
      brs.api.grpc.proto.PeerApi.PeerInfo> getExchangeInfoMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.PeerInfo, brs.api.grpc.proto.PeerApi.PeerInfo> getExchangeInfoMethod;
    if ((getExchangeInfoMethod = BrsPeerServiceGrpc.getExchangeInfoMethod) == null) {
      synchronized (BrsPeerServiceGrpc.class) {
        if ((getExchangeInfoMethod = BrsPeerServiceGrpc.getExchangeInfoMethod) == null) {
          BrsPeerServiceGrpc.getExchangeInfoMethod = getExchangeInfoMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.PeerApi.PeerInfo, brs.api.grpc.proto.PeerApi.PeerInfo>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ExchangeInfo"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.PeerInfo.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.PeerInfo.getDefaultInstance()))
              .setSchemaDescriptor(new BrsPeerServiceMethodDescriptorSupplier("ExchangeInfo"))
              .build();
        }
      }
    }
    return getExchangeInfoMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.Peers,
      com.google.protobuf.Empty> getAddPeersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AddPeers",
      requestType = brs.api.grpc.proto.PeerApi.Peers.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.Peers,
      com.google.protobuf.Empty> getAddPeersMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.Peers, com.google.protobuf.Empty> getAddPeersMethod;
    if ((getAddPeersMethod = BrsPeerServiceGrpc.getAddPeersMethod) == null) {
      synchronized (BrsPeerServiceGrpc.class) {
        if ((getAddPeersMethod = BrsPeerServiceGrpc.getAddPeersMethod) == null) {
          BrsPeerServiceGrpc.getAddPeersMethod = getAddPeersMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.PeerApi.Peers, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AddPeers"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.Peers.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new BrsPeerServiceMethodDescriptorSupplier("AddPeers"))
              .build();
        }
      }
    }
    return getAddPeersMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.PeerApi.Peers> getGetPeersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPeers",
      requestType = com.google.protobuf.Empty.class,
      responseType = brs.api.grpc.proto.PeerApi.Peers.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.PeerApi.Peers> getGetPeersMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, brs.api.grpc.proto.PeerApi.Peers> getGetPeersMethod;
    if ((getGetPeersMethod = BrsPeerServiceGrpc.getGetPeersMethod) == null) {
      synchronized (BrsPeerServiceGrpc.class) {
        if ((getGetPeersMethod = BrsPeerServiceGrpc.getGetPeersMethod) == null) {
          BrsPeerServiceGrpc.getGetPeersMethod = getGetPeersMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, brs.api.grpc.proto.PeerApi.Peers>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetPeers"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.Peers.getDefaultInstance()))
              .setSchemaDescriptor(new BrsPeerServiceMethodDescriptorSupplier("GetPeers"))
              .build();
        }
      }
    }
    return getGetPeersMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.PeerApi.CumulativeDifficulty> getGetCumulativeDifficultyMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetCumulativeDifficulty",
      requestType = com.google.protobuf.Empty.class,
      responseType = brs.api.grpc.proto.PeerApi.CumulativeDifficulty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.PeerApi.CumulativeDifficulty> getGetCumulativeDifficultyMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, brs.api.grpc.proto.PeerApi.CumulativeDifficulty> getGetCumulativeDifficultyMethod;
    if ((getGetCumulativeDifficultyMethod = BrsPeerServiceGrpc.getGetCumulativeDifficultyMethod) == null) {
      synchronized (BrsPeerServiceGrpc.class) {
        if ((getGetCumulativeDifficultyMethod = BrsPeerServiceGrpc.getGetCumulativeDifficultyMethod) == null) {
          BrsPeerServiceGrpc.getGetCumulativeDifficultyMethod = getGetCumulativeDifficultyMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, brs.api.grpc.proto.PeerApi.CumulativeDifficulty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetCumulativeDifficulty"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.CumulativeDifficulty.getDefaultInstance()))
              .setSchemaDescriptor(new BrsPeerServiceMethodDescriptorSupplier("GetCumulativeDifficulty"))
              .build();
        }
      }
    }
    return getGetCumulativeDifficultyMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.GetMilestoneBlockIdsRequest,
      brs.api.grpc.proto.PeerApi.MilestoneBlockIds> getGetMilestoneBlockIdsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetMilestoneBlockIds",
      requestType = brs.api.grpc.proto.PeerApi.GetMilestoneBlockIdsRequest.class,
      responseType = brs.api.grpc.proto.PeerApi.MilestoneBlockIds.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.GetMilestoneBlockIdsRequest,
      brs.api.grpc.proto.PeerApi.MilestoneBlockIds> getGetMilestoneBlockIdsMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.GetMilestoneBlockIdsRequest, brs.api.grpc.proto.PeerApi.MilestoneBlockIds> getGetMilestoneBlockIdsMethod;
    if ((getGetMilestoneBlockIdsMethod = BrsPeerServiceGrpc.getGetMilestoneBlockIdsMethod) == null) {
      synchronized (BrsPeerServiceGrpc.class) {
        if ((getGetMilestoneBlockIdsMethod = BrsPeerServiceGrpc.getGetMilestoneBlockIdsMethod) == null) {
          BrsPeerServiceGrpc.getGetMilestoneBlockIdsMethod = getGetMilestoneBlockIdsMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.PeerApi.GetMilestoneBlockIdsRequest, brs.api.grpc.proto.PeerApi.MilestoneBlockIds>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetMilestoneBlockIds"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.GetMilestoneBlockIdsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.MilestoneBlockIds.getDefaultInstance()))
              .setSchemaDescriptor(new BrsPeerServiceMethodDescriptorSupplier("GetMilestoneBlockIds"))
              .build();
        }
      }
    }
    return getGetMilestoneBlockIdsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.ProcessBlockRequest,
      com.google.protobuf.Empty> getAddBlockMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AddBlock",
      requestType = brs.api.grpc.proto.PeerApi.ProcessBlockRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.ProcessBlockRequest,
      com.google.protobuf.Empty> getAddBlockMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.ProcessBlockRequest, com.google.protobuf.Empty> getAddBlockMethod;
    if ((getAddBlockMethod = BrsPeerServiceGrpc.getAddBlockMethod) == null) {
      synchronized (BrsPeerServiceGrpc.class) {
        if ((getAddBlockMethod = BrsPeerServiceGrpc.getAddBlockMethod) == null) {
          BrsPeerServiceGrpc.getAddBlockMethod = getAddBlockMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.PeerApi.ProcessBlockRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AddBlock"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.ProcessBlockRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new BrsPeerServiceMethodDescriptorSupplier("AddBlock"))
              .build();
        }
      }
    }
    return getAddBlockMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest,
      brs.api.grpc.proto.PeerApi.RawBlocks> getGetNextBlocksMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetNextBlocks",
      requestType = brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest.class,
      responseType = brs.api.grpc.proto.PeerApi.RawBlocks.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest,
      brs.api.grpc.proto.PeerApi.RawBlocks> getGetNextBlocksMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest, brs.api.grpc.proto.PeerApi.RawBlocks> getGetNextBlocksMethod;
    if ((getGetNextBlocksMethod = BrsPeerServiceGrpc.getGetNextBlocksMethod) == null) {
      synchronized (BrsPeerServiceGrpc.class) {
        if ((getGetNextBlocksMethod = BrsPeerServiceGrpc.getGetNextBlocksMethod) == null) {
          BrsPeerServiceGrpc.getGetNextBlocksMethod = getGetNextBlocksMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest, brs.api.grpc.proto.PeerApi.RawBlocks>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetNextBlocks"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.RawBlocks.getDefaultInstance()))
              .setSchemaDescriptor(new BrsPeerServiceMethodDescriptorSupplier("GetNextBlocks"))
              .build();
        }
      }
    }
    return getGetNextBlocksMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest,
      brs.api.grpc.proto.PeerApi.BlockIds> getGetNextBlockIdsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetNextBlockIds",
      requestType = brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest.class,
      responseType = brs.api.grpc.proto.PeerApi.BlockIds.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest,
      brs.api.grpc.proto.PeerApi.BlockIds> getGetNextBlockIdsMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest, brs.api.grpc.proto.PeerApi.BlockIds> getGetNextBlockIdsMethod;
    if ((getGetNextBlockIdsMethod = BrsPeerServiceGrpc.getGetNextBlockIdsMethod) == null) {
      synchronized (BrsPeerServiceGrpc.class) {
        if ((getGetNextBlockIdsMethod = BrsPeerServiceGrpc.getGetNextBlockIdsMethod) == null) {
          BrsPeerServiceGrpc.getGetNextBlockIdsMethod = getGetNextBlockIdsMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest, brs.api.grpc.proto.PeerApi.BlockIds>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetNextBlockIds"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.BlockIds.getDefaultInstance()))
              .setSchemaDescriptor(new BrsPeerServiceMethodDescriptorSupplier("GetNextBlockIds"))
              .build();
        }
      }
    }
    return getGetNextBlockIdsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.PeerApi.RawTransactions> getGetUnconfirmedTransactionsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetUnconfirmedTransactions",
      requestType = com.google.protobuf.Empty.class,
      responseType = brs.api.grpc.proto.PeerApi.RawTransactions.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      brs.api.grpc.proto.PeerApi.RawTransactions> getGetUnconfirmedTransactionsMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, brs.api.grpc.proto.PeerApi.RawTransactions> getGetUnconfirmedTransactionsMethod;
    if ((getGetUnconfirmedTransactionsMethod = BrsPeerServiceGrpc.getGetUnconfirmedTransactionsMethod) == null) {
      synchronized (BrsPeerServiceGrpc.class) {
        if ((getGetUnconfirmedTransactionsMethod = BrsPeerServiceGrpc.getGetUnconfirmedTransactionsMethod) == null) {
          BrsPeerServiceGrpc.getGetUnconfirmedTransactionsMethod = getGetUnconfirmedTransactionsMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, brs.api.grpc.proto.PeerApi.RawTransactions>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetUnconfirmedTransactions"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.RawTransactions.getDefaultInstance()))
              .setSchemaDescriptor(new BrsPeerServiceMethodDescriptorSupplier("GetUnconfirmedTransactions"))
              .build();
        }
      }
    }
    return getGetUnconfirmedTransactionsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.RawTransactions,
      com.google.protobuf.Empty> getAddUnconfirmedTransactionsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AddUnconfirmedTransactions",
      requestType = brs.api.grpc.proto.PeerApi.RawTransactions.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.RawTransactions,
      com.google.protobuf.Empty> getAddUnconfirmedTransactionsMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.RawTransactions, com.google.protobuf.Empty> getAddUnconfirmedTransactionsMethod;
    if ((getAddUnconfirmedTransactionsMethod = BrsPeerServiceGrpc.getAddUnconfirmedTransactionsMethod) == null) {
      synchronized (BrsPeerServiceGrpc.class) {
        if ((getAddUnconfirmedTransactionsMethod = BrsPeerServiceGrpc.getAddUnconfirmedTransactionsMethod) == null) {
          BrsPeerServiceGrpc.getAddUnconfirmedTransactionsMethod = getAddUnconfirmedTransactionsMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.PeerApi.RawTransactions, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AddUnconfirmedTransactions"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.RawTransactions.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new BrsPeerServiceMethodDescriptorSupplier("AddUnconfirmedTransactions"))
              .build();
        }
      }
    }
    return getAddUnconfirmedTransactionsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static BrsPeerServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BrsPeerServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BrsPeerServiceStub>() {
        @java.lang.Override
        public BrsPeerServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BrsPeerServiceStub(channel, callOptions);
        }
      };
    return BrsPeerServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static BrsPeerServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BrsPeerServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BrsPeerServiceBlockingStub>() {
        @java.lang.Override
        public BrsPeerServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BrsPeerServiceBlockingStub(channel, callOptions);
        }
      };
    return BrsPeerServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static BrsPeerServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<BrsPeerServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<BrsPeerServiceFutureStub>() {
        @java.lang.Override
        public BrsPeerServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new BrsPeerServiceFutureStub(channel, callOptions);
        }
      };
    return BrsPeerServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class BrsPeerServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void exchangeInfo(brs.api.grpc.proto.PeerApi.PeerInfo request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.PeerInfo> responseObserver) {
      asyncUnimplementedUnaryCall(getExchangeInfoMethod(), responseObserver);
    }

    /**
     */
    public void addPeers(brs.api.grpc.proto.PeerApi.Peers request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getAddPeersMethod(), responseObserver);
    }

    /**
     */
    public void getPeers(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.Peers> responseObserver) {
      asyncUnimplementedUnaryCall(getGetPeersMethod(), responseObserver);
    }

    /**
     */
    public void getCumulativeDifficulty(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.CumulativeDifficulty> responseObserver) {
      asyncUnimplementedUnaryCall(getGetCumulativeDifficultyMethod(), responseObserver);
    }

    /**
     */
    public void getMilestoneBlockIds(brs.api.grpc.proto.PeerApi.GetMilestoneBlockIdsRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.MilestoneBlockIds> responseObserver) {
      asyncUnimplementedUnaryCall(getGetMilestoneBlockIdsMethod(), responseObserver);
    }

    /**
     */
    public void addBlock(brs.api.grpc.proto.PeerApi.ProcessBlockRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getAddBlockMethod(), responseObserver);
    }

    /**
     */
    public void getNextBlocks(brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.RawBlocks> responseObserver) {
      asyncUnimplementedUnaryCall(getGetNextBlocksMethod(), responseObserver);
    }

    /**
     */
    public void getNextBlockIds(brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.BlockIds> responseObserver) {
      asyncUnimplementedUnaryCall(getGetNextBlockIdsMethod(), responseObserver);
    }

    /**
     */
    public void getUnconfirmedTransactions(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.RawTransactions> responseObserver) {
      asyncUnimplementedUnaryCall(getGetUnconfirmedTransactionsMethod(), responseObserver);
    }

    /**
     */
    public void addUnconfirmedTransactions(brs.api.grpc.proto.PeerApi.RawTransactions request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getAddUnconfirmedTransactionsMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getExchangeInfoMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.PeerApi.PeerInfo,
                brs.api.grpc.proto.PeerApi.PeerInfo>(
                  this, METHODID_EXCHANGE_INFO)))
          .addMethod(
            getAddPeersMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.PeerApi.Peers,
                com.google.protobuf.Empty>(
                  this, METHODID_ADD_PEERS)))
          .addMethod(
            getGetPeersMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                brs.api.grpc.proto.PeerApi.Peers>(
                  this, METHODID_GET_PEERS)))
          .addMethod(
            getGetCumulativeDifficultyMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                brs.api.grpc.proto.PeerApi.CumulativeDifficulty>(
                  this, METHODID_GET_CUMULATIVE_DIFFICULTY)))
          .addMethod(
            getGetMilestoneBlockIdsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.PeerApi.GetMilestoneBlockIdsRequest,
                brs.api.grpc.proto.PeerApi.MilestoneBlockIds>(
                  this, METHODID_GET_MILESTONE_BLOCK_IDS)))
          .addMethod(
            getAddBlockMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.PeerApi.ProcessBlockRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_ADD_BLOCK)))
          .addMethod(
            getGetNextBlocksMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest,
                brs.api.grpc.proto.PeerApi.RawBlocks>(
                  this, METHODID_GET_NEXT_BLOCKS)))
          .addMethod(
            getGetNextBlockIdsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest,
                brs.api.grpc.proto.PeerApi.BlockIds>(
                  this, METHODID_GET_NEXT_BLOCK_IDS)))
          .addMethod(
            getGetUnconfirmedTransactionsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                brs.api.grpc.proto.PeerApi.RawTransactions>(
                  this, METHODID_GET_UNCONFIRMED_TRANSACTIONS)))
          .addMethod(
            getAddUnconfirmedTransactionsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.PeerApi.RawTransactions,
                com.google.protobuf.Empty>(
                  this, METHODID_ADD_UNCONFIRMED_TRANSACTIONS)))
          .build();
    }
  }

  /**
   */
  public static final class BrsPeerServiceStub extends io.grpc.stub.AbstractAsyncStub<BrsPeerServiceStub> {
    private BrsPeerServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BrsPeerServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BrsPeerServiceStub(channel, callOptions);
    }

    /**
     */
    public void exchangeInfo(brs.api.grpc.proto.PeerApi.PeerInfo request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.PeerInfo> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getExchangeInfoMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void addPeers(brs.api.grpc.proto.PeerApi.Peers request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getAddPeersMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getPeers(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.Peers> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetPeersMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getCumulativeDifficulty(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.CumulativeDifficulty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetCumulativeDifficultyMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getMilestoneBlockIds(brs.api.grpc.proto.PeerApi.GetMilestoneBlockIdsRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.MilestoneBlockIds> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetMilestoneBlockIdsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void addBlock(brs.api.grpc.proto.PeerApi.ProcessBlockRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getAddBlockMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getNextBlocks(brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.RawBlocks> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetNextBlocksMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getNextBlockIds(brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.BlockIds> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetNextBlockIdsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getUnconfirmedTransactions(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.RawTransactions> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetUnconfirmedTransactionsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void addUnconfirmedTransactions(brs.api.grpc.proto.PeerApi.RawTransactions request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getAddUnconfirmedTransactionsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class BrsPeerServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<BrsPeerServiceBlockingStub> {
    private BrsPeerServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BrsPeerServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BrsPeerServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public brs.api.grpc.proto.PeerApi.PeerInfo exchangeInfo(brs.api.grpc.proto.PeerApi.PeerInfo request) {
      return blockingUnaryCall(
          getChannel(), getExchangeInfoMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty addPeers(brs.api.grpc.proto.PeerApi.Peers request) {
      return blockingUnaryCall(
          getChannel(), getAddPeersMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.PeerApi.Peers getPeers(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), getGetPeersMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.PeerApi.CumulativeDifficulty getCumulativeDifficulty(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), getGetCumulativeDifficultyMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.PeerApi.MilestoneBlockIds getMilestoneBlockIds(brs.api.grpc.proto.PeerApi.GetMilestoneBlockIdsRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetMilestoneBlockIdsMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty addBlock(brs.api.grpc.proto.PeerApi.ProcessBlockRequest request) {
      return blockingUnaryCall(
          getChannel(), getAddBlockMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.PeerApi.RawBlocks getNextBlocks(brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetNextBlocksMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.PeerApi.BlockIds getNextBlockIds(brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetNextBlockIdsMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.PeerApi.RawTransactions getUnconfirmedTransactions(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), getGetUnconfirmedTransactionsMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty addUnconfirmedTransactions(brs.api.grpc.proto.PeerApi.RawTransactions request) {
      return blockingUnaryCall(
          getChannel(), getAddUnconfirmedTransactionsMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class BrsPeerServiceFutureStub extends io.grpc.stub.AbstractFutureStub<BrsPeerServiceFutureStub> {
    private BrsPeerServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected BrsPeerServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new BrsPeerServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.PeerApi.PeerInfo> exchangeInfo(
        brs.api.grpc.proto.PeerApi.PeerInfo request) {
      return futureUnaryCall(
          getChannel().newCall(getExchangeInfoMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> addPeers(
        brs.api.grpc.proto.PeerApi.Peers request) {
      return futureUnaryCall(
          getChannel().newCall(getAddPeersMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.PeerApi.Peers> getPeers(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getGetPeersMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.PeerApi.CumulativeDifficulty> getCumulativeDifficulty(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getGetCumulativeDifficultyMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.PeerApi.MilestoneBlockIds> getMilestoneBlockIds(
        brs.api.grpc.proto.PeerApi.GetMilestoneBlockIdsRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetMilestoneBlockIdsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> addBlock(
        brs.api.grpc.proto.PeerApi.ProcessBlockRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getAddBlockMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.PeerApi.RawBlocks> getNextBlocks(
        brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetNextBlocksMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.PeerApi.BlockIds> getNextBlockIds(
        brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetNextBlockIdsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.PeerApi.RawTransactions> getUnconfirmedTransactions(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getGetUnconfirmedTransactionsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> addUnconfirmedTransactions(
        brs.api.grpc.proto.PeerApi.RawTransactions request) {
      return futureUnaryCall(
          getChannel().newCall(getAddUnconfirmedTransactionsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_EXCHANGE_INFO = 0;
  private static final int METHODID_ADD_PEERS = 1;
  private static final int METHODID_GET_PEERS = 2;
  private static final int METHODID_GET_CUMULATIVE_DIFFICULTY = 3;
  private static final int METHODID_GET_MILESTONE_BLOCK_IDS = 4;
  private static final int METHODID_ADD_BLOCK = 5;
  private static final int METHODID_GET_NEXT_BLOCKS = 6;
  private static final int METHODID_GET_NEXT_BLOCK_IDS = 7;
  private static final int METHODID_GET_UNCONFIRMED_TRANSACTIONS = 8;
  private static final int METHODID_ADD_UNCONFIRMED_TRANSACTIONS = 9;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final BrsPeerServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(BrsPeerServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_EXCHANGE_INFO:
          serviceImpl.exchangeInfo((brs.api.grpc.proto.PeerApi.PeerInfo) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.PeerInfo>) responseObserver);
          break;
        case METHODID_ADD_PEERS:
          serviceImpl.addPeers((brs.api.grpc.proto.PeerApi.Peers) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_GET_PEERS:
          serviceImpl.getPeers((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.Peers>) responseObserver);
          break;
        case METHODID_GET_CUMULATIVE_DIFFICULTY:
          serviceImpl.getCumulativeDifficulty((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.CumulativeDifficulty>) responseObserver);
          break;
        case METHODID_GET_MILESTONE_BLOCK_IDS:
          serviceImpl.getMilestoneBlockIds((brs.api.grpc.proto.PeerApi.GetMilestoneBlockIdsRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.MilestoneBlockIds>) responseObserver);
          break;
        case METHODID_ADD_BLOCK:
          serviceImpl.addBlock((brs.api.grpc.proto.PeerApi.ProcessBlockRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_GET_NEXT_BLOCKS:
          serviceImpl.getNextBlocks((brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.RawBlocks>) responseObserver);
          break;
        case METHODID_GET_NEXT_BLOCK_IDS:
          serviceImpl.getNextBlockIds((brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.BlockIds>) responseObserver);
          break;
        case METHODID_GET_UNCONFIRMED_TRANSACTIONS:
          serviceImpl.getUnconfirmedTransactions((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.RawTransactions>) responseObserver);
          break;
        case METHODID_ADD_UNCONFIRMED_TRANSACTIONS:
          serviceImpl.addUnconfirmedTransactions((brs.api.grpc.proto.PeerApi.RawTransactions) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
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

  private static abstract class BrsPeerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    BrsPeerServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return brs.api.grpc.proto.PeerApi.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("BrsPeerService");
    }
  }

  private static final class BrsPeerServiceFileDescriptorSupplier
      extends BrsPeerServiceBaseDescriptorSupplier {
    BrsPeerServiceFileDescriptorSupplier() {}
  }

  private static final class BrsPeerServiceMethodDescriptorSupplier
      extends BrsPeerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    BrsPeerServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (BrsPeerServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new BrsPeerServiceFileDescriptorSupplier())
              .addMethod(getExchangeInfoMethod())
              .addMethod(getAddPeersMethod())
              .addMethod(getGetPeersMethod())
              .addMethod(getGetCumulativeDifficultyMethod())
              .addMethod(getGetMilestoneBlockIdsMethod())
              .addMethod(getAddBlockMethod())
              .addMethod(getGetNextBlocksMethod())
              .addMethod(getGetNextBlockIdsMethod())
              .addMethod(getGetUnconfirmedTransactionsMethod())
              .addMethod(getAddUnconfirmedTransactionsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
