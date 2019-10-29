package brs.api.grpc.handlers

import brs.Burst
import brs.entity.DependencyProvider
import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.objects.Props
import com.google.protobuf.Empty

class GetStateHandler(private val dp: DependencyProvider) : GrpcApiHandler<Empty, BrsApi.State> {

    override fun handleRequest(empty: Empty): BrsApi.State {
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
            .setNumberOfForgers(dp.generatorService.allGenerators.size)
            .setLastBlockchainFeeder(lastBlockchainFeeder?.announcedAddress ?: "null")
            .setLastBlockchainFeederHeight(dp.blockchainProcessorService.lastBlockchainFeederHeight ?: 0)
            .setAvailableProcessors(Runtime.getRuntime().availableProcessors())
            .setMaxMemory(Runtime.getRuntime().maxMemory())
            .setTotalMemory(Runtime.getRuntime().totalMemory())
            .setFreeMemory(Runtime.getRuntime().freeMemory())
            .setIndirectIncomingServiceEnabled(dp.propertyService.get(Props.INDIRECT_INCOMING_SERVICE_ENABLE))
            .build()
    }
}
