package brs.db.sql;

import brs.Signum;
import brs.db.SignumKey;
import brs.db.cache.DBCacheManagerImpl;
import brs.db.store.Dbs;
import brs.props.PropertyService;
import brs.props.Props;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.conf.StatementType;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.JDBCUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Db {

  private static final Logger logger = LoggerFactory.getLogger(Db.class);

  private static HikariDataSource cp;
  private static SQLDialect dialect;
  private static final ThreadLocal<Connection> localConnection = new ThreadLocal<>();
  private static final ThreadLocal<Map<String, Map<SignumKey, Object>>> transactionCaches = new ThreadLocal<>();
  private static final ThreadLocal<Map<String, Map<SignumKey, Object>>> transactionBatches = new ThreadLocal<>();

  private static DBCacheManagerImpl dbCacheManager;

  private static Flyway flyway;

  public static void init(PropertyService propertyService, DBCacheManagerImpl dbCacheManager) {
    Db.dbCacheManager = dbCacheManager;

    String dbUrl;
    String dbUsername;
    String dbPassword;

    dbUrl = propertyService.getString(Props.DB_URL);
    dbUsername = propertyService.getString(Props.DB_USERNAME);
    dbPassword = propertyService.getString(Props.DB_PASSWORD);
    dialect = JDBCUtils.dialect(dbUrl);

    logger.debug("Database jdbc url set to: {}", dbUrl);
    try {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      config.setAutoCommit(true);
      if (dbUsername != null)
        config.setUsername(dbUsername);
      if (dbPassword != null)
        config.setPassword(dbPassword);

      config.setMaximumPoolSize(propertyService.getInt(Props.DB_CONNECTIONS));

      FluentConfiguration flywayBuilder = Flyway.configure()
        .dataSource(dbUrl, dbUsername, dbPassword)
        .baselineOnMigrate(true);
      String locationDialect = null;
      String location = "classpath:/brs/db/sql/migration";

      switch (dialect) {
        case MYSQL:
        case MARIADB:
          locationDialect = "classpath:/db/migration_mariadb";
          config.addDataSourceProperty("cachePrepStmts", "true");
          config.addDataSourceProperty("prepStmtCacheSize", "512");
          config.addDataSourceProperty("prepStmtCacheSqlLimit", "4096");
          config.addDataSourceProperty("characterEncoding", "utf8mb4");
          config.addDataSourceProperty("cacheServerConfiguration", "true");
          config.addDataSourceProperty("useLocalSessionState", "true");
          config.addDataSourceProperty("useLocalTransactionState", "true");
          config.addDataSourceProperty("useUnicode", "true");
          config.addDataSourceProperty("useServerPrepStmts", "true");
          config.addDataSourceProperty("rewriteBatchedStatements", "true");
          config.addDataSourceProperty("maintainTimeStats", "false");
          config.addDataSourceProperty("useUnbufferedIO", "false");
          config.addDataSourceProperty("useReadAheadInput", "false");
          config.setConnectionInitSql("SET NAMES utf8mb4;");
          break;
        case H2:
          Class.forName("org.h2.Driver");
          locationDialect = "classpath:/db/migration_h2_v2";
          config.setAutoCommit(true);
          config.addDataSourceProperty("cachePrepStmts", "true");
          config.addDataSourceProperty("prepStmtCacheSize", "250");
          config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
          config.addDataSourceProperty("DATABASE_TO_UPPER", "false");
          config.addDataSourceProperty("CASE_INSENSITIVE_IDENTIFIERS", "true");
          break;
        case POSTGRES:
          Class.forName("org.postgresql.Driver");
          locationDialect = "classpath:/db/migration_postgres";
          // check https://jdbc.postgresql.org/documentation/use/ for more options
        default:
          break;
      }

      flywayBuilder.locations(location, locationDialect);
      cp = new HikariDataSource(config);

      logger.info("Running flyway migration");
      flyway = flywayBuilder.load();
      flyway.migrate();
    } catch (Exception e) {
      throw new RuntimeException(e.toString(), e);
    }
  }

  public static void clean() {
    try {
      flyway.clean();
      flyway.migrate();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }

  private Db() {
  } // never

  public static Dbs getDbsByDatabaseType() {
    logger.info("Using SQL Backend with Dialect {} - Version {}", dialect.getName(), getDatabaseVersion());
    return new SqlDbs();
  }


  public static void analyzeTables() {
    if (dialect == SQLDialect.H2) {
      try (Connection con = cp.getConnection();
           Statement stmt = con.createStatement()) {
        stmt.execute("ANALYZE SAMPLE_SIZE 0");
      } catch (SQLException e) {
        throw new RuntimeException(e.toString(), e);
      }
    }
  }

  public static void shutdown() {
    if (cp == null || cp.isClosed()) {
      return;
    }
    if (dialect == SQLDialect.H2) {
      try {
        Connection con = cp.getConnection();
        Statement stmt = con.createStatement();
        // COMPACT is not giving good result.
        if (Signum.getPropertyService().getBoolean(Props.DB_H2_DEFRAG_ON_SHUTDOWN)) {
          logger.info("H2 defragmentation started, this can take a while");
          stmt.execute("SHUTDOWN DEFRAG");
        } else {
          stmt.execute("SHUTDOWN");
        }
      } catch (SQLException e) {
        logger.info(e.toString(), e);
      } finally {
        logger.info("Database shutdown completed.");
      }
    }
    if (cp != null && !cp.isClosed()) {
      cp.close();
    }
  }

  public static void backup(String filename) {
    if (dialect == SQLDialect.H2) {
      logger.info("Database backup to {} started, it might take a while.", filename);
      try (Connection con = cp.getConnection(); Statement stmt = con.createStatement()) {
        stmt.execute("BACKUP TO '" + filename + "'");
      } catch (SQLException e) {
        logger.info(e.toString(), e);
      } finally {
        logger.info("Database backup completed, file {}.", filename);
      }
    } else {
      logger.error("Backup not yet implemented for {}", dialect.toString());
    }
  }

  private static Connection getPooledConnection() throws SQLException {
    return cp.getConnection();
  }

  public static Connection getConnection() throws SQLException {
    Connection con = localConnection.get();
    if (con != null) {
      return con;
    }

    con = getPooledConnection();
    con.setAutoCommit(true);

    return con;
  }

  public static <T> T useDSLContext(Function<DSLContext, T> function) {
    return function.apply(getDSLContext());
  }

  public static void useDSLContext(Consumer<DSLContext> consumer) { // TODO RxJava
    consumer.accept(getDSLContext());
  }

  private static DSLContext getDSLContext() {
    Connection con = localConnection.get();
    Settings settings = new Settings();

    settings.setRenderSchema(Boolean.FALSE);

    if (con == null) {
      return DSL.using(cp, dialect, settings);
    } else {
      settings.setStatementType(StatementType.STATIC_STATEMENT);
      return DSL.using(con, dialect, settings);
    }
  }

  static <V> Map<SignumKey, V> getCache(String tableName) {
    if (!isInTransaction()) {
      throw new IllegalStateException("Not in transaction");
    }
    //noinspection unchecked
    return (Map<SignumKey, V>) transactionCaches.get().computeIfAbsent(tableName, k -> new HashMap<>());
  }

  static <V> Map<SignumKey, V> getBatch(String tableName) {
    if (!isInTransaction()) {
      throw new IllegalStateException("Not in transaction");
    }
    //noinspection unchecked
    return (Map<SignumKey, V>) transactionBatches.get().computeIfAbsent(tableName, k -> new HashMap<>());
  }

  public static boolean isInTransaction() {
    return localConnection.get() != null;
  }

  public static Connection beginTransaction() {
    if (localConnection.get() != null) {
      throw new IllegalStateException("Transaction already in progress");
    }
    try {
      Connection con = cp.getConnection();
      con.setAutoCommit(false);

      localConnection.set(con);
      transactionCaches.set(new HashMap<>());
      transactionBatches.set(new HashMap<>());

      return con;
    } catch (Exception e) {
      throw new RuntimeException(e.toString(), e);
    }
  }

  public static void commitTransaction() {
    Connection con = localConnection.get();
    if (con == null) {
      throw new IllegalStateException("Not in transaction");
    }
    try {
      con.commit();
    } catch (SQLException e) {
      throw new RuntimeException(e.toString(), e);
    }
  }

  public static void rollbackTransaction() {
    Connection con = localConnection.get();
    if (con == null) {
      throw new IllegalStateException("Not in transaction");
    }
    try {
      con.rollback();
    } catch (SQLException e) {
      throw new RuntimeException(e.toString(), e);
    }
    transactionCaches.get().clear();
    transactionBatches.get().clear();
    dbCacheManager.flushCache();
  }

  public static void endTransaction() {
    Connection con = localConnection.get();
    if (con == null) {
      throw new IllegalStateException("Not in transaction");
    }
    localConnection.set(null);
    transactionCaches.get().clear();
    transactionCaches.set(null);
    transactionBatches.get().clear();
    transactionBatches.set(null);
    DbUtils.close(con);
  }

  public static void optimizeTable(String tableName) {
    useDSLContext(ctx -> {
      try {
        switch (ctx.dialect()) {
          case MYSQL:
          case MARIADB:
            ctx.execute("OPTIMIZE NO_WRITE_TO_BINLOG TABLE " + tableName);
            break;
          default:
            break;
        }
      } catch (Exception e) {
        logger.debug("Failed to optimize table {}", tableName, e);
      }
    });
  }

  private static String getDatabaseVersion() {
    DSLContext ctx = getDSLContext();
    ResultQuery queryVersion;
    String version = "N/A";
    try {
      if (ctx.dialect() == SQLDialect.H2) {
        queryVersion = ctx.resultQuery("SELECT H2VERSION()");
      } else {
        queryVersion = ctx.resultQuery("SELECT VERSION()");
      }
      Record record = queryVersion.fetchOne();
      if (record != null) {
        version = record.get(0, String.class);
        if (ctx.dialect() == SQLDialect.POSTGRES) {
          version += " (EXPERIMENTAL)";
        }
      }
    } catch (Exception e) {
      logger.warn("Failed to fetch version");
    }
    return version;
  }
}
