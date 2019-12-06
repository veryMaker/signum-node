package brs.db.sql

import brs.db.ValuesTable
import brs.entity.DependencyProvider
import org.jooq.impl.TableImpl

internal abstract class VersionedValuesSqlTable<T, V> internal constructor(
    table: String,
    tableClass: TableImpl<*>,
    dbKeyFactory: SqlDbKey.Factory<T>,
    private val dp: DependencyProvider
) : ValuesSqlTable<T, V>(table, tableClass, dbKeyFactory, true, dp), ValuesTable<T, V> {
    override fun rollback(height: Int) {
        VersionedEntitySqlTable.rollback(dp, table, tableClass, heightField, latestField, height, dbKeyFactory)
    }

    override fun trim(height: Int) {
        VersionedEntitySqlTable.trim(dp, tableClass, heightField, height, dbKeyFactory)
    }
}
