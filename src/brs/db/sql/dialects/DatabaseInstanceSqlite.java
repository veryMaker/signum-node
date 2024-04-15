package brs.db.sql.dialects;

import brs.Signum;
import brs.props.PropertyService;
import brs.props.Props;
import com.zaxxer.hikari.HikariConfig;
import org.jooq.SQLDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseInstanceSqlite extends DatabaseInstanceBaseImpl {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseInstanceSqlite.class);

  protected DatabaseInstanceSqlite(PropertyService propertyService) {
    super(propertyService);
  }

  @Override
  protected HikariConfig configureImpl(HikariConfig config) {
    config.setMaximumPoolSize(10); // 1 is not working
    config.setConnectionTestQuery("SELECT 1;");
    config.addDataSourceProperty("foreign_keys", "on");
    config.addDataSourceProperty("journal_mode", "WAL");
    config.addDataSourceProperty("busy_timeout", "10000");
    config.addDataSourceProperty("wal_autocheckpoint", "500");
    return config;
  }

  @Override
  protected void onShutdownImpl() {
    logger.info("Applying SQLite Checkpoint...");
    executeSQL("PRAGMA wal_checkpoint(TRUNCATE)");
  }

  @Override
  public SQLDialect getDialect() {
    return SQLDialect.SQLITE;
  }


  @Override
  protected void onStartupImpl() {

    if (Signum.getPropertyService().getBoolean(Props.DB_OPTIMIZE)) {
      logger.info("SQLite optimization started...");
      executeSQL("PRAGMA optimize");
      logger.info("SQLite VACUUM started, this can take a while");
      executeSQL("VACUUM");
    }

  }


  @Override
  public String getMigrationSqlScriptPath() {
    return "classpath:/db/migration_sqlite";
  }

  @Override
  public String getDatabaseVersionSQLScript() {
    return "SELECT sqlite_version()";
  }

  @Override
  public boolean isStable() {
    return false;
  }
}
