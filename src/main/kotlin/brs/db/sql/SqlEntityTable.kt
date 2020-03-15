package brs.db.sql

import brs.db.BurstKey
import brs.db.EntityTable
import brs.db.assertInTransaction
import brs.db.useDslContext
import brs.entity.DependencyProvider
import brs.util.db.fetchAndMap
import org.jooq.*
import org.jooq.impl.DSL

internal abstract class SqlEntityTable<T> internal constructor(
    table: Table<*>,
    internal val dbKeyFactory: SqlDbKey.Factory<T>,
    heightField: Field<Int>,
    /** If not null then this is multi-version, if null this is not */
    internal val latestField: Field<Boolean>?,
    private val dp: DependencyProvider
) : SqlDerivedTable<T>(table, heightField, dp), EntityTable<T> {
    override val defaultSort: Collection<SortField<*>> by lazy {
        val sort = mutableListOf<SortField<*>>()
        if (latestField != null) {
            this.dbKeyFactory.primaryKeyColumns.forEach { column ->
                sort.add(column.asc())
            }
        }
        sort.add(heightField.desc())
        sort
    }

    override val count
        get() = dp.db.useDslContext { ctx ->
            val r = ctx.selectCount().from(table)
            (if (latestField != null) r.where(latestField.isTrue) else r).fetchOne(0, Int::class.javaPrimitiveType)!!
        }

    override val rowCount
        get() = dp.db.useDslContext { ctx ->
            ctx.selectCount().from(table).fetchOne(0, Int::class.javaPrimitiveType)!!
        }

    internal val cache: MutableMap<BurstKey, T>
        get() = dp.db.getCache(table)

    override fun clearCache() {
        cache.clear()
    }

    /**
     * Create an entity from a DB record
     */
    protected abstract fun load(record: Record): T

    /**
     * Save a single entity in the DB
     */
    internal abstract fun save(ctx: DSLContext, entity: T)

    /**
     * Save multiple entities in the DB
     * TODO this is barely utilized
     */
    internal abstract fun save(ctx: DSLContext, entities: Collection<T>)

    override fun ensureAvailable(height: Int) {
        require(latestField == null || height >= dp.blockchainProcessorService.minRollbackHeight) { "Historical data as of height $height not available, set brs.trimDerivedTables=false and re-scan" }
    }

    override fun get(dbKey: BurstKey): T? {
        val key = dbKey as SqlDbKey
        if (dp.db.isInTransaction()) {
            val t = cache[key]
            if (t != null) {
                return t
            }
        }
        return dp.db.useDslContext { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(table)
            query.addConditions(key.getPrimaryKeyConditions(table))
            if (latestField != null) {
                query.addConditions(latestField.isTrue)
            }
            query.addLimit(1)

            get(ctx, query, true)
        }
    }

    override fun get(dbKey: BurstKey, height: Int): T? {
        val key = dbKey as SqlDbKey
        ensureAvailable(height)

        return dp.db.useDslContext { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(table)
            query.addConditions(key.getPrimaryKeyConditions(table))
            query.addConditions(heightField.le(height))
            if (latestField != null) {
                val innerTable = table.`as`("b")
                val innerQuery = ctx.selectQuery()
                innerQuery.addConditions(innerTable.field("height", Int::class.java).gt(height))
                innerQuery.addConditions(key.getPrimaryKeyConditions(innerTable))
                query.addConditions(latestField.isTrue.or(DSL.field(DSL.exists(innerQuery))))
            }
            query.addOrderBy(heightField.desc())
            query.addLimit(1)

            get(ctx, query, false)
        }
    }

    override fun getBy(condition: Condition): T? {
        return dp.db.useDslContext { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(table)
            query.addConditions(condition)
            if (latestField != null) {
                query.addConditions(latestField.isTrue)
            }
            query.addLimit(1)

            get(ctx, query, true)
        }
    }

    override fun getBy(condition: Condition, height: Int): T? {
        ensureAvailable(height)
        return dp.db.useDslContext { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(table)
            query.addConditions(condition)
            query.addConditions(heightField.le(height))
            if (latestField != null) {
                val innerTable = table.`as`("b")
                val innerQuery = ctx.selectQuery()
                innerQuery.addConditions(innerTable.field("height", Int::class.java).gt(height))
                dbKeyFactory.applySelfJoin(innerQuery, innerTable)
                query.addConditions(latestField.isTrue?.or(DSL.field(DSL.exists(innerQuery))))
            }
            query.addOrderBy(heightField.desc())
            query.addLimit(1)
            get(ctx, query, false)
        }
    }

    private fun get(ctx: DSLContext, query: SelectQuery<Record>, cache: Boolean): T? {
        val doCache = cache && dp.db.isInTransaction()
        val record = query.fetchOne() ?: return null
        var t: T? = null
        var dbKey: SqlDbKey? = null
        if (doCache) {
            dbKey = dbKeyFactory.newKey(record) as SqlDbKey
            t = this.cache[dbKey]
        }
        return if (t == null) {
            t = load(record)
            if (doCache && dbKey != null) {
                this.cache[dbKey] = t
            }
            t
        } else {
            t
        }
    }

    override fun getManyBy(condition: Condition, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T> {
        return dp.db.useDslContext { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(table)
            query.addConditions(condition)
            query.addOrderBy(sort)
            if (latestField != null) {
                query.addConditions(latestField.isTrue)
            }
            SqlDbUtils.applyLimits(query, from, to)
            getManyBy(ctx, query, true)
        }
    }

    override fun getManyBy(
        condition: Condition,
        height: Int,
        from: Int,
        to: Int,
        sort: Collection<SortField<*>>
    ): Collection<T> {
        ensureAvailable(height)
        return dp.db.useDslContext { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(table)
            query.addConditions(condition)
            query.addConditions(heightField.le(height))
            if (latestField != null) {
                val innerTableB = table.`as`("b")
                val innerQueryB = ctx.selectQuery()
                innerQueryB.addConditions(innerTableB.field("height", Int::class.java).gt(height))
                dbKeyFactory.applySelfJoin(innerQueryB, innerTableB)

                val innerTableC = table.`as`("c")
                val innerQueryC = ctx.selectQuery()
                innerQueryC.addConditions(
                    innerTableC.field("height", Int::class.java).le(height).and(
                        innerTableC.field("height", Int::class.java).gt(heightField)
                    )
                )
                dbKeyFactory.applySelfJoin(innerQueryC, innerTableC)
                query.addConditions(latestField.isTrue.or(DSL.field(DSL.exists(innerQueryB).and(DSL.notExists(innerQueryC)))))
            }
            query.addOrderBy(sort)

            SqlDbUtils.applyLimits(query, from, to)
            getManyBy(ctx, query, true)
        }
    }

    override fun getManyBy(ctx: DSLContext, query: SelectQuery<out Record>, cache: Boolean): Collection<T> {
        val doCache = cache && dp.db.isInTransaction()
        return query.fetchAndMap<Record, T> { record ->
            var t: T? = null
            var dbKey: SqlDbKey? = null
            if (doCache) {
                dbKey = dbKeyFactory.newKey(record) as SqlDbKey
                t = this.cache[dbKey]
            }
            if (t == null) {
                t = load(record)
                if (doCache && dbKey != null) {
                    this.cache[dbKey] = t
                }
                t
            } else t
        }
    }

    override fun getAll(from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T> {
        return dp.db.useDslContext { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(table)
            if (latestField != null) {
                query.addConditions(latestField.isTrue)
            }
            query.addOrderBy(sort)
            SqlDbUtils.applyLimits(query, from, to)
            getManyBy(ctx, query, true)
        }
    }

    override fun getAll(height: Int, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T> {
        ensureAvailable(height)
        return dp.db.useDslContext { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(table)
            query.addConditions(heightField.le(height))
            if (latestField != null) {
                val innerTableB = table.`as`("b")
                val innerQueryB = ctx.selectQuery()
                innerQueryB.addConditions(innerTableB.field("height", Int::class.java).gt(height))
                dbKeyFactory.applySelfJoin(innerQueryB, innerTableB)

                val innerTableC = table.`as`("c")
                val innerQueryC = ctx.selectQuery()
                innerQueryC.addConditions(
                    innerTableC.field("height", Int::class.java).le(height).and(
                        innerTableC.field("height", Int::class.java).gt(heightField)
                    )
                )
                dbKeyFactory.applySelfJoin(innerQueryC, innerTableC)
                query.addConditions(latestField.isTrue.or(DSL.field(DSL.exists(innerQueryB).and(DSL.notExists(innerQueryC)))))
            }
            query.addOrderBy(sort)
            query.addLimit(from, to)
            getManyBy(ctx, query, true)
        }
    }

    override fun insert(entity: T) {
        dp.db.assertInTransaction()
        val dbKey = dbKeyFactory.newKey(entity) as SqlDbKey
        val cachedT = cache[dbKey]
        if (cachedT == null) {
            cache[dbKey] = entity
        } else check(!(entity !== cachedT)) { "Trying to insert an object which has a duplicate in cache, perhaps trying to save an object that was read outside the current transaction" }
        dp.db.useDslContext { ctx ->
            if (latestField != null) {
                val query = ctx.updateQuery(table)
                query.addValue(
                    latestField,
                    false
                )
                query.addConditions(dbKey.getPrimaryKeyConditions(table))
                query.addConditions(latestField.isTrue)
                query.execute()
            }
            save(ctx, entity)
        }
    }
}
