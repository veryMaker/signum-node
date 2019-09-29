package brs.grpc.handlers

import brs.Burst
import brs.DependencyProvider
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.props.Props
import com.google.protobuf.Empty

class GetStateHandler(private val dp: DependencyProvider) : GrpcApiHandler<Empty, BrsApi.State> {

    override fun handleRequest(empty: Empty): BrsApi.State {
        val lastBlock = dp.blockchain.lastBlock
        val lastBlockchainFeeder = dp.blockchainProcessor.lastBlockchainFeeder
        return BrsApi.State.newBuilder()
                .setApplication(Burst.APPLICATION)
                .setVersion(Burst.VERSION.toString())
                .setTime(BrsApi.Time.newBuilder().setTime(dp.timeService.epochTime).build())
                .setLastBlock(lastBlock.id)
                .setLastHeight(dp.blockchain.height)
                .setCumulativeDifficulty(lastBlock.cumulativeDifficulty.toString())
                .setNumberOfPeers(dp.peers.allPeers.size)
                .setNumberOfActivePeers(dp.peers.activePeers.size)
                .setNumberOfForgers(dp.generator.allGenerators.size)
                .setLastBlockchainFeeder(lastBlockchainFeeder?.announcedAddress ?: "null")
                .setLastBlockchainFeederHeight(dp.blockchainProcessor.lastBlockchainFeederHeight ?: 0)
                .setAvailableProcessors(Runtime.getRuntime().availableProcessors())
                .setMaxMemory(Runtime.getRuntime().maxMemory())
                .setTotalMemory(Runtime.getRuntime().totalMemory())
                .setFreeMemory(Runtime.getRuntime().freeMemory())
                .setIndirectIncomingServiceEnabled(dp.propertyService.get(Props.INDIRECT_INCOMING_SERVICE_ENABLE))
                .build()
    }
}
