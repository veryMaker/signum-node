package brs.api.grpc.peer

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider
import com.google.protobuf.ByteString
import com.google.protobuf.Empty

internal class GetCumulativeDifficultyHandler(private val dp: DependencyProvider) : GrpcApiHandler<Empty, PeerApi.CumulativeDifficulty> {
    override fun handleRequest(request: Empty): PeerApi.CumulativeDifficulty {
        val lastBlock = dp.blockchainService.lastBlock
        return PeerApi.CumulativeDifficulty.newBuilder()
            .setCumulativeDifficulty(ByteString.copyFrom(lastBlock.cumulativeDifficulty.toByteArray()))
            .setBlockchainHeight(lastBlock.height)
            .build()
    }
}
