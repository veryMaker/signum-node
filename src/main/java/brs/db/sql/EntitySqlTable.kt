package brs.db.sql

import brs.Burst
import brs.DependencyProvider
import brs.db.BurstKey
import brs.db.EntityTable
import brs.db.store.DerivedTableManager
import org.jooq.*
import org.jooq.impl.DSL
import org.jooq.impl.TableImpl

import java.util.ArrayList

abstract class EntitySqlTable<T> internal constructor(table: String, tableClass: TableImpl<*>, dbKeyFactory: BurstKey.Factory<T>, private val multiversion: Boolean, private val dp: DependencyProvider) : DerivedSqlTable(table, tableClass, dp), EntityTable<T> {
    internal val dbKeyFactory: DbKey.Factory<T>
    private val defaultSort: MutableList<SortField<*>>

    internal val heightField: Field<Int>
    internal val latestField: Field<Boolean>

    private val cache: Map<BurstKey, T>
        get() = Db.getCache(table)

    override val count: Int
        get() = Db.useDSLContext<Int> { ctx ->
            val r = ctx.selectCount().from(tableClass)
            (if (multiversion) r.where(latestField.isTrue()) else r).fetchOne(0, Int::class.javaPrimitiveType)
        }

    override val rowCount: Int
        get() = Db.useDSLContext<Int> { ctx -> ctx.selectCount().from(tableClass).fetchOne(0, Int::class.javaPrimitiveType) }

    internal constructor(table: String, tableClass: TableImpl<*>, dbKeyFactory: BurstKey.Factory<T>, dp: DependencyProvider) : this(table, tableClass, dbKeyFactory, false, dp) {}

    init {
        this.dbKeyFactory = dbKeyFactory as DbKey.Factory<T>
        this.defaultSort = mutableListOf()
        this.heightField = tableClass.field("height", Int::class.java)
        this.latestField = tableClass.field("latest", Boolean::class.java)
        if (multiversion) {
            for (column in this.dbKeyFactory.pkColumns) {
                defaultSort.add(tableClass.field(column, Long::class.java).asc())
            }
        }
        defaultSort.add(heightField.desc())
    }

    protected abstract fun load(ctx: DSLContext, rs: Record): T

    internal open fun save(ctx: DSLContext, t: T) {}

    internal open fun save(ctx: DSLContext, ts: Array<T>) {
        for (t in ts) {
            save(ctx, t)
        }
    }

    internal open fun defaultSort(): List<SortField<*>> {
        return defaultSort
    }

    override fun checkAvailable(height: Int) {
        require(!(multiversion && height < dp.blockchainProcessor.minRollbackHeight)) { "Historical data as of height $height not available, set brs.trimDerivedTables=false and re-scan" }
    }

    override fun get(nxtKey: BurstKey): T {
        val dbKey = nxtKey as DbKey
        if (Db.isInTransaction) {
            val t = cache[dbKey]
            if (t != null) {
                return t
            }
        }
        return Db.useDSLContext<T> { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(tableClass)
            query.addConditions(dbKey.getPKConditions(tableClass))
            if (multiversion) {
                query.addConditions(latestField.isTrue())
            }
            query.addLimit(1)

            get(ctx, query, true)
        }
    }

    override fun get(nxtKey: BurstKey, height: Int): T {
        val dbKey = nxtKey as DbKey
        checkAvailable(height)

        return Db.useDSLContext<T> { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(tableClass)
            query.addConditions(dbKey.getPKConditions(tableClass))
            query.addConditions(heightField.le(height))
            if (multiversion) {
                val innerTable = tableClass.`as`("b")
                val innerQuery = ctx.selectQuery()
                innerQuery.addConditions(innerTable.field("height", Int::class.java).gt(height))
                innerQuery.addConditions(dbKey.getPKConditions(innerTable))
                query.addConditions(
                        latestField.isTrue().or(
                                DSL.field(DSL.exists(innerQuery))
                        )
                )
            }
            query.addOrderBy(heightField.desc())
            query.addLimit(1)

            get(ctx, query, false)
        }
    }

    override fun getBy(condition: Condition): T {
        return Db.useDSLContext<T> { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(tableClass)
            query.addConditions(condition)
            if (multiversion) {
                query.addConditions(latestField.isTrue())
            }
            query.addLimit(1)

            get(ctx, query, true)
        }
    }

    override fun getBy(condition: Condition, height: Int): T {
        checkAvailable(height)

        return Db.useDSLContext<T> { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(tableClass)
            query.addConditions(condition)
            query.addConditions(heightField.le(height))
            if (multiversion) {
                val innerTable = tableClass.`as`("b")
                val innerQuery = ctx.selectQuery()
                innerQuery.addConditions(innerTable.field("height", Int::class.java).gt(height))
                dbKeyFactory.applySelfJoin(innerQuery, innerTable, tableClass)
                query.addConditions(
                        latestField.isTrue().or(
                                DSL.field(DSL.exists(innerQuery))
                        )
                )
            }
            query.addOrderBy(heightField.desc())
            query.addLimit(1)

            get(ctx, query, false)
        }
    }

    private operator fun get(ctx: DSLContext, query: SelectQuery<Record>, cache: Boolean): T? {
        val doCache = cache && Db.isInTransaction
        val record = query.fetchOne() ?: return null
        var t: T? = null
        var dbKey: DbKey? = null
        if (doCache) {
            dbKey = dbKeyFactory.newKey(record) as DbKey
            t = cache.get(dbKey)
        }
        if (t == null) {
            t = load(ctx, record)
            if (doCache) {
                Db.getCache<Any>(table)[dbKey] = t
            }
        }
        return t
    }

    override fun getManyBy(condition: Condition, from: Int, to: Int): Collection<T> {
        return getManyBy(condition, from, to, defaultSort())
    }

    override fun getManyBy(condition: Condition, from: Int, to: Int, sort: List<SortField<*>>): Collection<T> {
        return Db.useDSLContext<Collection<T>> { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(tableClass)
            query.addConditions(condition)
            query.addOrderBy(sort)
            if (multiversion) {
                query.addConditions(latestField.isTrue())
            }
            DbUtils.applyLimits(query, from, to)
            getManyBy(ctx, query, true)
        }
    }

    override fun getManyBy(condition: Condition, height: Int, from: Int, to: Int): Collection<T> {
        return getManyBy(condition, height, from, to, defaultSort())
    }

    override fun getManyBy(condition: Condition, height: Int, from: Int, to: Int, sort: List<SortField<*>>): Collection<T> {
        checkAvailable(height)
        return Db.useDSLContext<Collection<T>> { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(tableClass)
            query.addConditions(condition)
            query.addConditions(heightField.le(height))
            if (multiversion) {
                val innerTableB = tableClass.`as`("b")
                val innerQueryB = ctx.selectQuery()
                innerQueryB.addConditions(innerTableB.field("height", Int::class.java).gt(height))
                dbKeyFactory.applySelfJoin(innerQueryB, innerTableB, tableClass)

                val innerTableC = tableClass.`as`("c")
                val innerQueryC = ctx.selectQuery()
                innerQueryC.addConditions(
                        innerTableC.field("height", Int::class.java).le(height).and(
                                innerTableC.field("height", Int::class.java).gt(heightField)
                        )
                )
                dbKeyFactory.applySelfJoin(innerQueryC, innerTableC, tableClass)

                query.addConditions(
                        latestField.isTrue().or(
                                DSL.field(
                                        DSL.exists(innerQueryB).and(DSL.notExists(innerQueryC))
                                )
                        )
                )
            }
            query.addOrderBy(sort)

            DbUtils.applyLimits(query, from, to)
            getManyBy(ctx, query, true)
        }
    }

    override fun getManyBy(ctx: DSLContext, query: SelectQuery<out Record>, cache: Boolean): Collection<T> {
        val doCache = cache && Db.isInTransaction
        return query.fetch<T> { record ->
            var t: T? = null
            var dbKey: DbKey? = null
            if (doCache) {
                dbKey = dbKeyFactory.newKey(record) as DbKey
                t = cache.get(dbKey)
            }
            if (t == null) {
                t = load(ctx, record)
                if (doCache) {
                    Db.getCache<Any>(table)[dbKey] = t
                }
            }
            t
        }
    }

    override fun getAll(from: Int, to: Int): Collection<T> {
        return getAll(from, to, defaultSort())
    }

    override fun getAll(from: Int, to: Int, sort: List<SortField<*>>): Collection<T> {
        return Db.useDSLContext<Collection<T>> { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(tableClass)
            if (multiversion) {
                query.addConditions(latestField.isTrue())
            }
            query.addOrderBy(sort)
            DbUtils.applyLimits(query, from, to)
            getManyBy(ctx, query, true)
        }
    }

    override fun getAll(height: Int, from: Int, to: Int): Collection<T> {
        return getAll(height, from, to, defaultSort())
    }

    override fun getAll(height: Int, from: Int, to: Int, sort: List<SortField<*>>): Collection<T> {
        checkAvailable(height)
        return Db.useDSLContext<Collection<T>> { ctx ->
            val query = ctx.selectQuery()
            query.addFrom(tableClass)
            query.addConditions(heightField.le(height))
            if (multiversion) {
                val innerTableB = tableClass.`as`("b")
                val innerQueryB = ctx.selectQuery()
                innerQueryB.addConditions(innerTableB.field("height", Int::class.java).gt(height))
                dbKeyFactory.applySelfJoin(innerQueryB, innerTableB, tableClass)

                val innerTableC = tableClass.`as`("c")
                val innerQueryC = ctx.selectQuery()
                innerQueryC.addConditions(
                        innerTableC.field("height", Int::class.java).le(height).and(
                                innerTableC.field("height", Int::class.java).gt(heightField)
                        )
                )
                dbKeyFactory.applySelfJoin(innerQueryC, innerTableC, tableClass)

                query.addConditions(
                        latestField.isTrue().or(
                                DSL.field(
                                        DSL.exists(innerQueryB).and(DSL.notExists(innerQueryC))
                                )
                        )
                )
            }
            query.addOrderBy(sort)
            query.addLimit(from, to)
            getManyBy(ctx, query, true)
        }
    }

    override fun insert(t: T) {
        check(Db.isInTransaction) { "Not in transaction" }
        val dbKey = dbKeyFactory.newKey(t) as DbKey
        val cachedT = cache[dbKey]
        if (cachedT == null) {
            Db.getCache<Any>(table)[dbKey] = t
        } else check(!(t !== cachedT)) { // not a bug
            "Different instance found in Db cache, perhaps trying to save an object " + "that was read outside the current transaction"
        }
        Db.useDSLContext { ctx ->
            if (multiversion) {
                val query = ctx.updateQuery<*>(tableClass)
                query.addValue(
                        latestField,
                        false
                )
                query.addConditions(dbKey.getPKConditions(tableClass))
                query.addConditions(latestField.isTrue())
                query.execute()
            }
            save(ctx, t)
        }
    }

    override fun rollback(height: Int) {
        super.rollback(height)
        Db.getCache<Any>(table).clear()
    }

    override fun truncate() {
        super.truncate()
        Db.getCache<Any>(table).clear()
    }
}
