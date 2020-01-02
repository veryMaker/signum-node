package brs.api.grpc.peer

import brs.api.grpc.ApiException
import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider
import brs.util.convert.toUnsignedString
import kotlin.math.max
import kotlin.math.min

internal class GetMilestoneBlockIdsHandler(private val dp: DependencyProvider) : GrpcApiHandler<PeerApi.GetMilestoneBlockIdsRequest, PeerApi.MilestoneBlockIds> {
    override fun handleRequest(request: PeerApi.GetMilestoneBlockIdsRequest): PeerApi.MilestoneBlockIds {
        val lastBlockId = request.lastBlockId
        if (lastBlockId != 0L) {
            val myLastBlockId = dp.blockchainService.lastBlock.id
            if (myLastBlockId == lastBlockId || dp.blockchainService.hasBlock(lastBlockId)) {
                return PeerApi.MilestoneBlockIds.newBuilder()
                    .addMilestoneBlockIds(lastBlockId)
                    .setLast(myLastBlockId == lastBlockId)
                    .build()
            }
        }

        var blockId: Long
        var height: Int
        val jump: Int
        var limit = 10
        val blockchainHeight = dp.blockchainService.height
        val lastMilestoneBlockId = request.lastMilestoneBlockId
        when {
            lastMilestoneBlockId != 0L -> {
                val lastMilestoneBlock = dp.blockchainService.getBlock(lastMilestoneBlockId) ?: error("Don't have block ${lastMilestoneBlockId.toUnsignedString()}")
                height = lastMilestoneBlock.height
                jump = min(1440, max(blockchainHeight - height, 1))
                height = max(height - jump, 0)
            }
            lastBlockId != 0L -> {
                height = blockchainHeight
                jump = 10
            }
            else -> throw ApiException("At least one of lastBlockId and lastMilestoneBlockId must be set")
        }
        blockId = dp.blockchainService.getBlockIdAtHeight(height)

        val milestoneBlockIds = mutableListOf<Long>()
        while (height > 0 && limit-- > 0) {
            milestoneBlockIds.add(blockId)
            blockId = dp.blockchainService.getBlockIdAtHeight(height)
            height -= jump
        }

        return PeerApi.MilestoneBlockIds.newBuilder()
            .addAllMilestoneBlockIds(milestoneBlockIds)
            .setLast(false)
            .build()
    }
}
