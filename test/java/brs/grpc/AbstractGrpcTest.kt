package brs.grpc

import brs.*
import brs.assetexchange.AssetExchange
import brs.common.QuickMocker
import brs.feesuggestions.FeeSuggestionCalculator
import brs.fluxcapacitor.FluxCapacitor
import brs.fluxcapacitor.FluxCapacitorImpl
import brs.grpc.proto.BrsApiServiceGrpc
import brs.grpc.proto.BrsService
import brs.props.Prop
import brs.props.PropertyService
import brs.props.Props
import brs.services.*
import com.nhaarman.mockitokotlin2.*
import io.grpc.Context
import io.grpc.ManagedChannel
import io.grpc.Server
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.testing.GrpcCleanupRule
import org.junit.Rule
import org.mockito.Answers
import org.mockito.ArgumentMatchers

import java.io.IOException


abstract class AbstractGrpcTest {

    @get:Rule
    val grpcCleanup = GrpcCleanupRule()

    protected var brsService: BrsApiServiceGrpc.BrsApiServiceBlockingStub? = null

    @Throws(IOException::class)
    protected fun defaultBrsService() {
        // Mocks
        val latestBlock = mock<Block>()
        val blockchainProcessor = mock<BlockchainProcessor>()
        val blockchain = mock<Blockchain>()
        val blockService = mock<BlockService>()
        val accountService = mock<AccountService>()
        val generator = mock<Generator>()
        val transactionProcessor = mock<TransactionProcessor>()
        val timeService = mock<TimeService>()
        val feeSuggestionCalculator = mock<FeeSuggestionCalculator>()
        val atService = mock<ATService>()
        val aliasService = mock<AliasService>()
        val indirectIncomingService = mock<IndirectIncomingService>()
        val escrowService = mock<EscrowService>()
        val assetExchange = mock<AssetExchange>()
        val subscriptionService = mock<SubscriptionService>()
        val dgsGoodsStoreService = mock<DGSGoodsStoreService>()
        val propertyService = QuickMocker.defaultPropertyService()

        // Returns
        doReturn(Integer.MAX_VALUE).whenever(blockchain).height
        doReturn(latestBlock).whenever(blockchain).lastBlock
        doReturn(true).whenever(propertyService).get(Props.DEV_TESTNET)
        doReturn(ByteArray(32)).whenever(generator).calculateGenerationSignature(any(), any())
        doReturn(0L).whenever(latestBlock).generatorId
        doReturn(ByteArray(32)).whenever(latestBlock).generationSignature
        doReturn(emptyList<String>()).whenever(propertyService).get(Props.SOLO_MINING_PASSPHRASES)

        // Real classes
        val fluxCapacitor = FluxCapacitorImpl(blockchain, propertyService)
        TransactionType.init(blockchain, fluxCapacitor, accountService, dgsGoodsStoreService, aliasService, assetExchange, subscriptionService, escrowService)

        setUpBrsService(BrsService(blockchainProcessor, blockchain, blockService, accountService, generator, transactionProcessor, timeService, feeSuggestionCalculator, atService, aliasService, indirectIncomingService, fluxCapacitor, escrowService, assetExchange, subscriptionService, dgsGoodsStoreService, propertyService))
    }

    @Throws(IOException::class)
    protected fun setUpBrsService(brsService: BrsService) {
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(brsService).build().start())

        this.brsService = BrsApiServiceGrpc.newBlockingStub(grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()))
    }

    /**
     * Needed so that streaming calls can be gracefully shutdown afterwards.
     * @param runnable The test to execute
     */
    protected fun runAndCancel(runnable: () -> Unit) {
        val withCancellation = Context.current().withCancellation()
        val prevCtx = withCancellation.attach()
        try {
            runnable()
        } finally {
            withCancellation.detach(prevCtx)
            withCancellation.close()
        }
    }
}
