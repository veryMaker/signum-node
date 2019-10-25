package brs

import brs.api.http.API
import brs.api.http.APITransactionManager
import brs.at.AtApi
import brs.at.AtApiController
import brs.at.AtConstants
import brs.at.AtController
import brs.services.BlockchainService
import brs.services.BlockchainProcessorService
import brs.db.BlockDb
import brs.db.PeerDb
import brs.db.TransactionDb
import brs.db.cache.DBCacheManagerImpl
import brs.db.sql.Db
import brs.db.store.*
import brs.services.FluxCapacitorService
import brs.services.*
import brs.services.TaskSchedulerService
import brs.services.TransactionProcessorService
import brs.transaction.type.TransactionType
import brs.transaction.unconfirmed.UnconfirmedTransactionStore
import brs.util.DownloadCacheImpl
import io.grpc.Server

class DependencyProvider {
    lateinit var accountStore: AccountStore
    lateinit var aliasStore: AliasStore
    lateinit var assetTransferStore: AssetTransferStore
    lateinit var assetStore: AssetStore
    lateinit var atStore: ATStore
    lateinit var blockchainStore: BlockchainStore
    lateinit var digitalGoodsStoreStore: DigitalGoodsStoreStore
    lateinit var escrowStore: EscrowStore
    lateinit var orderStore: OrderStore
    lateinit var tradeStore: TradeStore
    lateinit var subscriptionStore: SubscriptionStore
    lateinit var unconfirmedTransactionStore: UnconfirmedTransactionStore
    lateinit var indirectIncomingStore: IndirectIncomingStore
    lateinit var blockDb: BlockDb
    lateinit var transactionDb: TransactionDb
    lateinit var peerDb: PeerDb
    lateinit var blockchainService: BlockchainService
    lateinit var blockchainProcessorService: BlockchainProcessorService
    lateinit var transactionProcessorService: TransactionProcessorService
    lateinit var propertyService: PropertyService
    lateinit var fluxCapacitorService: FluxCapacitorService
    lateinit var dbCacheManager: DBCacheManagerImpl
    lateinit var api: API
    lateinit var apiV2Server: Server
    lateinit var timeService: TimeService
    lateinit var derivedTableManager: DerivedTableManager
    lateinit var statisticsService: StatisticsService
    lateinit var taskSchedulerService: TaskSchedulerService
    lateinit var aliasService: AliasService
    lateinit var economicClusteringService: EconomicClusteringService
    lateinit var generatorService: GeneratorService
    lateinit var accountService: AccountService
    lateinit var transactionService: TransactionService
    lateinit var atService: ATService
    lateinit var subscriptionService: SubscriptionService
    lateinit var digitalGoodsStoreService: DigitalGoodsStoreService
    lateinit var escrowService: EscrowService
    lateinit var assetExchangeService: AssetExchangeService
    lateinit var downloadCache: DownloadCacheImpl
    lateinit var indirectIncomingService: IndirectIncomingService
    lateinit var blockService: BlockService
    lateinit var feeSuggestionService: FeeSuggestionService
    lateinit var deeplinkQRCodeGeneratorService: DeeplinkQRCodeGeneratorService
    lateinit var parameterService: ParameterService
    lateinit var apiTransactionManager: APITransactionManager
    lateinit var peerService: PeerService
    var oclPoC: OCLPoC? = null
    lateinit var atConstants: AtConstants
    lateinit var atApi: AtApi
    lateinit var atApiController: AtApiController
    lateinit var atController: AtController
    lateinit var transactionTypes: Map<Byte, Map<Byte, TransactionType>>
    lateinit var db: Db
}
