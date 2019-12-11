package brs.db.sql

import brs.db.VersionedEntityTable
import brs.db.getUsingDslContext
import brs.db.useDslContext
import brs.entity.DependencyProvider
import org.jooq.Field
import org.jooq.impl.DSL
import org.jooq.impl.TableImpl

internal abstract class VersionedEntitySqlTable<T> internal constructor(
    table: String,
    tableClass: TableImpl<*>,
    dbKeyFactory: SqlDbKey.Factory<T>,
    private val dp: DependencyProvider
) : EntitySqlTable<T>(table, tableClass, dbKeyFactory, true, dp), VersionedEntityTable<T> {
    override fun rollback(height: Int) {
        rollback(dp, table, tableClass, heightField, latestField, height, dbKeyFactory)
    }

    override fun trim(height: Int) {
        trim(dp, tableClass, heightField, height, dbKeyFactory)
    }

    override fun delete(t: T): Boolean {
        dp.db.assertInTransaction()
        val dbKey = dbKeyFactory.newKey(t) as SqlDbKey
        return dp.db.getUsingDslContext { ctx ->
            try {
                val countQuery = ctx.selectQuery()
                countQuery.addFrom(tableClass)
                countQuery.addConditions(dbKey.getPKConditions(tableClass))
                countQuery.addConditions(heightField.lt(dp.blockchainService.height))
                if (ctx.fetchCount(countQuery) > 0) {
                    val updateQuery = ctx.updateQuery(tableClass)
                    updateQuery.addValue(
                        latestField,
                        false
                    )
                    updateQuery.addConditions(dbKey.getPKConditions(tableClass))
                    updateQuery.addConditions(latestField?.isTrue)

                    updateQuery.execute()
                    save(ctx, t)
                    // delete after the save
                    updateQuery.execute()

                    return@getUsingDslContext true
                } else {
                    val deleteQuery = ctx.deleteQuery(tableClass)
                    deleteQuery.addConditions(dbKey.getPKConditions(tableClass))
                    return@getUsingDslContext deleteQuery.execute() > 0
                }
            } finally {
                dp.db.getCache<Any>(table).remove(dbKey)
            }
        }
    }

    companion object {
        internal fun rollback(
            dp: DependencyProvider,
            table: String,
            tableClass: TableImpl<*>,
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
                for (column in dbKeyFactory.pkColumns) {
                    selectForDeleteQuery.addSelect(tableClass.field(column, Long::class.java))
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
                    selectMaxHeightQuery.addConditions(dbKey.getPKConditions(tableClass))
                    selectMaxHeightQuery.addSelect(DSL.max(heightField))
                    val maxHeight = selectMaxHeightQuery.fetchOne().get(DSL.max(heightField))

                    if (maxHeight != null) {
                        val setLatestQuery = ctx.updateQuery(tableClass)
                        setLatestQuery.addConditions(dbKey.getPKConditions(tableClass))
                        setLatestQuery.addConditions(heightField.eq(maxHeight))
                        setLatestQuery.addValue(latestField, true)
                        setLatestQuery.execute()
                    }
                }
            }
            dp.db.getCache<Any>(table).clear()
        }

        internal fun trim(
            dp: DependencyProvider,
            tableClass: TableImpl<*>,
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
                for (column in dbKeyFactory.pkColumns) {
                    val pkField = tableClass.field(column, Long::class.java)
                    selectMaxHeightQuery.addSelect(pkField)
                    selectMaxHeightQuery.addGroupBy(pkField)
                }
                selectMaxHeightQuery.addConditions(heightField.lt(height))
                selectMaxHeightQuery.addHaving(DSL.countDistinct(heightField).gt(1))

                // delete all fetched accounts, except if it's height is the max height we figured out
                val deleteLowerHeightQuery = ctx.deleteQuery(tableClass)
                deleteLowerHeightQuery.addConditions(heightField.lt(null as Int?))
                for (column in dbKeyFactory.pkColumns) {
                    val pkField = tableClass.field(column, Long::class.java)
                    deleteLowerHeightQuery.addConditions(pkField.eq(null as Long?))
                }
                val deleteBatch = ctx.batch(deleteLowerHeightQuery)

                for (record in selectMaxHeightQuery.fetch()) {
                    val dbKey = dbKeyFactory.newKey(record) as SqlDbKey
                    val maxHeight = record.get("max_height", Int::class.java)
                    val bindValues = mutableListOf<Long>()
                    bindValues.add(maxHeight.toLong())
                    for (pkValue in dbKey.pkValues) {
                        bindValues.add(pkValue)
                    }
                    deleteBatch.bind(bindValues.toTypedArray())
                }
                if (deleteBatch.size() > 0) {
                    deleteBatch.execute()
                }
            }
        }
    }
}
