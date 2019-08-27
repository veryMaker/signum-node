package brs.db.sql;

import brs.DependencyProvider;
import brs.db.VersionedValuesTable;
import brs.db.store.DerivedTableManager;
import org.jooq.impl.TableImpl;

public abstract class VersionedValuesSqlTable<T, V> extends ValuesSqlTable<T, V> implements VersionedValuesTable<T, V> {
  VersionedValuesSqlTable(String table, TableImpl<?> tableClass, DbKey.Factory<T> dbKeyFactory, DependencyProvider dp) {
    super(table, tableClass, dbKeyFactory, true, dp);
  }

  @Override
  public final void rollback(int height) {
    VersionedEntitySqlTable.rollback(table, tableClass, heightField, latestField, height, dbKeyFactory);
  }

  @Override
  public final void trim(int height) {
    VersionedEntitySqlTable.trim(tableClass, heightField, height, dbKeyFactory);
  }
}
