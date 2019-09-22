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
import brs.unconfirmedtransactions.UnconfirmedTransactionStoreImpl
import brs.util.DownloadCacheImpl
import brs.util.LoggerConfigurator
import brs.util.ThreadPool
import brs.util.Time
import io.grpc.ServerBuilder
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.IOException
import java.net.InetSocketAddress
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
            Appendix.init(dp)
            AtApiPlatformImpl.init(dp)
            AtController.init(dp)
            AtApiImpl.init(dp)
            OCLPoC.init(dp)
            dp.timeService = TimeServiceImpl()
            dp.derivedTableManager = DerivedTableManager()
            dp.statisticsManager = StatisticsManagerImpl(dp)
            dp.dbCacheManager = DBCacheManagerImpl(dp)
            dp.threadPool = ThreadPool(dp)
            LoggerConfigurator.init()
            Db.init(dp)
            dp.dbs = Db.dbsByDatabaseType
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
            dp.blockService = BlockServiceImpl(dp)
            dp.blockchainProcessor = BlockchainProcessorImpl(dp)
            AtConstants.init(dp)
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
            dp.generator.generateForBlockchainProcessor(dp)
            dp.deeplinkQRCodeGenerator = DeeplinkQRCodeGenerator()
            dp.parameterService = ParameterServiceImpl(dp)
            dp.blockchainProcessor.addListener(AT.HandleATBlockTransactionsListener(dp), BlockchainProcessor.Event.AFTER_BLOCK_APPLY)
            dp.blockchainProcessor.addListener(DGSGoodsStoreServiceImpl.ExpiredPurchaseListener(dp), BlockchainProcessor.Event.AFTER_BLOCK_APPLY)
            dp.apiTransactionManager = APITransactionManagerImpl(dp)
            Peers.init(dp)
            TransactionType.init(dp)
            dp.api = API(dp)

            if (dp.propertyService.get(Props.API_V2_SERVER)) {
                val port = if (dp.propertyService.get(Props.DEV_TESTNET)) dp.propertyService.get(Props.DEV_API_V2_PORT) else dp.propertyService.get(Props.API_V2_PORT)
                logger.info("Starting V2 API Server on port {}", port)
                val apiV2 = BrsService(dp)
                val hostname = dp.propertyService.get(Props.API_V2_LISTEN)
                dp.apiV2Server = if (hostname == "0.0.0.0") ServerBuilder.forPort(port).addService(apiV2).build().start() else NettyServerBuilder.forAddress(InetSocketAddress(hostname, port)).addService(apiV2).build().start()
            } else {
                logger.info("Not starting V2 API Server - it is disabled.")
            }

            if (dp.propertyService.get(Props.BRS_DEBUG_TRACE_ENABLED))
                DebugTrace.init(dp)

            val timeMultiplier = if (dp.propertyService.get(Props.DEV_TESTNET) && dp.propertyService.get(Props.DEV_OFFLINE)) dp.propertyService.get(Props.DEV_TIMEWARP).coerceAtLeast(1) else 1

            dp.threadPool.start(timeMultiplier)
            if (timeMultiplier > 1) {
                dp.timeService.setTime(Time.FasterTime(max(dp.timeService.epochTime, dp.blockchain.lastBlock.timestamp), timeMultiplier))
                logger.info("TIME WILL FLOW {} TIMES FASTER!", timeMultiplier)
            }

            val currentTime = System.currentTimeMillis()
            logger.info("Initialization took {} ms", currentTime - startTime)
            logger.info("BRS {} started successfully.", VERSION)

            if (dp.propertyService.get(Props.DEV_TESTNET)) {
                logger.info("RUNNING ON TESTNET - DO NOT USE REAL ACCOUNTS!")
            }
        } catch (e: Exception) {
            logger.error(e.message, e)
            exitProcess(1)
        }
    }

    private fun validateVersionNotDev(propertyService: PropertyService) {
        if (VERSION.isPrelease && !propertyService.get(Props.DEV_TESTNET)) {
            logger.error("THIS IS A DEVELOPMENT WALLET, PLEASE DO NOT USE THIS")
            exitProcess(0)
        }
    }

    private fun shutdown() {
        shutdown(false)
    }

    fun shutdown(ignoreDBShutdown: Boolean) {
        logger.info("Shutting down...")
        try {
            dp.api.shutdown()
        } catch (ignored: UninitializedPropertyAccessException) {}
        try {
            dp.apiV2Server.shutdownNow()
        } catch (ignored: UninitializedPropertyAccessException) {}
        try {
            Peers.shutdown(dp.threadPool)
        } catch (ignored: UninitializedPropertyAccessException) {}
        try {
            dp.threadPool.shutdown()
        } catch (ignored: UninitializedPropertyAccessException) {}
        if (!ignoreDBShutdown) {
            Db.shutdown()
        }
        try {
            dp.dbCacheManager.close()
        } catch (ignored: UninitializedPropertyAccessException) {}
        try {
            if (dp.blockchainProcessor.oclVerify) {
                OCLPoC.destroy()
            }
        } catch (ignored: UninitializedPropertyAccessException) {}
        logger.info("BRS {} stopped.", VERSION)
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
            logger.info("Initializing Burst Reference Software (BRS) version {}", VERSION)
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
