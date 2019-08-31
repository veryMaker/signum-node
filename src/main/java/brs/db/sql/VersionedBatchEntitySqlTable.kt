package brs.db.sql

import brs.DependencyProvider
import brs.db.BurstKey
import brs.db.VersionedBatchEntityTable
import brs.db.cache.DBCacheManagerImpl
import brs.db.store.DerivedTableManager
import org.ehcache.Cache
import org.jooq.*
import org.jooq.impl.TableImpl

import java.util.*

abstract class VersionedBatchEntitySqlTable<T> internal constructor(table: String, tableClass: TableImpl<*>, dbKeyFactory: DbKey.Factory<T>, private val tClass: Class<T>, private val dp: DependencyProvider) : VersionedEntitySqlTable<T>(table, tableClass, dbKeyFactory, dp), VersionedBatchEntityTable<T> {

    override val count: Int
        get() {
            assertInTransaction()
            return super.count
        }

    override val rowCount: Int
        get() {
            assertInTransaction()
            return super.rowCount
        }

    override val batch: MutableMap<BurstKey, T>
        get() = Db.getBatch(table)

    override val cache: Cache<BurstKey, T>?
        get() = dp.dbCacheManager.getCache(table, tClass)

    private fun assertInTransaction() {
        check(!Db.isInTransaction) { "Cannot use in batch table transaction" }
    }

    private fun assertNotInTransaction() {
        check(Db.isInTransaction) { "Not in transaction" }
    }

    protected abstract fun bulkInsert(ctx: DSLContext, t: Collection<T>)

    override fun delete(t: T?): Boolean {
        assertNotInTransaction()
        val dbKey = dbKeyFactory.newKey(t) as DbKey
        cache!!.remove(dbKey)
        batch.remove(dbKey)
        return true
    }

    override fun get(dbKey: BurstKey): T? {
        if (cache!!.containsKey(dbKey)) {
            return cache!!.get(dbKey)
        } else if (Db.isInTransaction && batch.containsKey(dbKey)) {
            return batch[dbKey]
        }
        val item = super.get(dbKey)
        if (item != null) {
            cache!!.put(dbKey, item)
        }
        return item
    }

    override fun insert(t: T) {
        assertNotInTransaction()
        val key = dbKeyFactory.newKey(t)
        batch[key] = t
        cache!!.put(key, t)
    }

    override fun finish() {
        assertNotInTransaction()
        val keySet = batch.keys
        if (keySet.isEmpty()) {
            return
        }

        Db.useDSLContext { ctx ->
            val updateQuery = ctx.updateQuery<*>(tableClass)
            updateQuery.addValue(latestField, false)
            for (idColumn in dbKeyFactory.pkColumns) {
                updateQuery.addConditions(tableClass.field(idColumn, Long::class.java).eq(0L))
            }
            updateQuery.addConditions(latestField.isTrue)

            val updateBatch = ctx.batch(updateQuery)
            for (dbKey in keySet) {
                val pkValues = dbKey.pkValues
                val bindArgs = arrayOfNulls<Any>(pkValues.size + 1)
                bindArgs[0] = false
                System.arraycopy(pkValues, 0, bindArgs, 1, pkValues.size)
                updateBatch.bind(*bindArgs) // TODO once in kotlin just do bind(false, *pkValues)
            }
            updateBatch.execute()
            bulkInsert(ctx, batch.values)
            batch.clear()
        }
    }

    override fun get(dbKey: BurstKey, height: Int): T {
        assertInTransaction()
        return super.get(dbKey, height)
    }

    override fun getBy(condition: Condition): T {
        assertInTransaction()
        return super.getBy(condition)
    }

    override fun getBy(condition: Condition, height: Int): T {
        assertInTransaction()
        return super.getBy(condition, height)
    }

    override fun getManyBy(condition: Condition, from: Int, to: Int): Collection<T> {
        assertInTransaction()
        return super.getManyBy(condition, from, to)
    }

    override fun getManyBy(condition: Condition, from: Int, to: Int, sort: List<SortField<*>>): Collection<T> {
        assertInTransaction()
        return super.getManyBy(condition, from, to, sort)
    }

    override fun getManyBy(condition: Condition, height: Int, from: Int, to: Int): Collection<T> {
        assertInTransaction()
        return super.getManyBy(condition, height, from, to)
    }

    override fun getManyBy(condition: Condition, height: Int, from: Int, to: Int, sort: List<SortField<*>>): Collection<T> {
        assertInTransaction()
        return super.getManyBy(condition, height, from, to, sort)
    }

    override fun getManyBy(ctx: DSLContext, query: SelectQuery<out Record>, cache: Boolean): Collection<T> {
        assertInTransaction()
        return super.getManyBy(ctx, query, cache)
    }

    override fun getAll(from: Int, to: Int): Collection<T> {
        assertInTransaction()
        return super.getAll(from, to)
    }

    override fun getAll(from: Int, to: Int, sort: List<SortField<*>>): Collection<T> {
        assertInTransaction()
        return super.getAll(from, to, sort)
    }

    override fun getAll(height: Int, from: Int, to: Int): Collection<T> {
        assertInTransaction()
        return super.getAll(height, from, to)
    }

    override fun getAll(height: Int, from: Int, to: Int, sort: List<SortField<*>>): Collection<T> {
        assertInTransaction()
        return super.getAll(height, from, to, sort)
    }

    override fun rollback(height: Int) {
        super.rollback(height)
        batch.clear()
    }

    override fun truncate() {
        super.truncate()
        batch.clear()
    }

    override fun flushCache() {
        cache!!.clear()
    }
}
