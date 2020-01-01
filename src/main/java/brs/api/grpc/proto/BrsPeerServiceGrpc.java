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
    value = "by gRPC proto compiler (version 1.26.0)",
    comments = "Source: peerApi.proto")
public final class BrsPeerServiceGrpc {

  private BrsPeerServiceGrpc() {}

  public static final String SERVICE_NAME = "brs.peer.BrsPeerService";

  // Static method descriptors that strictly reflect the proto.
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

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.PeerInfo,
      brs.api.grpc.proto.PeerApi.PeerInfo> getGetInfoMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetInfo",
      requestType = brs.api.grpc.proto.PeerApi.PeerInfo.class,
      responseType = brs.api.grpc.proto.PeerApi.PeerInfo.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.PeerInfo,
      brs.api.grpc.proto.PeerApi.PeerInfo> getGetInfoMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.PeerInfo, brs.api.grpc.proto.PeerApi.PeerInfo> getGetInfoMethod;
    if ((getGetInfoMethod = BrsPeerServiceGrpc.getGetInfoMethod) == null) {
      synchronized (BrsPeerServiceGrpc.class) {
        if ((getGetInfoMethod = BrsPeerServiceGrpc.getGetInfoMethod) == null) {
          BrsPeerServiceGrpc.getGetInfoMethod = getGetInfoMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.PeerApi.PeerInfo, brs.api.grpc.proto.PeerApi.PeerInfo>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetInfo"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.PeerInfo.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.PeerInfo.getDefaultInstance()))
              .setSchemaDescriptor(new BrsPeerServiceMethodDescriptorSupplier("GetInfo"))
              .build();
        }
      }
    }
    return getGetInfoMethod;
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

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest,
      brs.api.grpc.proto.PeerApi.RawBlocks> getGetBlocksAfterMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetBlocksAfter",
      requestType = brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest.class,
      responseType = brs.api.grpc.proto.PeerApi.RawBlocks.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest,
      brs.api.grpc.proto.PeerApi.RawBlocks> getGetBlocksAfterMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest, brs.api.grpc.proto.PeerApi.RawBlocks> getGetBlocksAfterMethod;
    if ((getGetBlocksAfterMethod = BrsPeerServiceGrpc.getGetBlocksAfterMethod) == null) {
      synchronized (BrsPeerServiceGrpc.class) {
        if ((getGetBlocksAfterMethod = BrsPeerServiceGrpc.getGetBlocksAfterMethod) == null) {
          BrsPeerServiceGrpc.getGetBlocksAfterMethod = getGetBlocksAfterMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest, brs.api.grpc.proto.PeerApi.RawBlocks>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetBlocksAfter"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.RawBlocks.getDefaultInstance()))
              .setSchemaDescriptor(new BrsPeerServiceMethodDescriptorSupplier("GetBlocksAfter"))
              .build();
        }
      }
    }
    return getGetBlocksAfterMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest,
      brs.api.grpc.proto.PeerApi.BlockIds> getGetBlockIdsAfterMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetBlockIdsAfter",
      requestType = brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest.class,
      responseType = brs.api.grpc.proto.PeerApi.BlockIds.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest,
      brs.api.grpc.proto.PeerApi.BlockIds> getGetBlockIdsAfterMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest, brs.api.grpc.proto.PeerApi.BlockIds> getGetBlockIdsAfterMethod;
    if ((getGetBlockIdsAfterMethod = BrsPeerServiceGrpc.getGetBlockIdsAfterMethod) == null) {
      synchronized (BrsPeerServiceGrpc.class) {
        if ((getGetBlockIdsAfterMethod = BrsPeerServiceGrpc.getGetBlockIdsAfterMethod) == null) {
          BrsPeerServiceGrpc.getGetBlockIdsAfterMethod = getGetBlockIdsAfterMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest, brs.api.grpc.proto.PeerApi.BlockIds>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetBlockIdsAfter"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.BlockIds.getDefaultInstance()))
              .setSchemaDescriptor(new BrsPeerServiceMethodDescriptorSupplier("GetBlockIdsAfter"))
              .build();
        }
      }
    }
    return getGetBlockIdsAfterMethod;
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

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.RawBlock,
      brs.api.grpc.proto.PeerApi.ProcessResult> getProcessBlockMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ProcessBlock",
      requestType = brs.api.grpc.proto.PeerApi.RawBlock.class,
      responseType = brs.api.grpc.proto.PeerApi.ProcessResult.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.RawBlock,
      brs.api.grpc.proto.PeerApi.ProcessResult> getProcessBlockMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.RawBlock, brs.api.grpc.proto.PeerApi.ProcessResult> getProcessBlockMethod;
    if ((getProcessBlockMethod = BrsPeerServiceGrpc.getProcessBlockMethod) == null) {
      synchronized (BrsPeerServiceGrpc.class) {
        if ((getProcessBlockMethod = BrsPeerServiceGrpc.getProcessBlockMethod) == null) {
          BrsPeerServiceGrpc.getProcessBlockMethod = getProcessBlockMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.PeerApi.RawBlock, brs.api.grpc.proto.PeerApi.ProcessResult>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ProcessBlock"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.RawBlock.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.ProcessResult.getDefaultInstance()))
              .setSchemaDescriptor(new BrsPeerServiceMethodDescriptorSupplier("ProcessBlock"))
              .build();
        }
      }
    }
    return getProcessBlockMethod;
  }

  private static volatile io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.RawTransactions,
      brs.api.grpc.proto.PeerApi.ProcessResult> getProcessTransactionsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ProcessTransactions",
      requestType = brs.api.grpc.proto.PeerApi.RawTransactions.class,
      responseType = brs.api.grpc.proto.PeerApi.ProcessResult.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.RawTransactions,
      brs.api.grpc.proto.PeerApi.ProcessResult> getProcessTransactionsMethod() {
    io.grpc.MethodDescriptor<brs.api.grpc.proto.PeerApi.RawTransactions, brs.api.grpc.proto.PeerApi.ProcessResult> getProcessTransactionsMethod;
    if ((getProcessTransactionsMethod = BrsPeerServiceGrpc.getProcessTransactionsMethod) == null) {
      synchronized (BrsPeerServiceGrpc.class) {
        if ((getProcessTransactionsMethod = BrsPeerServiceGrpc.getProcessTransactionsMethod) == null) {
          BrsPeerServiceGrpc.getProcessTransactionsMethod = getProcessTransactionsMethod =
              io.grpc.MethodDescriptor.<brs.api.grpc.proto.PeerApi.RawTransactions, brs.api.grpc.proto.PeerApi.ProcessResult>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ProcessTransactions"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.RawTransactions.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  brs.api.grpc.proto.PeerApi.ProcessResult.getDefaultInstance()))
              .setSchemaDescriptor(new BrsPeerServiceMethodDescriptorSupplier("ProcessTransactions"))
              .build();
        }
      }
    }
    return getProcessTransactionsMethod;
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
    public void getInfo(brs.api.grpc.proto.PeerApi.PeerInfo request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.PeerInfo> responseObserver) {
      asyncUnimplementedUnaryCall(getGetInfoMethod(), responseObserver);
    }

    /**
     */
    public void getMilestoneBlockIds(brs.api.grpc.proto.PeerApi.GetMilestoneBlockIdsRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.MilestoneBlockIds> responseObserver) {
      asyncUnimplementedUnaryCall(getGetMilestoneBlockIdsMethod(), responseObserver);
    }

    /**
     */
    public void getBlocksAfter(brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.RawBlocks> responseObserver) {
      asyncUnimplementedUnaryCall(getGetBlocksAfterMethod(), responseObserver);
    }

    /**
     */
    public void getBlockIdsAfter(brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.BlockIds> responseObserver) {
      asyncUnimplementedUnaryCall(getGetBlockIdsAfterMethod(), responseObserver);
    }

    /**
     */
    public void getUnconfirmedTransactions(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.RawTransactions> responseObserver) {
      asyncUnimplementedUnaryCall(getGetUnconfirmedTransactionsMethod(), responseObserver);
    }

    /**
     */
    public void processBlock(brs.api.grpc.proto.PeerApi.RawBlock request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.ProcessResult> responseObserver) {
      asyncUnimplementedUnaryCall(getProcessBlockMethod(), responseObserver);
    }

    /**
     */
    public void processTransactions(brs.api.grpc.proto.PeerApi.RawTransactions request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.ProcessResult> responseObserver) {
      asyncUnimplementedUnaryCall(getProcessTransactionsMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
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
            getGetInfoMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.PeerApi.PeerInfo,
                brs.api.grpc.proto.PeerApi.PeerInfo>(
                  this, METHODID_GET_INFO)))
          .addMethod(
            getGetMilestoneBlockIdsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.PeerApi.GetMilestoneBlockIdsRequest,
                brs.api.grpc.proto.PeerApi.MilestoneBlockIds>(
                  this, METHODID_GET_MILESTONE_BLOCK_IDS)))
          .addMethod(
            getGetBlocksAfterMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest,
                brs.api.grpc.proto.PeerApi.RawBlocks>(
                  this, METHODID_GET_BLOCKS_AFTER)))
          .addMethod(
            getGetBlockIdsAfterMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest,
                brs.api.grpc.proto.PeerApi.BlockIds>(
                  this, METHODID_GET_BLOCK_IDS_AFTER)))
          .addMethod(
            getGetUnconfirmedTransactionsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                brs.api.grpc.proto.PeerApi.RawTransactions>(
                  this, METHODID_GET_UNCONFIRMED_TRANSACTIONS)))
          .addMethod(
            getProcessBlockMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.PeerApi.RawBlock,
                brs.api.grpc.proto.PeerApi.ProcessResult>(
                  this, METHODID_PROCESS_BLOCK)))
          .addMethod(
            getProcessTransactionsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                brs.api.grpc.proto.PeerApi.RawTransactions,
                brs.api.grpc.proto.PeerApi.ProcessResult>(
                  this, METHODID_PROCESS_TRANSACTIONS)))
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
    public void getInfo(brs.api.grpc.proto.PeerApi.PeerInfo request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.PeerInfo> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetInfoMethod(), getCallOptions()), request, responseObserver);
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
    public void getBlocksAfter(brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.RawBlocks> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetBlocksAfterMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getBlockIdsAfter(brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.BlockIds> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetBlockIdsAfterMethod(), getCallOptions()), request, responseObserver);
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
    public void processBlock(brs.api.grpc.proto.PeerApi.RawBlock request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.ProcessResult> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getProcessBlockMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void processTransactions(brs.api.grpc.proto.PeerApi.RawTransactions request,
        io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.ProcessResult> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getProcessTransactionsMethod(), getCallOptions()), request, responseObserver);
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
    public brs.api.grpc.proto.PeerApi.PeerInfo getInfo(brs.api.grpc.proto.PeerApi.PeerInfo request) {
      return blockingUnaryCall(
          getChannel(), getGetInfoMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.PeerApi.MilestoneBlockIds getMilestoneBlockIds(brs.api.grpc.proto.PeerApi.GetMilestoneBlockIdsRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetMilestoneBlockIdsMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.PeerApi.RawBlocks getBlocksAfter(brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetBlocksAfterMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.PeerApi.BlockIds getBlockIdsAfter(brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetBlockIdsAfterMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.PeerApi.RawTransactions getUnconfirmedTransactions(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), getGetUnconfirmedTransactionsMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.PeerApi.ProcessResult processBlock(brs.api.grpc.proto.PeerApi.RawBlock request) {
      return blockingUnaryCall(
          getChannel(), getProcessBlockMethod(), getCallOptions(), request);
    }

    /**
     */
    public brs.api.grpc.proto.PeerApi.ProcessResult processTransactions(brs.api.grpc.proto.PeerApi.RawTransactions request) {
      return blockingUnaryCall(
          getChannel(), getProcessTransactionsMethod(), getCallOptions(), request);
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
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.PeerApi.PeerInfo> getInfo(
        brs.api.grpc.proto.PeerApi.PeerInfo request) {
      return futureUnaryCall(
          getChannel().newCall(getGetInfoMethod(), getCallOptions()), request);
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
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.PeerApi.RawBlocks> getBlocksAfter(
        brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetBlocksAfterMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.PeerApi.BlockIds> getBlockIdsAfter(
        brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetBlockIdsAfterMethod(), getCallOptions()), request);
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
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.PeerApi.ProcessResult> processBlock(
        brs.api.grpc.proto.PeerApi.RawBlock request) {
      return futureUnaryCall(
          getChannel().newCall(getProcessBlockMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<brs.api.grpc.proto.PeerApi.ProcessResult> processTransactions(
        brs.api.grpc.proto.PeerApi.RawTransactions request) {
      return futureUnaryCall(
          getChannel().newCall(getProcessTransactionsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_ADD_PEERS = 0;
  private static final int METHODID_GET_PEERS = 1;
  private static final int METHODID_GET_CUMULATIVE_DIFFICULTY = 2;
  private static final int METHODID_GET_INFO = 3;
  private static final int METHODID_GET_MILESTONE_BLOCK_IDS = 4;
  private static final int METHODID_GET_BLOCKS_AFTER = 5;
  private static final int METHODID_GET_BLOCK_IDS_AFTER = 6;
  private static final int METHODID_GET_UNCONFIRMED_TRANSACTIONS = 7;
  private static final int METHODID_PROCESS_BLOCK = 8;
  private static final int METHODID_PROCESS_TRANSACTIONS = 9;

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
        case METHODID_GET_INFO:
          serviceImpl.getInfo((brs.api.grpc.proto.PeerApi.PeerInfo) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.PeerInfo>) responseObserver);
          break;
        case METHODID_GET_MILESTONE_BLOCK_IDS:
          serviceImpl.getMilestoneBlockIds((brs.api.grpc.proto.PeerApi.GetMilestoneBlockIdsRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.MilestoneBlockIds>) responseObserver);
          break;
        case METHODID_GET_BLOCKS_AFTER:
          serviceImpl.getBlocksAfter((brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.RawBlocks>) responseObserver);
          break;
        case METHODID_GET_BLOCK_IDS_AFTER:
          serviceImpl.getBlockIdsAfter((brs.api.grpc.proto.PeerApi.GetBlocksAfterRequest) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.BlockIds>) responseObserver);
          break;
        case METHODID_GET_UNCONFIRMED_TRANSACTIONS:
          serviceImpl.getUnconfirmedTransactions((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.RawTransactions>) responseObserver);
          break;
        case METHODID_PROCESS_BLOCK:
          serviceImpl.processBlock((brs.api.grpc.proto.PeerApi.RawBlock) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.ProcessResult>) responseObserver);
          break;
        case METHODID_PROCESS_TRANSACTIONS:
          serviceImpl.processTransactions((brs.api.grpc.proto.PeerApi.RawTransactions) request,
              (io.grpc.stub.StreamObserver<brs.api.grpc.proto.PeerApi.ProcessResult>) responseObserver);
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
              .addMethod(getAddPeersMethod())
              .addMethod(getGetPeersMethod())
              .addMethod(getGetCumulativeDifficultyMethod())
              .addMethod(getGetInfoMethod())
              .addMethod(getGetMilestoneBlockIdsMethod())
              .addMethod(getGetBlocksAfterMethod())
              .addMethod(getGetBlockIdsAfterMethod())
              .addMethod(getGetUnconfirmedTransactionsMethod())
              .addMethod(getProcessBlockMethod())
              .addMethod(getProcessTransactionsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
