package brs.api.grpc.peer

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.ProtoBuilder
import brs.api.grpc.proto.BrsPeerServiceGrpc
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider
import com.google.protobuf.Empty
import com.google.protobuf.Message
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.ServerInterceptors
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.stub.StreamObserver
import java.net.InetSocketAddress
import kotlin.reflect.KClass

class PeerApiService(dp: DependencyProvider) : BrsPeerServiceGrpc.BrsPeerServiceImplBase() {
    private val handlers: Map<KClass<out GrpcApiHandler<out Message, out Message>>, GrpcApiHandler<out Message, out Message>>

    init {
        val handlerMap = mutableMapOf<KClass<out GrpcApiHandler<out Message, out Message>>, GrpcApiHandler<out Message, out Message>>()

        handlerMap[AddPeersHandler::class] = AddPeersHandler(dp)
        handlerMap[GetPeersHandler::class] = GetPeersHandler(dp)
        handlerMap[GetCumulativeDifficultyHandler::class] = GetCumulativeDifficultyHandler(dp)
        handlerMap[GetInfoHandler::class] = GetInfoHandler(dp)
        handlerMap[GetMilestoneBlockIdsHandler::class] = GetMilestoneBlockIdsHandler(dp)
        handlerMap[GetBlocksAfterHandler::class] = GetBlocksAfterHandler(dp)
        handlerMap[GetBlockIdsAfterHandler::class] = GetBlockIdsAfterHandler(dp)
        handlerMap[GetUnconfirmedTransactionsHandler::class] = GetUnconfirmedTransactionsHandler(dp)
        handlerMap[ProcessBlockHandler::class] = ProcessBlockHandler(dp)
        handlerMap[ProcessTransactionsHandler::class] = ProcessTransactionsHandler(dp)

        this.handlers = handlerMap
    }

    fun start(hostname: String, port: Int): Server {
        val service = ServerInterceptors.intercept(this, PeerApiContextKeys.RemoteAddressInterceptor)
        return if (hostname == "0.0.0.0")
            ServerBuilder.forPort(port).addService(service).build().start()
        else
            NettyServerBuilder.forAddress(InetSocketAddress(hostname, port)).addService(service).build().start()
    }

    private inline fun <reified H : GrpcApiHandler<R, S>, R : Message, S : Message> handleRequest(
        handlerClass: KClass<H>,
        request: R,
        response: StreamObserver<S>
    ) {
        val handler = handlers[handlerClass]
        if (handler is H) {
            handler.handleRequest(request, response)
        } else {
            response.onError(ProtoBuilder.buildError(GrpcApiHandler.HandlerNotFoundException("H not registered: ${H::class}")))
        }
    }

    override fun addPeers(request: PeerApi.Peers, responseObserver: StreamObserver<Empty>) {
        return handleRequest(AddPeersHandler::class, request, responseObserver)
    }

    override fun getPeers(request: Empty, responseObserver: StreamObserver<PeerApi.Peers>) {
        return handleRequest(GetPeersHandler::class, request, responseObserver)
    }

    override fun getCumulativeDifficulty(request: Empty, responseObserver: StreamObserver<PeerApi.CumulativeDifficulty>) {
        return handleRequest(GetCumulativeDifficultyHandler::class, request, responseObserver)
    }

    override fun getInfo(request: PeerApi.PeerInfo, responseObserver: StreamObserver<PeerApi.PeerInfo>) {
        return handleRequest(GetInfoHandler::class, request, responseObserver)
    }

    override fun getMilestoneBlockIds(request: PeerApi.GetMilestoneBlockIdsRequest, responseObserver: StreamObserver<PeerApi.MilestoneBlockIds>) {
        return handleRequest(GetMilestoneBlockIdsHandler::class, request, responseObserver)
    }

    override fun getBlocksAfter(request: PeerApi.GetBlocksAfterRequest, responseObserver: StreamObserver<PeerApi.RawBlocks>) {
        return handleRequest(GetBlocksAfterHandler::class, request, responseObserver)
    }

    override fun getBlockIdsAfter(request: PeerApi.GetBlocksAfterRequest, responseObserver: StreamObserver<PeerApi.BlockIds>) {
        return handleRequest(GetBlockIdsAfterHandler::class, request, responseObserver)
    }

    override fun getUnconfirmedTransactions(request: Empty, responseObserver: StreamObserver<PeerApi.RawTransactions>) {
        return handleRequest(GetUnconfirmedTransactionsHandler::class, request, responseObserver)
    }

    override fun processBlock(request: PeerApi.ProcessBlockRequest, responseObserver: StreamObserver<Empty>) {
        return handleRequest(ProcessBlockHandler::class, request, responseObserver)
    }

    override fun processTransactions(request: PeerApi.RawTransactions, responseObserver: StreamObserver<Empty>) {
        return handleRequest(ProcessTransactionsHandler::class, request, responseObserver)
    }
}
