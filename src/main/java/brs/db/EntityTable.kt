package brs.db

import org.jooq.*

interface EntityTable<T> : DerivedTable {
    val count: Int

    val rowCount: Int
    fun checkAvailable(height: Int)

    operator fun get(dbKey: BurstKey): T?

    operator fun get(dbKey: BurstKey, height: Int): T?

    fun getBy(condition: Condition): T?

    fun getBy(condition: Condition, height: Int): T?

    fun getManyBy(condition: Condition, from: Int, to: Int): Collection<T>

    fun getManyBy(condition: Condition, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T>

    fun getManyBy(condition: Condition, height: Int, from: Int, to: Int): Collection<T>

    fun getManyBy(condition: Condition, height: Int, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T>

    fun getManyBy(ctx: DSLContext, query: SelectQuery<out Record>, cache: Boolean): Collection<T>

    fun getAll(from: Int, to: Int): Collection<T>

    fun getAll(from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T>

    fun getAll(height: Int, from: Int, to: Int): Collection<T>

    fun getAll(height: Int, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T>

    fun insert(t: T)

    override fun rollback(height: Int)

    override fun truncate()
}
