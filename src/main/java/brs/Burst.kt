package brs

import brs.assetexchange.AssetExchangeImpl
import brs.at.*
import brs.db.cache.DBCacheManagerImpl
import brs.db.sql.*
import brs.db.store.DerivedTableManager
import brs.deeplink.DeeplinkQRCodeGenerator
import brs.feesuggestions.FeeSuggestionCalculator
import brs.fluxcapacitor.FluxCapacitorImpl
import brs.grpc.proto.BrsService
import brs.http.API
import brs.http.APITransactionManagerImpl
import brs.peer.Peers
import brs.props.PropertyService
import brs.props.PropertyServiceImpl
import brs.props.Props
import brs.services.impl.*
import brs.statistics.StatisticsManagerImpl
import brs.taskScheduler.impl.RxJavaTaskScheduler
import brs.transaction.TransactionType
import brs.unconfirmedtransactions.UnconfirmedTransactionStoreImpl
import brs.util.DownloadCacheImpl
import brs.util.LoggerConfigurator
import brs.util.Time
import brs.util.logging.safeError
import brs.util.logging.safeInfo
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.IOException
import java.util.*
import kotlin.math.max
import kotlin.system.exitProcess

class Burst(properties: Properties, addShutdownHook: Boolean = true) {
    val dp = DependencyProvider()

    init {
        dp.propertyService = PropertyServiceImpl(properties)
        validateVersionNotDev(dp.propertyService)

        if (addShutdownHook) Runtime.getRuntime().addShutdownHook(Thread(Runnable { shutdown() }))

        try {
            val startTime = System.currentTimeMillis()
            Constants.init(dp)
            dp.taskScheduler = RxJavaTaskScheduler()
            dp.atApiPlatformImpl = AtApiPlatformImpl(dp)
            dp.atApiController = AtApiController(dp)
            dp.atController = AtController(dp)
            val atApiImpl = AtApiImpl(dp) // TODO ??
            if (dp.propertyService.get(Props.GPU_ACCELERATION)) {
                dp.oclPoC = OCLPoC(dp)
            }
            dp.timeService = TimeServiceImpl()
            dp.derivedTableManager = DerivedTableManager()
            dp.statisticsManager = StatisticsManagerImpl(dp)
            dp.dbCacheManager = DBCacheManagerImpl(dp)
            LoggerConfigurator.init()
            dp.db = Db(dp)
            val dbs = dp.db.dbsByDatabaseType
            dp.blockDb = dbs.blockDb
            dp.transactionDb = dbs.transactionDb
            dp.peerDb = dbs.peerDb
            dp.accountStore = SqlAccountStore(dp)
            dp.aliasStore = SqlAliasStore(dp)
            dp.assetStore = SqlAssetStore(dp)
            dp.assetTransferStore = SqlAssetTransferStore(dp)
            dp.atStore = SqlATStore(dp)
            dp.digitalGoodsStoreStore = SqlDigitalGoodsStoreStore(dp)
            dp.escrowStore = SqlEscrowStore(dp)
            dp.orderStore = SqlOrderStore(dp)
            dp.tradeStore = SqlTradeStore(dp)
            dp.subscriptionStore = SqlSubscriptionStore(dp)
            dp.unconfirmedTransactionStore = UnconfirmedTransactionStoreImpl(dp)
            dp.indirectIncomingStore = SqlIndirectIncomingStore(dp)
            dp.blockchainStore = SqlBlockchainStore(dp)
            dp.blockchain = BlockchainImpl(dp)
            dp.aliasService = AliasServiceImpl(dp)
            dp.fluxCapacitor = FluxCapacitorImpl(dp)
            dp.transactionTypes = TransactionType.getTransactionTypes(dp)
            dp.blockService = BlockServiceImpl(dp)
            dp.blockchainProcessor = BlockchainProcessorImpl(dp)
            dp.atConstants = AtConstants(dp)
            dp.economicClustering = EconomicClustering(dp)
            dp.generator = if (dp.propertyService.get(Props.DEV_MOCK_MINING)) GeneratorImpl.MockGenerator(dp) else GeneratorImpl(dp)
            dp.accountService = AccountServiceImpl(dp)
            dp.transactionService = TransactionServiceImpl(dp)
            dp.transactionProcessor = TransactionProcessorImpl(dp)
            dp.atService = ATServiceImpl(dp)
            dp.subscriptionService = SubscriptionServiceImpl(dp)
            dp.digitalGoodsStoreService = DGSGoodsStoreServiceImpl(dp)
            dp.escrowService = EscrowServiceImpl(dp)
            dp.assetExchange = AssetExchangeImpl(dp)
            dp.downloadCache = DownloadCacheImpl(dp)
            dp.indirectIncomingService = IndirectIncomingServiceImpl(dp)
            dp.feeSuggestionCalculator = FeeSuggestionCalculator(dp)
            dp.deeplinkQRCodeGenerator = DeeplinkQRCodeGenerator()
            dp.parameterService = ParameterServiceImpl(dp)
            dp.blockchainProcessor.addListener(BlockchainProcessor.Event.AFTER_BLOCK_APPLY, AT.handleATBlockTransactionsListener(dp))
            dp.blockchainProcessor.addListener(BlockchainProcessor.Event.AFTER_BLOCK_APPLY, DGSGoodsStoreServiceImpl.expiredPurchaseListener(dp))
            dp.apiTransactionManager = APITransactionManagerImpl(dp)
            dp.peers = Peers(dp)
            dp.api = API(dp)

            if (dp.propertyService.get(Props.API_V2_SERVER)) {
                val hostname = dp.propertyService.get(Props.API_V2_LISTEN)
                val port = if (dp.propertyService.get(Props.DEV_TESTNET)) dp.propertyService.get(Props.DEV_API_V2_PORT) else dp.propertyService.get(Props.API_V2_PORT)
                logger.safeInfo { "Starting V2 API Server on port $port" }
                dp.apiV2Server = BrsService(dp).start(hostname, port)
            } else {
                logger.safeInfo { "Not starting V2 API Server - it is disabled." }
            }

            if (dp.propertyService.get(Props.BRS_DEBUG_TRACE_ENABLED)) {
                val debugTraceManager = DebugTraceManager(dp)
            }

            val timeMultiplier =
                if (dp.propertyService.get(Props.DEV_TESTNET) && dp.propertyService.get(Props.DEV_OFFLINE)) dp.propertyService.get(
                    Props.DEV_TIMEWARP
                ).coerceAtLeast(1) else 1

            dp.taskScheduler.start()
            if (timeMultiplier > 1) {
                dp.timeService.setTime(
                    Time.FasterTime(
                        max(
                            dp.timeService.epochTime,
                            dp.blockchain.lastBlock.timestamp
                        ), timeMultiplier
                    )
                )
                logger.safeInfo { "TIME WILL FLOW $timeMultiplier TIMES FASTER!" }
            }

            val currentTime = System.currentTimeMillis()
            logger.safeInfo { "Initialization took ${currentTime - startTime} ms" }
            logger.safeInfo { "$APPLICATION $VERSION started successfully!" }

            if (dp.propertyService.get(Props.DEV_TESTNET)) {
                logger.safeInfo { "RUNNING ON TESTNET - DO NOT USE REAL ACCOUNTS!" }
            }
        } catch (e: Exception) {
            logger.safeError(e) { e.message }
            exitProcess(1)
        }
    }

    private fun validateVersionNotDev(propertyService: PropertyService) {
        if (VERSION.isPrelease && !propertyService.get(Props.DEV_TESTNET)) {
            logger.safeError { "THIS IS A DEVELOPMENT WALLET, PLEASE DO NOT USE THIS" }
            //exitProcess(0)
        }
    }

    private fun shutdown() {
        shutdown(false)
    }

    fun shutdown(ignoreDBShutdown: Boolean) {
        logger.safeInfo { "Shutting down..." }
        try {
            dp.api.shutdown()
        } catch (ignored: UninitializedPropertyAccessException) {}
        try {
            dp.apiV2Server.shutdownNow()
        } catch (ignored: UninitializedPropertyAccessException) {}
        try {
            dp.peers.shutdown()
        } catch (ignored: UninitializedPropertyAccessException) {}
        try {
            dp.taskScheduler.shutdown()
        } catch (ignored: UninitializedPropertyAccessException) {}
        if (!ignoreDBShutdown) {
            dp.db.shutdown()
        }
        try {
            dp.dbCacheManager.close()
        } catch (ignored: UninitializedPropertyAccessException) {}
        try {
            if (dp.blockchainProcessor.oclVerify && dp.oclPoC != null) {
                dp.oclPoC!!.destroy()
            }
        } catch (ignored: UninitializedPropertyAccessException) {}
        logger.safeInfo { "$APPLICATION $VERSION stopped." }
        LoggerConfigurator.shutdown()
    }

    companion object {
        val VERSION = Version.parse("v3.0.0-dev")
        const val APPLICATION = "BRS"
        private const val DEFAULT_PROPERTIES_NAME = "brs-default.properties"
        private val logger = LoggerFactory.getLogger(Burst::class.java)

        private fun loadProperties(): Properties {
            val defaultProperties = Properties()

            // TODO this can be refactored to be cleaner.
            logger.safeInfo { "Initializing Burst Reference Software ($APPLICATION) version $VERSION" }
            try {
                ClassLoader.getSystemResourceAsStream(DEFAULT_PROPERTIES_NAME).use { input ->
                    if (input != null) {
                        defaultProperties.load(input)
                    } else {
                        val configFile = System.getProperty(DEFAULT_PROPERTIES_NAME)

                        if (configFile != null) {
                            try {
                                FileInputStream(configFile).use { fis -> defaultProperties.load(fis) }
                            } catch (e: IOException) {
                                throw RuntimeException("Error loading $DEFAULT_PROPERTIES_NAME from $configFile")
                            }

                        } else {
                            throw RuntimeException("$DEFAULT_PROPERTIES_NAME not in classpath and system property $DEFAULT_PROPERTIES_NAME not defined either")
                        }
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException("Error loading $DEFAULT_PROPERTIES_NAME", e)
            }

            val properties = Properties(defaultProperties)
            try {
                ClassLoader.getSystemResourceAsStream("brs.properties").use { input ->
                    if (input != null) { // parse if brs.properties was loaded
                        properties.load(input)
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException("Error loading brs.properties", e)
            }

            return properties
        }

        fun init(addShutdownHook: Boolean = true): Burst {
            return Burst(loadProperties(), addShutdownHook)
        }
    }
}
