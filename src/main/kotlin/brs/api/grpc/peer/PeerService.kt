package brs.api.grpc.peer

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.ProtoBuilder
import brs.api.grpc.proto.BrsPeerServiceGrpc
import com.google.protobuf.Message
import io.grpc.stub.StreamObserver
import kotlin.reflect.KClass

class PeerService : BrsPeerServiceGrpc.BrsPeerServiceImplBase() {
    private val handlers: Map<KClass<out GrpcApiHandler<out Message, out Message>>, GrpcApiHandler<out Message, out Message>>

    init {
        val handlerMap = mutableMapOf<KClass<out GrpcApiHandler<out Message, out Message>>, GrpcApiHandler<out Message, out Message>>()

        this.handlers = handlerMap
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
}