package brs.db.sql

import brs.db.*
import brs.entity.DependencyProvider
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.TableImpl

internal abstract class SqlMutableBatchValuesTable<K, V> internal constructor(
    tableClass: TableImpl<*>,
    heightField: Field<Int>,
    latestField: Field<Boolean>,
    dbKeyFactory: SqlDbKey.Factory<K>,
    private val dp: DependencyProvider
) : SqlValuesTable<K, V>(tableClass, heightField, latestField, dbKeyFactory, dp), ValuesTable<K, V>, BatchTable {
    private var lastFinishHeight: Int = -1

    @Suppress("UNCHECKED_CAST")
    private val batch: MutableMap<SqlDbKey, Pair<K, List<V>>>
        get() = dp.db.getBatch<Pair<K, List<V>>>(table) as MutableMap<SqlDbKey, Pair<K, List<V>>>

    // TODO cache

    private fun assertNotInTransaction() {
        check(!dp.db.isInTransaction()) { "Cannot use batch table during transaction" }
    }

    final override fun save(ctx: DSLContext, key: K, values: List<V>) {
        insert(key, values)
    }

    override fun insert(key: K, values: List<V>) {
        dp.db.assertInTransaction()
        val dbKey = dbKeyFactory.newKey(key)
        check(dbKey is SqlDbKey)
        batch[dbKey] = Pair(key, values)
    }

    private fun updateLatest(ctx: DSLContext, keys: Collection<SqlDbKey>) {
        val updateQuery = ctx.updateQuery(table)
        updateQuery.addConditions(latestField?.isTrue)
        updateQuery.addValue(latestField, false)
        var updateCondition = DSL.noCondition()
        keys.forEach { key -> updateCondition = updateCondition.or(key.allPrimaryKeyConditions) }
        updateQuery.addConditions(updateCondition)
        updateQuery.execute()
    }

    protected abstract fun saveBatch(ctx: DSLContext, entities: Map<K, List<V>>)

    final override fun flushBatch(height: Int) {
        dp.db.assertInTransaction()
        if (batch.isEmpty()) return
        require(height != lastFinishHeight) { "Already finished block height $height and batch is not empty" }
        lastFinishHeight = height

        dp.db.useDslContext { ctx ->
            // Update "latest" fields.
            if (ctx.dialect() == SQLDialect.SQLITE) {
                // This is chunked as SQLite is limited to expression tress of depth 1000.
                // We have "WHERE latestField = true" and "SET latestField = false" so we have room for 998 more conditions.
                batch.keys.chunked(998).forEach { chunk -> updateLatest(ctx, chunk) }
            } else {
                updateLatest(ctx, batch.keys)
            }

            saveBatch(ctx, batch.values.toMap())
            batch.clear()
        }
    }

    override fun get(dbKey: BurstKey): List<V> {
        require(dbKey is SqlDbKey)
        return if (dp.db.isInTransaction() && batch.containsKey(dbKey)) {
            batch[dbKey]!!.second
        } else {
            super.get(dbKey)
        }
    }

    override fun rollback(height: Int) {
        SqlMutableEntityTable.rollback(dp, cache, table, heightField, latestField, height, dbKeyFactory)
        batch.clear()
        lastFinishHeight = -1
    }

    override fun trim(height: Int) {
        SqlMutableEntityTable.trim(dp, table, heightField, height, dbKeyFactory)
    }
}
