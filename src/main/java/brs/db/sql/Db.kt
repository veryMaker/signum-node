package brs.db.sql

import brs.DependencyProvider
import brs.db.BurstKey
import brs.db.store.Dbs
import brs.props.Props
import brs.util.logging.safeDebug
import brs.util.logging.safeInfo
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.conf.StatementType
import org.jooq.impl.DSL
import org.jooq.tools.jdbc.JDBCUtils
import org.mariadb.jdbc.MariaDbDataSource
import org.mariadb.jdbc.UrlParser
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException
import java.util.*

class Db(private val dp: DependencyProvider) { // TODO interface
    private val logger = LoggerFactory.getLogger(Db::class.java)

    private val settings = Settings()
    private val staticStatementSettings = Settings()
    private val cp: HikariDataSource
    private val dialect: SQLDialect
    private val localConnection = ThreadLocal<Connection>()
    private val transactionCaches = ThreadLocal<MutableMap<String, MutableMap<BurstKey, Any>>>()
    private val transactionBatches = ThreadLocal<MutableMap<String, MutableMap<BurstKey, Any>>>()

    init {
        settings.isRenderSchema = false
        staticStatementSettings.isRenderSchema = false
        staticStatementSettings.statementType = StatementType.STATIC_STATEMENT
    }

    val dbsByDatabaseType: Dbs
        get() {
            logger.safeInfo { "Using SQL Backend with Dialect ${dialect.getName()}" }
            return SqlDbs(dp)
        }

    fun getDslContext(): DSLContext {
        val con = localConnection.get()
        if (con == null) {
            DSL.using(cp, dialect, settings).use { ctx -> return ctx }
        } else {
            DSL.using(con, dialect, staticStatementSettings).use { ctx -> return ctx }
        }
    }

    fun isInTransaction() = localConnection.get() != null

    init {
        val dbUrl: String
        val dbUsername: String?
        val dbPassword: String?

        if (dp.propertyService.get(Props.DEV_TESTNET)) {
            dbUrl = dp.propertyService.get(Props.DEV_DB_URL)
            dbUsername = dp.propertyService.get(Props.DEV_DB_USERNAME)
            dbPassword = dp.propertyService.get(Props.DEV_DB_PASSWORD)
        } else {
            dbUrl = dp.propertyService.get(Props.DB_URL)
            dbUsername = dp.propertyService.get(Props.DB_USERNAME)
            dbPassword = dp.propertyService.get(Props.DB_PASSWORD)
        }
        dialect = JDBCUtils.dialect(dbUrl)

        logger.safeDebug { "Database jdbc url set to: $dbUrl" }
        try {
            val config = HikariConfig()
            config.jdbcUrl = dbUrl
            if (dbUsername.isNotEmpty()) config.username = dbUsername
            if (dbPassword.isNotEmpty()) config.password = dbPassword
            config.maximumPoolSize = dp.propertyService.get(Props.DB_CONNECTIONS)

            val flywayBuilder = Flyway.configure()
                    .dataSource(dbUrl, dbUsername, dbPassword)
                    .baselineOnMigrate(true)
            var runFlyway = false

            when (dialect) {
                SQLDialect.MYSQL, SQLDialect.MARIADB -> {
                    flywayBuilder.locations("classpath:/db/migration_mariadb")
                    runFlyway = true
                    config.isAutoCommit = true
                    config.addDataSourceProperty("cachePrepStmts", "true")
                    config.addDataSourceProperty("prepStmtCacheSize", "512")
                    config.addDataSourceProperty("prepStmtCacheSqlLimit", "4096")
                    config.addDataSourceProperty("characterEncoding", "utf8mb4")
                    config.addDataSourceProperty("cacheServerConfiguration", "true")
                    config.addDataSourceProperty("useLocalSessionState", "true")
                    config.addDataSourceProperty("useLocalTransactionState", "true")
                    config.addDataSourceProperty("useUnicode", "true")
                    config.addDataSourceProperty("useServerPrepStmts", "true")
                    config.addDataSourceProperty("rewriteBatchedStatements", "true")
                    config.addDataSourceProperty("maintainTimeStats", "false")
                    config.addDataSourceProperty("useUnbufferedIO", "false")
                    config.addDataSourceProperty("useReadAheadInput", "false")
                    val flywayDataSource = object : MariaDbDataSource(dbUrl) {
                        override fun initialize() {
                            super.initialize()
                            val props = Properties()
                            props.setProperty("user", dbUsername)
                            props.setProperty("password", dbPassword)
                            props.setProperty("useMysqlMetadata", "true")
                            try {
                                val f = MariaDbDataSource::class.java.getDeclaredField("urlParser")
                                f.isAccessible = true
                                f.set(this, UrlParser.parse(dbUrl, props))
                            } catch (e: Exception) {
                                throw RuntimeException(e)
                            }
                        }
                    }
                    flywayBuilder.dataSource(flywayDataSource) // TODO Remove this hack once a stable version of Flyway has this bug fixed
                    config.connectionInitSql = "SET NAMES utf8mb4;"
                }
                SQLDialect.H2 -> {
                    Class.forName("org.h2.Driver")
                    flywayBuilder.locations("classpath:/db/migration_h2")
                    runFlyway = true
                    config.isAutoCommit = true
                    config.addDataSourceProperty("cachePrepStmts", "true")
                    config.addDataSourceProperty("prepStmtCacheSize", "250")
                    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
                    config.addDataSourceProperty("DATABASE_TO_UPPER", "false")
                    config.addDataSourceProperty("CASE_INSENSITIVE_IDENTIFIERS", "true")
                }
                else -> {
                }
            }

            cp = HikariDataSource(config)

            if (runFlyway) {
                logger.safeInfo { "Running flyway migration" }
                val flyway = flywayBuilder.load()
                flyway.migrate()
            }
        } catch (e: Exception) {
            throw RuntimeException(e.toString(), e)
        }

    }

    fun shutdown() {
        if (dialect == SQLDialect.H2) {
            try {
                cp.connection.use { con ->
                    con.createStatement().use { stmt ->
                        // COMPACT is not giving good result.
                        if (dp.propertyService.get(Props.DB_H2_DEFRAG_ON_SHUTDOWN)) {
                            stmt.execute("SHUTDOWN DEFRAG")
                        } else {
                            stmt.execute("SHUTDOWN")
                        }
                    }
                }
            } catch (e: SQLException) {
                logger.safeInfo(e) { null }
            } finally {
                logger.safeInfo { "Database shutdown completed." }
            }
        }
        if (!cp.isClosed) {
            cp.close()
        }
    }

    inline fun <T> getUsingDslContext(function: (DSLContext) -> T): T {
        getDslContext().use { context -> return function(context) }
    }

    inline fun useDslContext(consumer: (DSLContext) -> Unit) { // TODO parallelize this? maybe we should have a dedicated coroutine that does db operations in sequence?
        getDslContext().use { context -> consumer(context) }
    }

    internal fun <V> getCache(tableName: String): MutableMap<BurstKey, V> {
        check(isInTransaction()) { "Not in transaction" }
        return transactionCaches.get()!!.computeIfAbsent(tableName) { mutableMapOf() } as MutableMap<BurstKey, V>
    }

    internal fun <V> getBatch(tableName: String): MutableMap<BurstKey, V> {
        check(isInTransaction()) { "Not in transaction" }
        return transactionBatches.get()!!.computeIfAbsent(tableName) { mutableMapOf() } as MutableMap<BurstKey, V>
    }

    fun beginTransaction(): Connection {
        check(!isInTransaction()) { "Transaction already in progress" }
        try {
            val con = cp.connection
            con.autoCommit = false

            localConnection.set(con)
            transactionCaches.set(mutableMapOf())
            transactionBatches.set(mutableMapOf())

            return con
        } catch (e: Exception) {
            throw RuntimeException(e.toString(), e)
        }
    }

    fun commitTransaction() {
        val con = localConnection.get() ?: error("Not in transaction")
        try {
            con.commit()
        } catch (e: SQLException) {
            throw RuntimeException(e.toString(), e)
        }
    }

    fun rollbackTransaction() {
        val con = localConnection.get() ?: error("Not in transaction")
        try {
            con.rollback()
        } catch (e: SQLException) {
            throw RuntimeException(e.toString(), e)
        }

        transactionCaches.get().clear()
        transactionBatches.get().clear()
        dp.dbCacheManager.flushCache()
    }

    fun endTransaction() {
        val con = localConnection.get() ?: error("Not in transaction")
        localConnection.remove()
        transactionCaches.get().clear()
        transactionCaches.remove()
        transactionBatches.get().clear()
        transactionBatches.remove()
        DbUtils.close(con)
    }

    fun optimizeTable(tableName: String) {
        useDslContext { ctx ->
            try {
                when (ctx.dialect()) {
                    SQLDialect.MYSQL, SQLDialect.MARIADB -> ctx.execute("OPTIMIZE NO_WRITE_TO_BINLOG TABLE $tableName")
                    else -> {}
                }
            } catch (e: Exception) {
                logger.safeDebug(e) { "Failed to optimize table $tableName" }
            }
        }
    }
}
