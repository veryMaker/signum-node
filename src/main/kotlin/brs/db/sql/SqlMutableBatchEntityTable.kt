package brs.db.sql

import brs.db.*
import brs.entity.DependencyProvider
import brs.util.cache.set
import org.ehcache.Cache
import org.jooq.*
import org.jooq.Table
import org.jooq.impl.DSL

internal abstract class SqlMutableBatchEntityTable<T> internal constructor(
    table: Table<*>,
    heightField: Field<Int>,
    latestField: Field<Boolean>,
    dbKeyFactory: SqlDbKey.Factory<T>,
    override val cacheValueClass: Class<T>,
    private val dp: DependencyProvider
) : SqlMutableEntityTable<T>(table, heightField, latestField, dbKeyFactory, dp), MutableBatchEntityTable<T>, CachedTable<SqlDbKey, T> {
    override val count: Int
        get() {
            ensureBatchIsEmpty()
            return super.count
        }

    override val rowCount: Int
        get() {
            ensureBatchIsEmpty()
            return super.rowCount
        }

    override val cacheKeyClass = SqlDbKey::class.java
    override val cacheName: String = table.name

    private var lastFlushHeight: Int = -1

    @Suppress("UNCHECKED_CAST")
    private val batch: MutableMap<SqlDbKey, T>
        get() = dp.db.getBatch<T>(table) as MutableMap<SqlDbKey, T>

    private val batchCache: Cache<SqlDbKey, T> get() = getCache(dp)

    private fun ensureBatchIsEmpty() {
        if (batch.isNotEmpty() && dp.db.isInTransaction()) flushBatch(dp.blockchainService.height + 1)
    }

    override fun delete(t: T): Boolean {
        dp.db.assertInTransaction()
        val dbKey = dbKeyFactory.newKey(t)
        check(dbKey is SqlDbKey)
        batchCache.remove(dbKey)
        batch.remove(dbKey)
        return true
    }

    final override fun save(ctx: DSLContext, entity: T) {
        insert(entity)
    }

    final override fun save(ctx: DSLContext, entities: Collection<T>) {
        if (entities.isEmpty()) return
        entities.forEach { insert(it) }
    }

    override fun get(dbKey: BurstKey): T? {
        require(dbKey is SqlDbKey)
        if (batchCache.containsKey(dbKey)) {
            return batchCache.get(dbKey)
        } else if (dp.db.isInTransaction() && batch.containsKey(dbKey)) {
            return batch[dbKey]
        }
        val item = super.get(dbKey)
        if (item != null) {
            batchCache[dbKey] = item
        }
        return item
    }

    override fun insert(entity: T) {
        dp.db.assertInTransaction()
        val key = dbKeyFactory.newKey(entity)
        check(key is SqlDbKey)
        batch[key] = entity
        batchCache[key] = entity
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

    protected abstract fun saveBatch(ctx: DSLContext, entities: Collection<T>)

    final override fun flushBatch(height: Int) {
        dp.db.assertInTransaction()
        if (batch.isEmpty()) return
        require(height != lastFlushHeight) { "Already finished block height $height and batch is not empty" }
        lastFlushHeight = height

        dp.db.useDslContext { ctx ->
            // Update "latest" fields.
            if (ctx.dialect() == SQLDialect.SQLITE) {
                // This is chunked as SQLite is limited to expression tress of depth 1000.
                // We have "UPDATE table", "SET latestField = false" and "WHERE latestField = true" so we have room for 997 more conditions.
                batch.keys.chunked(997).forEach { chunk -> updateLatest(ctx, chunk) }
            } else {
                updateLatest(ctx, batch.keys)
            }

            saveBatch(ctx, batch.values)
            batch.clear()
        }
    }

    override fun get(dbKey: BurstKey, height: Int): T? {
        ensureBatchIsEmpty()
        return super.get(dbKey, height)
    }

    override fun getBy(condition: Condition): T? {
        ensureBatchIsEmpty()
        return super.getBy(condition)
    }

    override fun getBy(condition: Condition, height: Int): T? {
        ensureBatchIsEmpty()
        return super.getBy(condition, height)
    }

    override fun getManyBy(condition: Condition, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T> {
        ensureBatchIsEmpty()
        return super.getManyBy(condition, from, to, sort)
    }

    override fun getManyBy(condition: Condition, height: Int, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T> {
        ensureBatchIsEmpty()
        return super.getManyBy(condition, height, from, to, sort)
    }

    override fun getManyBy(ctx: DSLContext, query: SelectQuery<out Record>, cache: Boolean): Collection<T> {
        ensureBatchIsEmpty()
        return super.getManyBy(ctx, query, cache)
    }

    override fun getAll(from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T> {
        ensureBatchIsEmpty()
        return super.getAll(from, to, sort)
    }

    override fun getAll(height: Int, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T> {
        ensureBatchIsEmpty()
        return super.getAll(height, from, to, sort)
    }

    override fun rollback(height: Int) {
        super.rollback(height)
        batch.clear()
        lastFlushHeight = -1
    }
}
