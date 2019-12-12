package brs.db.sql

import brs.db.BurstKey
import brs.db.VersionedBatchEntityTable
import brs.db.assertInTransaction
import brs.db.useDslContext
import brs.entity.DependencyProvider
import org.ehcache.Cache
import org.jooq.*
import org.jooq.impl.TableImpl

internal abstract class VersionedBatchEntitySqlTable<T> internal constructor(
    table: String,
    tableClass: TableImpl<*>,
    heightField: Field<Int>,
    latestField: Field<Boolean>?,
    dbKeyFactory: SqlDbKey.Factory<T>,
    private val tClass: Class<T>,
    private val dp: DependencyProvider
) : VersionedEntitySqlTable<T>(table, tableClass, heightField, latestField, dbKeyFactory, dp), VersionedBatchEntityTable<T> {
    override val count: Int
        get() {
            assertNotInTransaction()
            return super.count
        }

    override val rowCount: Int
        get() {
            assertNotInTransaction()
            return super.rowCount
        }

    override fun getBatch(): MutableMap<BurstKey, T> = dp.db.getBatch(table)

    override fun getCache(): Cache<BurstKey, T> = dp.dbCacheService.getCache(table, tClass)!!

    private fun assertNotInTransaction() {
        check(!dp.db.isInTransaction()) { "Cannot use in batch table transaction" }
    }

    protected abstract fun bulkInsert(ctx: DSLContext, t: Collection<T>)

    override fun delete(t: T): Boolean {
        dp.db.assertInTransaction()
        val dbKey = dbKeyFactory.newKey(t) as SqlDbKey
        getCache().remove(dbKey)
        getBatch().remove(dbKey)
        return true
    }

    override fun get(dbKey: BurstKey): T? {
        if (getCache().containsKey(dbKey)) {
            return getCache().get(dbKey)
        } else if (dp.db.isInTransaction() && getBatch().containsKey(dbKey)) {
            return getBatch()[dbKey]
        }
        val item = super.get(dbKey)
        if (item != null) {
            getCache().put(dbKey, item)
        }
        return item
    }

    override fun insert(t: T) {
        dp.db.assertInTransaction()
        val key = dbKeyFactory.newKey(t)
        getBatch()[key] = t
        getCache().put(key, t)
    }

    override fun finish() {
        dp.db.assertInTransaction()
        val keySet = getBatch().keys
        if (keySet.isEmpty()) {
            return
        }

        dp.db.useDslContext { ctx ->
            val updateQuery = ctx.updateQuery(tableClass)
            updateQuery.addValue(latestField, false)
            for (idColumn in dbKeyFactory.pkColumns) {
                updateQuery.addConditions(tableClass.field(idColumn, Long::class.java).eq(0L))
            }
            updateQuery.addConditions(latestField?.isTrue)

            val updateBatch = ctx.batch(updateQuery)
            for (dbKey in keySet) {
                val pkValues = dbKey.pkValues
                val bindArgs = arrayOfNulls<Any>(pkValues.size + 1)
                bindArgs[0] = false
                for (i in pkValues.indices) {
                    bindArgs[i + 1] = pkValues[i]
                }
                updateBatch.bind(bindArgs)
            }
            updateBatch.execute()
            bulkInsert(ctx, getBatch().values)
            getBatch().clear()
        }
    }

    override fun get(dbKey: BurstKey, height: Int): T? {
        assertNotInTransaction()
        return super.get(dbKey, height)
    }

    override fun getBy(condition: Condition): T? {
        assertNotInTransaction()
        return super.getBy(condition)
    }

    override fun getBy(condition: Condition, height: Int): T? {
        assertNotInTransaction()
        return super.getBy(condition, height)
    }

    override fun getManyBy(condition: Condition, from: Int, to: Int): Collection<T> {
        assertNotInTransaction()
        return super.getManyBy(condition, from, to)
    }

    override fun getManyBy(condition: Condition, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T> {
        assertNotInTransaction()
        return super.getManyBy(condition, from, to, sort)
    }

    override fun getManyBy(condition: Condition, height: Int, from: Int, to: Int): Collection<T> {
        assertNotInTransaction()
        return super.getManyBy(condition, height, from, to)
    }

    override fun getManyBy(
        condition: Condition,
        height: Int,
        from: Int,
        to: Int,
        sort: Collection<SortField<*>>
    ): Collection<T> {
        assertNotInTransaction()
        return super.getManyBy(condition, height, from, to, sort)
    }

    override fun getManyBy(ctx: DSLContext, query: SelectQuery<out Record>, cache: Boolean): Collection<T> {
        assertNotInTransaction()
        return super.getManyBy(ctx, query, cache)
    }

    override fun getAll(from: Int, to: Int): Collection<T> {
        assertNotInTransaction()
        return super.getAll(from, to)
    }

    override fun getAll(from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T> {
        assertNotInTransaction()
        return super.getAll(from, to, sort)
    }

    override fun getAll(height: Int, from: Int, to: Int): Collection<T> {
        assertNotInTransaction()
        return super.getAll(height, from, to)
    }

    override fun getAll(height: Int, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T> {
        assertNotInTransaction()
        return super.getAll(height, from, to, sort)
    }

    override fun rollback(height: Int) {
        super.rollback(height)
        getBatch().clear()
    }

    override fun flushCache() {
        getCache().clear()
    }
}
