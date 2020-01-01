package brs.api.grpc.peer

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider

internal class GetMilestoneBlockIdsHandler(private val dp: DependencyProvider) : GrpcApiHandler<PeerApi.GetMilestoneBlockIdsRequest, PeerApi.MilestoneBlockIds> {
    override fun handleRequest(request: PeerApi.GetMilestoneBlockIdsRequest): PeerApi.MilestoneBlockIds {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
