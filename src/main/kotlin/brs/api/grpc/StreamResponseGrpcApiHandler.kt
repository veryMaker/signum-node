package brs.api.grpc

import brs.api.grpc.proto.ProtoBuilder
import com.google.protobuf.Message
import io.grpc.stub.StreamObserver

interface StreamResponseGrpcApiHandler<R : Message, S : Message> : GrpcApiHandler<R, S> {
    override fun handleRequest(request: R): S {
        throw UnsupportedOperationException("Cannot return single value from stream response")
    }

    fun handleStreamRequest(request: R, responseObserver: StreamObserver<S>)

    override fun handleRequest(request: R, responseObserver: StreamObserver<S>) {
        try {
            handleStreamRequest(request, responseObserver)
        } catch (e: Exception) {
            responseObserver.onError(ProtoBuilder.buildError(e))
        }
    }
}
