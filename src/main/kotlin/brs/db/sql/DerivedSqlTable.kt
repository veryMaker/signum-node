package brs.db.sql

import brs.db.DerivedTable
import brs.db.assertInTransaction
import brs.db.useDslContext
import brs.entity.DependencyProvider
import brs.util.logging.safeTrace
import org.jooq.Field
import org.jooq.impl.TableImpl
import org.slf4j.LoggerFactory

internal abstract class DerivedSqlTable internal constructor(
    internal val table: String,
    internal val tableClass: TableImpl<*>,
    internal val heightField: Field<Int>,
    internal val latestField: Field<Boolean>?,
    private val dp: DependencyProvider
) : DerivedTable {

    init {
        logger.safeTrace { "Creating derived table for $table" }
        dp.derivedTableService.registerDerivedTable(this)
    }

    override fun rollback(height: Int) {
        dp.db.assertInTransaction()
        dp.db.useDslContext { ctx -> ctx.delete(tableClass).where(heightField.gt(height)).execute() }
        dp.db.getCache<Any>(table).clear()
    }

    override fun trim(height: Int) {
        // Nothing to trim
    }

    override fun finish() {
        // Nothing to finish
    }

    override fun optimize() {
        dp.db.optimizeTable(table)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DerivedSqlTable::class.java)
    }
}
