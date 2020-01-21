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
        handlerMap[ExchangeInfoHandler::class] = ExchangeInfoHandler(dp)
        handlerMap[GetMilestoneBlockIdsHandler::class] = GetMilestoneBlockIdsHandler(dp)
        handlerMap[GetNextBlocksHandler::class] = GetNextBlocksHandler(dp)
        handlerMap[GetNextBlockIdsHandler::class] = GetNextBlockIdsHandler(dp)
        handlerMap[GetUnconfirmedTransactionsHandler::class] = GetUnconfirmedTransactionsHandler(dp)
        handlerMap[AddBlockHandler::class] = AddBlockHandler(dp)
        handlerMap[AddUnconfirmedTransactionsHandler::class] = AddUnconfirmedTransactionsHandler(dp)

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

    override fun exchangeInfo(request: PeerApi.PeerInfo, responseObserver: StreamObserver<PeerApi.PeerInfo>) {
        return handleRequest(ExchangeInfoHandler::class, request, responseObserver)
    }

    override fun getMilestoneBlockIds(request: PeerApi.GetMilestoneBlockIdsRequest, responseObserver: StreamObserver<PeerApi.MilestoneBlockIds>) {
        return handleRequest(GetMilestoneBlockIdsHandler::class, request, responseObserver)
    }

    override fun getNextBlocks(request: PeerApi.GetBlocksAfterRequest, responseObserver: StreamObserver<PeerApi.RawBlocks>) {
        return handleRequest(GetNextBlocksHandler::class, request, responseObserver)
    }

    override fun getNextBlockIds(request: PeerApi.GetBlocksAfterRequest, responseObserver: StreamObserver<PeerApi.BlockIds>) {
        return handleRequest(GetNextBlockIdsHandler::class, request, responseObserver)
    }

    override fun getUnconfirmedTransactions(request: Empty, responseObserver: StreamObserver<PeerApi.RawTransactions>) {
        return handleRequest(GetUnconfirmedTransactionsHandler::class, request, responseObserver)
    }

    override fun addBlock(request: PeerApi.ProcessBlockRequest, responseObserver: StreamObserver<Empty>) {
        return handleRequest(AddBlockHandler::class, request, responseObserver)
    }

    override fun addUnconfirmedTransactions(request: PeerApi.RawTransactions, responseObserver: StreamObserver<Empty>) {
        return handleRequest(AddUnconfirmedTransactionsHandler::class, request, responseObserver)
    }
}
