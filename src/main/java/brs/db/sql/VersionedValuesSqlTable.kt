package brs.db.sql

import brs.DependencyProvider
import brs.db.VersionedValuesTable
import org.jooq.impl.TableImpl

abstract class VersionedValuesSqlTable<T, V> internal constructor(table: String, tableClass: TableImpl<*>, dbKeyFactory: DbKey.Factory<T>, private val dp: DependencyProvider) : ValuesSqlTable<T, V>(table, tableClass, dbKeyFactory, true, dp), VersionedValuesTable<T, V> {
    override fun rollback(height: Int) {
        VersionedEntitySqlTable.rollback(dp, table, tableClass, heightField, latestField, height, dbKeyFactory)
    }

    override fun trim(height: Int) {
        VersionedEntitySqlTable.trim(dp, tableClass, heightField, height, dbKeyFactory)
    }
}
