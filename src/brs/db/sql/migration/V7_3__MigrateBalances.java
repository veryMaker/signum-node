package brs.db.sql.migration;

import java.sql.Statement;

import brs.db.sql.Db;
import brs.props.Props;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.SQLDialect;
import org.jooq.tools.jdbc.JDBCUtils;

public class V7_3__MigrateBalances extends BaseJavaMigration {

  public void migrate(Context context) throws Exception {

    if (Db.getDialect() == SQLDialect.SQLITE ||
      Db.getDialect() == SQLDialect.POSTGRES
    ) {
      return;
    }


    // copy all balance entries
    Statement selectTx = context.getConnection().createStatement();
    selectTx.executeUpdate(
      "INSERT INTO account_balance(id, balance, unconfirmed_balance, forged_balance, height, latest) " +
        "SELECT id, balance, unconfirmed_balance, forged_balance, height, latest FROM account"
    );
  }
}
