package brs.db.sql

import brs.db.DerivedTable
import brs.db.assertInTransaction
import brs.db.useDslContext
import brs.entity.DependencyProvider
import org.jooq.Field
import org.jooq.Table

internal abstract class SqlDerivedTable<T> internal constructor(
    internal val table: Table<*>,
    internal val heightField: Field<Int>,
    private val dp: DependencyProvider
) : DerivedTable {
    internal val tableName = table.name

    init {
        dp.derivedTableService.registerDerivedTable(this)
    }

    override fun rollback(height: Int) {
        dp.db.assertInTransaction()
        dp.db.useDslContext { ctx -> ctx.delete(table).where(heightField.gt(height)).execute() }
        clearCache()
    }

    override fun trim(height: Int) {
        // Nothing to trim
    }

    override fun optimize() {
        dp.db.optimizeTable(tableName)
    }

    abstract fun clearCache()
}
