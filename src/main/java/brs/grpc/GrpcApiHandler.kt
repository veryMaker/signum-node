package brs.grpc

import brs.grpc.proto.ProtoBuilder
import com.google.protobuf.Message
import io.grpc.stub.StreamObserver

interface GrpcApiHandler<R : Message, S : Message> {
    /**
     * This should only ever be internally called.
     */
    suspend fun handleRequest(request: R): S

    suspend fun handleRequest(request: R, responseObserver: StreamObserver<S>) {
        try {
            responseObserver.onNext(handleRequest(request))
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(ProtoBuilder.buildError(e))
        }
    }
}
