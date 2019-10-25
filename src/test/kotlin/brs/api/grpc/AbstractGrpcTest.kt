package brs.api.grpc

import brs.services.AssetExchangeService
import brs.entity.Block
import brs.services.BlockchainService
import brs.services.BlockchainProcessorService
import brs.services.GeneratorService
import brs.common.QuickMocker
import brs.services.impl.FeeSuggestionServiceImpl
import brs.services.impl.FluxCapacitorServiceImpl
import brs.api.grpc.proto.BrsApiServiceGrpc
import brs.api.grpc.proto.BrsService
import brs.entity.DependencyProvider
import brs.objects.Props
import brs.services.*
import brs.services.TransactionProcessorService
import brs.transaction.type.TransactionType
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.grpc.Context
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.testing.GrpcCleanupRule
import org.junit.Rule

abstract class AbstractGrpcTest {

    @get:Rule
    val grpcCleanup = GrpcCleanupRule()

    protected lateinit var dp: DependencyProvider
    protected lateinit var brsService: BrsApiServiceGrpc.BrsApiServiceBlockingStub

    protected fun defaultBrsService() {
        // Mocks
        val latestBlock = mock<Block>()
        val blockchainProcessor = mock<BlockchainProcessorService>()
        val blockchain = mock<BlockchainService>()
        val blockService = mock<BlockService>()
        val accountService = mock<AccountService>()
        val generator = mock<GeneratorService>()
        val transactionProcessor = mock<TransactionProcessorService>()
        val timeService = mock<TimeService>()
        val feeSuggestionCalculator = mock<FeeSuggestionServiceImpl>()
        val atService = mock<ATService>()
        val aliasService = mock<AliasService>()
        val indirectIncomingService = mock<IndirectIncomingService>()
        val escrowService = mock<EscrowService>()
        val assetExchange = mock<AssetExchangeService>()
        val subscriptionService = mock<SubscriptionService>()
        val dgsGoodsStoreService = mock<DigitalGoodsStoreService>()
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
        dp = QuickMocker.dependencyProvider(blockchainProcessor, blockchain, blockService, accountService, generator, transactionProcessor, timeService, feeSuggestionCalculator, atService, aliasService, indirectIncomingService, escrowService, assetExchange, subscriptionService, dgsGoodsStoreService, propertyService)
        dp.fluxCapacitorService = FluxCapacitorServiceImpl(dp)
        dp.transactionTypes = TransactionType.getTransactionTypes(dp)

        setUpBrsService(BrsService(dp))
    }

    private fun setUpBrsService(brsService: BrsService) {
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(brsService).build().start())

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
