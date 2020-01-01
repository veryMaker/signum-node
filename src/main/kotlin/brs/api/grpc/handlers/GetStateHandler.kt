package brs.api.grpc.handlers

import brs.Burst
import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.entity.DependencyProvider
import com.google.protobuf.Empty

class GetStateHandler(private val dp: DependencyProvider) : GrpcApiHandler<Empty, BrsApi.State> {
    override fun handleRequest(request: Empty): BrsApi.State {
        val lastBlock = dp.blockchainService.lastBlock
        val lastBlockchainFeeder = dp.blockchainProcessorService.lastBlockchainFeeder
        return BrsApi.State.newBuilder()
            .setApplication(Burst.APPLICATION)
            .setVersion(Burst.VERSION.toString())
            .setTime(BrsApi.Time.newBuilder().setTime(dp.timeService.epochTime).build())
            .setLastBlock(lastBlock.id)
            .setLastHeight(dp.blockchainService.height)
            .setCumulativeDifficulty(lastBlock.cumulativeDifficulty.toString())
            .setNumberOfPeers(dp.peerService.allPeers.size)
            .setNumberOfActivePeers(dp.peerService.activePeers.size)
            .setNumberOfForgers(dp.generatorService.numberOfGenerators)
            .setLastBlockchainFeeder(lastBlockchainFeeder?.announcedAddress ?: "null")
            .setLastBlockchainFeederHeight(dp.blockchainProcessorService.lastBlockchainFeederHeight ?: 0)
            .setAvailableProcessors(Runtime.getRuntime().availableProcessors())
            .setMaxMemory(Runtime.getRuntime().maxMemory())
            .setTotalMemory(Runtime.getRuntime().totalMemory())
            .setFreeMemory(Runtime.getRuntime().freeMemory())
            .build()
    }
}
