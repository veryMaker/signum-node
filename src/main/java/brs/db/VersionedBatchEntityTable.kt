package brs.db

import org.ehcache.Cache
import org.jooq.*

interface VersionedBatchEntityTable<T> : DerivedTable, EntityTable<T> {

    override suspend fun getCount(): Int

    override suspend fun getRowCount(): Int

    suspend fun getBatch(): Map<BurstKey, T>

    // TODO naming this getCache causes class loader problems...
    suspend fun cache(): Cache<BurstKey, T>?

    suspend fun delete(t: T): Boolean

    override suspend fun get(dbKey: BurstKey): T?

    override suspend fun insert(t: T)

    override suspend fun finish()

    override suspend fun get(dbKey: BurstKey, height: Int): T?

    override suspend fun getBy(condition: Condition): T?

    override suspend fun getBy(condition: Condition, height: Int): T?

    override suspend fun getManyBy(condition: Condition, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T>

    override suspend fun getManyBy(condition: Condition, height: Int, from: Int, to: Int): Collection<T>

    override suspend fun getManyBy(condition: Condition, height: Int, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T>

    override suspend fun getManyBy(ctx: DSLContext, query: SelectQuery<out Record>, cache: Boolean): Collection<T>

    override suspend fun getAll(from: Int, to: Int): Collection<T>

    override suspend fun getAll(from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T>

    override suspend fun getAll(height: Int, from: Int, to: Int): Collection<T>

    override suspend fun getAll(height: Int, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T>

    override suspend fun rollback(height: Int)

    override suspend fun truncate()

    suspend fun flushCache()
}
