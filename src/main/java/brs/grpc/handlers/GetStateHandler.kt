package brs.grpc.handlers

import brs.*
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.peer.Peer
import brs.peer.Peers
import brs.props.PropertyService
import brs.props.Props
import brs.services.TimeService
import com.google.protobuf.Empty

class GetStateHandler(private val timeService: TimeService, private val blockchain: Blockchain, private val generator: Generator, private val blockchainProcessor: BlockchainProcessor, private val propertyService: PropertyService) : GrpcApiHandler<Empty, BrsApi.State> {

    @Throws(Exception::class)
    override fun handleRequest(empty: Empty): BrsApi.State {
        val lastBlock = blockchain.lastBlock
        val lastBlockchainFeeder = blockchainProcessor.lastBlockchainFeeder
        return BrsApi.State.newBuilder()
                .setApplication(Burst.APPLICATION)
                .setVersion(Burst.VERSION.toString())
                .setTime(BrsApi.Time.newBuilder().setTime(timeService.epochTime).build())
                .setLastBlock(lastBlock.id)
                .setLastHeight(blockchain.height)
                .setCumulativeDifficulty(lastBlock.cumulativeDifficulty.toString())
                .setNumberOfPeers(Peers.allPeers.size)
                .setNumberOfActivePeers(Peers.activePeers.size)
                .setNumberOfForgers(generator.allGenerators.size)
                .setLastBlockchainFeeder(lastBlockchainFeeder?.announcedAddress ?: "null")
                .setLastBlockchainFeederHeight(blockchainProcessor.lastBlockchainFeederHeight)
                .setIsScanning(blockchainProcessor.isScanning)
                .setAvailableProcessors(Runtime.getRuntime().availableProcessors())
                .setMaxMemory(Runtime.getRuntime().maxMemory())
                .setTotalMemory(Runtime.getRuntime().totalMemory())
                .setFreeMemory(Runtime.getRuntime().freeMemory())
                .setIndirectIncomingServiceEnabled(propertyService.get(Props.INDIRECT_INCOMING_SERVICE_ENABLE))
                .build()
    }
}
