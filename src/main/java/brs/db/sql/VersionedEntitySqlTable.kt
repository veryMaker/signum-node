package brs.db.sql

import brs.Burst
import brs.DependencyProvider
import brs.db.BurstKey
import brs.db.VersionedEntityTable
import brs.db.store.DerivedTableManager
import org.jooq.*
import org.jooq.impl.DSL
import org.jooq.impl.TableImpl

import java.util.ArrayList

abstract class VersionedEntitySqlTable<T> internal constructor(table: String, tableClass: TableImpl<*>, dbKeyFactory: BurstKey.Factory<T>, private val dp: DependencyProvider) : EntitySqlTable<T>(table, tableClass, dbKeyFactory, true, dp), VersionedEntityTable<T> {

    override fun rollback(height: Int) {
        rollback(table, tableClass, heightField, latestField, height, dbKeyFactory)
    }

    override fun trim(height: Int) {
        trim(tableClass, heightField, height, dbKeyFactory)
    }

    override fun delete(t: T): Boolean {
        if (t == null) {
            return false
        }
        check(Db.isInTransaction) { "Not in transaction" }
        val dbKey = dbKeyFactory.newKey(t) as DbKey
        return Db.useDSLContext<Boolean> { ctx ->
            try {
                val countQuery = ctx.selectQuery()
                countQuery.addFrom(tableClass)
                countQuery.addConditions(dbKey.getPKConditions(tableClass))
                countQuery.addConditions(heightField.lt(dp.blockchain.height))
                if (ctx.fetchCount(countQuery) > 0) {
                    val updateQuery = ctx.updateQuery<*>(tableClass)
                    updateQuery.addValue(
                            latestField,
                            false
                    )
                    updateQuery.addConditions(dbKey.getPKConditions(tableClass))
                    updateQuery.addConditions(latestField.isTrue)

                    updateQuery.execute()
                    save(ctx, t)
                    // delete after the save
                    updateQuery.execute()

                    return@Db.useDSLContext true
                } else {
                    val deleteQuery = ctx.deleteQuery<*>(tableClass)
                    deleteQuery.addConditions(dbKey.getPKConditions(tableClass))
                    return@Db.useDSLContext deleteQuery . execute () > 0
                }
            } finally {
                Db.getCache<Any>(table).remove(dbKey)
            }
        }
    }

    companion object {

        internal fun rollback(table: String, tableClass: TableImpl<*>, heightField: Field<Int>, latestField: Field<Boolean>, height: Int, dbKeyFactory: DbKey.Factory<*>) {
            check(Db.isInTransaction) { "Not in transaction" }

            Db.useDSLContext { ctx ->
                // get dbKey's for entries whose stuff newer than height would be deleted, to allow fixing
                // their latest flag of the "potential" remaining newest entry
                val selectForDeleteQuery = ctx.selectQuery()
                selectForDeleteQuery.addFrom(tableClass)
                selectForDeleteQuery.addConditions(heightField.gt(height))
                for (column in dbKeyFactory.pkColumns) {
                    selectForDeleteQuery.addSelect(tableClass.field(column, Long::class.java))
                }
                selectForDeleteQuery.setDistinct(true)
                val dbKeys = selectForDeleteQuery.fetch { r -> dbKeyFactory.newKey(r) as DbKey }

                // delete all entries > height
                val deleteQuery = ctx.deleteQuery<*>(tableClass)
                deleteQuery.addConditions(heightField.gt(height))
                deleteQuery.execute()

                // update latest flags for remaining entries, if there any remaining (per deleted dbKey)
                for (dbKey in dbKeys) {
                    val selectMaxHeightQuery = ctx.selectQuery()
                    selectMaxHeightQuery.addFrom(tableClass)
                    selectMaxHeightQuery.addConditions(dbKey.getPKConditions(tableClass))
                    selectMaxHeightQuery.addSelect(DSL.max(heightField))
                    val maxHeight = selectMaxHeightQuery.fetchOne().get(DSL.max(heightField))

                    if (maxHeight != null) {
                        val setLatestQuery = ctx.updateQuery<*>(tableClass)
                        setLatestQuery.addConditions(dbKey.getPKConditions(tableClass))
                        setLatestQuery.addConditions(heightField.eq(maxHeight))
                        setLatestQuery.addValue(latestField, true)
                        setLatestQuery.execute()
                    }
                }
            }
            Db.getCache<Any>(table).clear()
        }

        internal fun trim(tableClass: TableImpl<*>, heightField: Field<Int>, height: Int, dbKeyFactory: DbKey.Factory<*>) {
            check(Db.isInTransaction) { "Not in transaction" }

            // "accounts" is just an example to make it easier to understand what the code does
            // select all accounts with multiple entries where height < trimToHeight[current height - 1440]
            Db.useDSLContext { ctx ->
                val selectMaxHeightQuery = ctx.selectQuery()
                selectMaxHeightQuery.addFrom(tableClass)
                selectMaxHeightQuery.addSelect(DSL.max(heightField).`as`("max_height"))
                for (column in dbKeyFactory.pkColumns) {
                    val pkField = tableClass.field(column, Long::class.java)
                    selectMaxHeightQuery.addSelect(pkField)
                    selectMaxHeightQuery.addGroupBy(pkField)
                }
                selectMaxHeightQuery.addConditions(heightField.lt(height))
                selectMaxHeightQuery.addHaving(DSL.countDistinct(heightField).gt(1))

                // delete all fetched accounts, except if it's height is the max height we figured out
                val deleteLowerHeightQuery = ctx.deleteQuery<*>(tableClass)
                deleteLowerHeightQuery.addConditions(heightField.lt(null!!.toInt()))
                for (column in dbKeyFactory.pkColumns) {
                    val pkField = tableClass.field(column, Long::class.java)
                    deleteLowerHeightQuery.addConditions(pkField.eq(null!!.toLong()))
                }
                val deleteBatch = ctx.batch(deleteLowerHeightQuery)

                for (record in selectMaxHeightQuery.fetch()) {
                    val dbKey = dbKeyFactory.newKey(record) as DbKey
                    val maxHeight = record.get("max_height", Int::class.java)
                    val bindValues = mutableListOf<Long>()
                    bindValues.add(maxHeight.toLong())
                    for (pkValue in dbKey.pkValues) {
                        bindValues.add(pkValue)
                    }
                    deleteBatch.bind(*bindValues.toTypedArray())
                }
                if (deleteBatch.size() > 0) {
                    deleteBatch.execute()
                }
            }
        }
    }
}
