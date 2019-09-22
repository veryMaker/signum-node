package brs.db.sql

import brs.DependencyProvider
import brs.db.BurstKey
import brs.db.ValuesTable
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.jooq.impl.TableImpl

abstract class ValuesSqlTable<T, V> internal constructor(table: String, tableClass: TableImpl<*>, internal val dbKeyFactory: DbKey.Factory<T>, private val multiversion: Boolean, dp: DependencyProvider) : DerivedSqlTable(table, tableClass, dp), ValuesTable<T, V> {
    protected constructor(table: String, tableClass: TableImpl<*>, dbKeyFactory: DbKey.Factory<T>, dp: DependencyProvider) : this(table, tableClass, dbKeyFactory, false, dp) {}

    protected abstract fun load(ctx: DSLContext, record: Record): V

    protected abstract fun save(ctx: DSLContext, t: T, v: V)

    override fun get(dbKey: BurstKey): List<V> {
        return Db.useDSLContext<List<V>> { ctx ->
            val key = dbKey as DbKey
            var values: List<V>?
            if (Db.isInTransaction) {
                values = Db.getCache<List<V>>(table)[key]
                if (values != null) {
                    return@useDSLContext values
                }
            }
            values = ctx.selectFrom(tableClass)
                    .where(key.getPKConditions(tableClass))
                    .and(if (multiversion) latestField?.isTrue ?: DSL.noCondition() else DSL.noCondition())
                    .orderBy(tableClass.field("db_id").desc())
                    .fetch { record -> load(ctx, record) }
            if (Db.isInTransaction) {
                Db.getCache<Any>(table)[key] = values
            }
            values
        }
    }

    override fun insert(t: T, values: List<V>) {
        check(Db.isInTransaction) { "Not in transaction" }
        Db.useDSLContext { ctx ->
            val dbKey = dbKeyFactory.newKey(t) as DbKey
            Db.getCache<Any>(table)[dbKey] = values
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
        Db.getCache<Any>(table).clear()
    }

    override fun truncate() {
        super.truncate()
        Db.getCache<Any>(table).clear()
    }
}
