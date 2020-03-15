package brs.db.sql

import brs.db.BurstKey
import brs.db.ValuesTable
import brs.db.assertInTransaction
import brs.db.useDslContext
import brs.entity.DependencyProvider
import brs.util.db.fetchAndMap
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.impl.TableImpl

internal abstract class SqlValuesTable<K, V> internal constructor(
    tableClass: TableImpl<*>,
    heightField: Field<Int>,
    internal val latestField: Field<Boolean>,
    internal val dbKeyFactory: SqlDbKey.Factory<K>,
    private val dp: DependencyProvider
) : SqlDerivedTable<K>(tableClass, heightField, dp), ValuesTable<K, V> {
    protected abstract fun load(ctx: DSLContext, record: Record): V

    protected abstract fun save(ctx: DSLContext, key: K, values: List<V>)

    internal val cache: MutableMap<BurstKey, List<V>>
        get() = dp.db.getCache(table)

    override fun clearCache() {
        cache.clear()
    }

    override fun get(dbKey: BurstKey): List<V> {
        return dp.db.useDslContext { ctx ->
            val key = dbKey as SqlDbKey
            if (dp.db.isInTransaction()) cache[key]?.let { return@useDslContext it }
            val values = ctx.selectFrom(table)
                .where(key.primaryKeyConditions)
                .and(latestField.isTrue)
                .orderBy(table.field("db_id").desc())
                .fetchAndMap { record -> load(ctx, record) }
            if (dp.db.isInTransaction()) cache[key] = values
            values
        }
    }

    override fun insert(key: K, values: List<V>) {
        dp.db.assertInTransaction()
        dp.db.useDslContext { ctx ->
            val dbKey = dbKeyFactory.newKey(key) as SqlDbKey
            cache[dbKey] = values
            ctx.update(table)
                .set(latestField, false)
                .where(dbKey.primaryKeyConditions)
                .and(latestField.isTrue)
                .execute() // TODO this is optimal! do this elsewhere
            save(ctx, key, values)
        }
    }
}
