package brs.db.sql

import brs.db.ValuesTable
import brs.entity.DependencyProvider
import org.jooq.Field
import org.jooq.impl.TableImpl

internal abstract class SqlVersionedValuesTable<T, V> internal constructor(
    tableClass: TableImpl<*>,
    heightField: Field<Int>,
    latestField: Field<Boolean>,
    dbKeyFactory: SqlDbKey.Factory<T>,
    private val dp: DependencyProvider
) : SqlValuesTable<T, V>(tableClass, heightField, latestField, dbKeyFactory, dp), ValuesTable<T, V> {
    override fun rollback(height: Int) {
        SqlMutableEntityTable.rollback(dp, cache, table, heightField, latestField, height, dbKeyFactory)
    }

    override fun trim(height: Int) {
        SqlMutableEntityTable.trim(dp, table, heightField, height, dbKeyFactory)
    }
}
