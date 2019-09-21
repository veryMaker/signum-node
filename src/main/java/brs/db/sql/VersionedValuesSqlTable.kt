package brs.db.sql

import brs.DependencyProvider
import brs.db.VersionedValuesTable
import brs.db.store.DerivedTableManager
import org.jooq.impl.TableImpl

abstract class VersionedValuesSqlTable<T, V> internal constructor(table: String, tableClass: TableImpl<*>, dbKeyFactory: DbKey.Factory<T>, dp: DependencyProvider) : ValuesSqlTable<T, V>(table, tableClass, dbKeyFactory, true, dp), VersionedValuesTable<T, V> {
    override fun rollback(height: Int) {
        VersionedEntitySqlTable.rollback(table, tableClass, heightField, latestField, height, dbKeyFactory)
    }

    override fun trim(height: Int) {
        VersionedEntitySqlTable.trim(tableClass, heightField, height, dbKeyFactory)
    }
}
