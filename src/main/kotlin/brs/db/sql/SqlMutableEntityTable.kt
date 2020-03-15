package brs.db.sql

import brs.db.BurstKey
import brs.db.MutableEntityTable
import brs.db.assertInTransaction
import brs.db.useDslContext
import brs.entity.DependencyProvider
import org.jooq.Field
import org.jooq.Table
import org.jooq.impl.DSL

internal abstract class SqlMutableEntityTable<T> internal constructor(
    table: Table<*>,
    heightField: Field<Int>,
    latestField: Field<Boolean>,
    dbKeyFactory: SqlDbKey.Factory<T>,
    private val dp: DependencyProvider
) : SqlEntityTable<T>(table, dbKeyFactory, heightField, latestField, dp), MutableEntityTable<T> {
    override fun rollback(height: Int) {
        rollback(dp, cache, table, heightField, latestField, height, dbKeyFactory)
    }

    override fun trim(height: Int) {
        trim(dp, table, heightField, height, dbKeyFactory)
    }

    override fun delete(t: T): Boolean {
        dp.db.assertInTransaction()
        val dbKey = dbKeyFactory.newKey(t) as SqlDbKey
        return dp.db.useDslContext { ctx ->
            try {
                val countQuery = ctx.selectQuery()
                countQuery.addFrom(table)
                countQuery.addConditions(dbKey.primaryKeyConditions)
                countQuery.addConditions(heightField.lt(dp.blockchainService.height))
                if (ctx.fetchCount(countQuery) > 0) {
                    val updateQuery = ctx.updateQuery(table)
                    updateQuery.addValue(
                        latestField,
                        false
                    )
                    updateQuery.addConditions(dbKey.primaryKeyConditions)
                    updateQuery.addConditions(latestField?.isTrue)

                    updateQuery.execute()
                    save(ctx, t)
                    // delete after the save
                    updateQuery.execute()

                    return@useDslContext true
                } else {
                    val deleteQuery = ctx.deleteQuery(table)
                    deleteQuery.addConditions(dbKey.primaryKeyConditions)
                    return@useDslContext deleteQuery.execute() > 0
                }
            } finally {
                cache.remove(dbKey)
            }
        }
    }

    companion object {
        internal fun <T> rollback(
            dp: DependencyProvider,
            cache: MutableMap<BurstKey, T>,
            tableClass: Table<*>,
            heightField: Field<Int>,
            latestField: Field<Boolean>?,
            height: Int,
            dbKeyFactory: SqlDbKey.Factory<*>
        ) {
            dp.db.assertInTransaction()

            dp.db.useDslContext { ctx ->
                // get dbKey's for entries whose stuff newer than height would be deleted, to allow fixing
                // their latest flag of the "potential" remaining newest entry
                val selectForDeleteQuery = ctx.selectQuery()
                selectForDeleteQuery.addFrom(tableClass)
                selectForDeleteQuery.addConditions(heightField.gt(height))
                for (primaryKeyField in dbKeyFactory.primaryKeyColumns) {
                    selectForDeleteQuery.addSelect(primaryKeyField)
                }
                selectForDeleteQuery.setDistinct(true)
                val dbKeys = selectForDeleteQuery.fetch { r -> dbKeyFactory.newKey(r) as SqlDbKey }

                // delete all entries > height
                val deleteQuery = ctx.deleteQuery(tableClass)
                deleteQuery.addConditions(heightField.gt(height))
                deleteQuery.execute()

                // update latest flags for remaining entries, if there any remaining (per deleted dbKey)
                for (dbKey in dbKeys) {
                    val selectMaxHeightQuery = ctx.selectQuery()
                    selectMaxHeightQuery.addFrom(tableClass)
                    selectMaxHeightQuery.addConditions(dbKey.primaryKeyConditions)
                    selectMaxHeightQuery.addSelect(DSL.max(heightField))
                    val maxHeight = selectMaxHeightQuery.fetchOne()?.get(DSL.max(heightField))

                    if (maxHeight != null) {
                        val setLatestQuery = ctx.updateQuery(tableClass)
                        setLatestQuery.addConditions(dbKey.primaryKeyConditions)
                        setLatestQuery.addConditions(heightField.eq(maxHeight))
                        setLatestQuery.addValue(latestField, true)
                        setLatestQuery.execute()
                    }
                }
            }
            cache.clear()
        }

        internal fun trim(
            dp: DependencyProvider,
            tableClass: Table<*>,
            heightField: Field<Int>,
            height: Int,
            dbKeyFactory: SqlDbKey.Factory<*>
        ) {
            dp.db.assertInTransaction()

            // "accounts" is just an example to make it easier to understand what the code does
            // select all accounts with multiple entries where height < trimToHeight[current height - 1440]
            dp.db.useDslContext { ctx ->
                val selectMaxHeightQuery = ctx.selectQuery()
                selectMaxHeightQuery.addFrom(tableClass)
                selectMaxHeightQuery.addSelect(DSL.max(heightField).`as`("max_height"))
                for (primaryKeyField in dbKeyFactory.primaryKeyColumns) {
                    selectMaxHeightQuery.addSelect(primaryKeyField)
                    selectMaxHeightQuery.addGroupBy(primaryKeyField)
                }
                selectMaxHeightQuery.addConditions(heightField.lt(height))
                selectMaxHeightQuery.addHaving(DSL.countDistinct(heightField).gt(1))

                // delete all fetched accounts, except if it's height is the max height we figured out
                val deleteLowerHeightQuery = ctx.deleteQuery(tableClass)
                deleteLowerHeightQuery.addConditions(heightField.lt(null as Int?))
                for (primaryKeyField in dbKeyFactory.primaryKeyColumns) {
                    deleteLowerHeightQuery.addConditions(primaryKeyField.eq(null as Long?))
                }
                val deleteBatch = ctx.batch(deleteLowerHeightQuery)

                for (record in selectMaxHeightQuery.fetch()) {
                    val dbKey = dbKeyFactory.newKey(record) as SqlDbKey
                    val maxHeight = record.get("max_height", Int::class.java)
                    val bindValues = mutableListOf<Long>()
                    bindValues.add(maxHeight.toLong())
                    bindValues.addAll(dbKey.primaryKeyValues.toTypedArray())
                    deleteBatch.bind(bindValues.toTypedArray())
                }
                if (deleteBatch.size() > 0) {
                    deleteBatch.execute()
                }
            }
        }
    }
}
