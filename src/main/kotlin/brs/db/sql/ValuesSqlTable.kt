package brs.db.sql

import brs.entity.DependencyProvider
import brs.db.BurstKey
import brs.db.ValuesTable
import brs.db.getUsingDslContext
import brs.db.useDslContext
import brs.util.db.fetchAndMap
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.jooq.impl.TableImpl

internal abstract class ValuesSqlTable<T, V> internal constructor(table: String, tableClass: TableImpl<*>, internal val dbKeyFactory: SqlDbKey.Factory<T>, private val multiversion: Boolean, private val dp: DependencyProvider) : DerivedSqlTable(table, tableClass, dp), ValuesTable<T, V> {
    protected abstract fun load(ctx: DSLContext, record: Record): V

    protected abstract fun save(ctx: DSLContext, t: T, v: V)

    override fun get(dbKey: BurstKey): List<V> {
        return dp.db.getUsingDslContext { ctx ->
            val key = dbKey as SqlDbKey
            var values: List<V>?
            if (dp.db.isInTransaction()) {
                values = dp.db.getCache<List<V>>(table)[key]
                if (values != null) {
                    return@getUsingDslContext values
                }
            }
            values = ctx.selectFrom(tableClass)
                    .where(key.getPKConditions(tableClass))
                    .and(if (multiversion) latestField?.isTrue ?: DSL.noCondition() else DSL.noCondition())
                    .orderBy(tableClass.field("db_id").desc())
                    .fetchAndMap { record -> load(ctx, record) }
            if (dp.db.isInTransaction()) {
                dp.db.getCache<Any>(table)[key] = values
            }
            values
        }
    }

    override fun insert(t: T, values: List<V>) {
        check(dp.db.isInTransaction()) { "Not in transaction" }
        dp.db.useDslContext { ctx ->
            val dbKey = dbKeyFactory.newKey(t) as SqlDbKey
            dp.db.getCache<Any>(table)[dbKey] = values
            if (multiversion) {
                ctx.update(tableClass)
                        .set(latestField, false)
                        .where(dbKey.getPKConditions(tableClass))
                        .and(latestField?.isTrue)
                        .execute()
            }
            for (v in values) {
                save(ctx, t, v)
            }
        }
    }

    override fun rollback(height: Int) {
        super.rollback(height)
        dp.db.getCache<Any>(table).clear()
    }

    override fun truncate() {
        super.truncate()
        dp.db.getCache<Any>(table).clear()
    }
}
