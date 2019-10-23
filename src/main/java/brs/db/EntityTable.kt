package brs.db

import org.jooq.*

interface EntityTable<T> : DerivedTable {
    suspend fun getCount(): Int

    suspend fun getRowCount(): Int

    fun checkAvailable(height: Int)

    suspend fun get(dbKey: BurstKey): T?

    suspend fun get(dbKey: BurstKey, height: Int): T?

    suspend fun getBy(condition: Condition): T?

    suspend fun getBy(condition: Condition, height: Int): T?

    suspend fun getManyBy(condition: Condition, from: Int, to: Int): Collection<T>

    suspend fun getManyBy(condition: Condition, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T>

    suspend fun getManyBy(condition: Condition, height: Int, from: Int, to: Int): Collection<T>

    suspend fun getManyBy(condition: Condition, height: Int, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T>

    suspend fun getManyBy(ctx: DSLContext, query: SelectQuery<out Record>, cache: Boolean): Collection<T>

    suspend fun getAll(from: Int, to: Int): Collection<T>

    suspend fun getAll(from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T>

    suspend fun getAll(height: Int, from: Int, to: Int): Collection<T>

    suspend fun getAll(height: Int, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T>

    suspend fun insert(t: T)

    override suspend fun rollback(height: Int)

    override suspend fun truncate()
}
