package brs.entity

import brs.api.http.API
import brs.api.http.APITransactionManager
import brs.at.AtApi
import brs.at.AtApiController
import brs.at.AtConstants
import brs.at.AtController
import brs.db.*
import brs.services.*
import brs.transaction.type.TransactionType
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
    lateinit var unconfirmedTransactionService: UnconfirmedTransactionService
    lateinit var indirectIncomingStore: IndirectIncomingStore
    lateinit var blockDb: BlockDb
    lateinit var transactionDb: TransactionDb
    lateinit var peerDb: PeerDb
    lateinit var blockchainService: BlockchainService
    lateinit var blockchainProcessorService: BlockchainProcessorService
    lateinit var transactionProcessorService: TransactionProcessorService
    lateinit var propertyService: PropertyService
    lateinit var fluxCapacitorService: FluxCapacitorService
    lateinit var dbCacheService: DBCacheService
    lateinit var api: API
    lateinit var apiV2Server: Server
    lateinit var timeService: TimeService
    lateinit var derivedTableService: DerivedTableService
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
    lateinit var downloadCacheService: DownloadCacheService
    lateinit var indirectIncomingService: IndirectIncomingService
    lateinit var blockService: BlockService
    lateinit var feeSuggestionService: FeeSuggestionService
    @Deprecated("Use deeplinkGeneratorService")
    lateinit var deeplinkQRCodeGeneratorService: DeeplinkQRCodeGeneratorService
    lateinit var deeplinkGeneratorService: DeeplinkGeneratorService
    lateinit var parameterService: ParameterService
    lateinit var apiTransactionManager: APITransactionManager
    lateinit var peerService: PeerService
    lateinit var oclPocService: OclPocService
    lateinit var atConstants: AtConstants
    lateinit var atApi: AtApi
    lateinit var atApiController: AtApiController
    lateinit var atController: AtController
    lateinit var transactionTypes: Map<Byte, Map<Byte, TransactionType>>
    lateinit var db: Db
    lateinit var qrGeneratorService: QRGeneratorService
}
