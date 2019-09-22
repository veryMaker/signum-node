package brs.db.sql

import brs.DependencyProvider
import brs.db.DerivedTable
import org.jooq.Field
import org.jooq.impl.TableImpl
import org.slf4j.LoggerFactory

abstract class DerivedSqlTable internal constructor(internal val table: String, internal val tableClass: TableImpl<*>, dp: DependencyProvider) : DerivedTable {
    internal val heightField: Field<Int>
    internal val latestField: Field<Boolean>?

    init {
        logger.trace("Creating derived table for {}", table)
        dp.derivedTableManager.registerDerivedTable(this)
        this.heightField = tableClass.field("height", Int::class.java)
        this.latestField = tableClass.field("latest", Boolean::class.java)
    }

    override fun rollback(height: Int) {
        check(Db.isInTransaction) { "Not in transaction" }
        Db.useDSLContext { ctx -> ctx.delete(tableClass).where(heightField.gt(height)).execute() }
    }

    override fun truncate() {
        check(Db.isInTransaction) { "Not in transaction" }
        Db.useDSLContext { ctx -> ctx.delete(tableClass).execute() }
    }

    override fun trim(height: Int) {
        // Nothing to trim
    }

    override fun finish() {
        // Nothing to finish
    }

    override fun optimize() {
        Db.optimizeTable(table)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DerivedSqlTable::class.java)
    }
}
