package brs.api.grpc.peer

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider
import com.google.protobuf.Empty

internal class GetCumulativeDifficultyHandler(private val dp: DependencyProvider) : GrpcApiHandler<Empty, PeerApi.CumulativeDifficulty> {
    override fun handleRequest(request: Empty): PeerApi.CumulativeDifficulty {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
