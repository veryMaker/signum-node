package brs.api.grpc

import brs.api.grpc.proto.BrsApiServiceGrpc
import brs.api.grpc.http.ApiService
import brs.common.QuickMocker
import brs.entity.Block
import brs.entity.DependencyProvider
import brs.objects.Props
import brs.services.*
import brs.services.impl.FeeSuggestionServiceImpl
import brs.services.impl.FluxCapacitorServiceImpl
import brs.transaction.type.TransactionType
import io.grpc.Context
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.testing.GrpcCleanupRule
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule

abstract class AbstractGrpcTest {
    @get:Rule
    val grpcCleanup = GrpcCleanupRule()

    protected lateinit var dp: DependencyProvider
    protected lateinit var brsService: BrsApiServiceGrpc.BrsApiServiceBlockingStub

    protected fun defaultBrsService() {
        // Mocks
        val latestBlock = mockk<Block>()
        val blockchainProcessor = mockk<BlockchainProcessorService>(relaxUnitFun = true)
        val blockchain = mockk<BlockchainService>()
        val blockService = mockk<BlockService>()
        val accountService = mockk<AccountService>()
        val generator = mockk<GeneratorService>()
        val transactionProcessor = mockk<TransactionProcessorService>(relaxUnitFun = true)
        val timeService = mockk<TimeService>()
        val feeSuggestionCalculator = mockk<FeeSuggestionServiceImpl>()
        val atService = mockk<ATService>()
        val aliasService = mockk<AliasService>()
        val indirectIncomingService = mockk<IndirectIncomingService>()
        val escrowService = mockk<EscrowService>()
        val assetExchange = mockk<AssetExchangeService>()
        val subscriptionService = mockk<SubscriptionService>()
        val dgsGoodsStoreService = mockk<DigitalGoodsStoreService>()
        val propertyService = QuickMocker.defaultPropertyService()

        // Returns
        every { blockchain.getBlock(any()) } returns null
        every { blockchain.height } returns Integer.MAX_VALUE
        every { blockchain.lastBlock } returns latestBlock
        every { propertyService.get(Props.DEV_TESTNET) } returns true
        every { generator.calculateGenerationSignature(any(), any()) } returns ByteArray(32)
        every { latestBlock.height } returns 0
        every { latestBlock.baseTarget } returns 0
        every { latestBlock.generatorId } returns 0L
        every { latestBlock.generationSignature } returns ByteArray(32)
        every { propertyService.get(Props.SOLO_MINING_PASSPHRASES) } returns emptyList<String>()

        // Real classes
        dp = QuickMocker.dependencyProvider(blockchainProcessor, blockchain, blockService, accountService, generator, transactionProcessor, timeService, feeSuggestionCalculator, atService, aliasService, indirectIncomingService, escrowService, assetExchange, subscriptionService, dgsGoodsStoreService, propertyService)
        dp.fluxCapacitorService = FluxCapacitorServiceImpl(dp)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        setUpBrsService(ApiService(dp))
    }

    private fun setUpBrsService(apiService: ApiService) {
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(apiService).build().start())

        this.brsService = BrsApiServiceGrpc.newBlockingStub(grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()))
    }

    /**
     * Needed so that streaming calls can be gracefully shutdown afterwards.
     * @param runnable The test to execute
     */
    protected inline fun runAndCancel(runnable: () -> Unit) {
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
