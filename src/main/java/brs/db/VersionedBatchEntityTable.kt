package brs.db

import org.ehcache.Cache
import org.jooq.*

interface VersionedBatchEntityTable<T> : DerivedTable, EntityTable<T> {

    override val count: Int

    override val rowCount: Int

    val batch: Map<BurstKey, T>

    val cache: Cache<BurstKey, T>?
    fun delete(t: T): Boolean

    override fun get(dbKey: BurstKey): T

    override fun insert(t: T)

    override fun finish()

    override fun get(dbKey: BurstKey, height: Int): T

    override fun getBy(condition: Condition): T

    override fun getBy(condition: Condition, height: Int): T

    override fun getManyBy(condition: Condition, from: Int, to: Int, sort: List<SortField<*>>): Collection<T>

    override fun getManyBy(condition: Condition, height: Int, from: Int, to: Int): Collection<T>

    override fun getManyBy(condition: Condition, height: Int, from: Int, to: Int, sort: List<SortField<*>>): Collection<T>

    override fun getManyBy(ctx: DSLContext, query: SelectQuery<out Record>, cache: Boolean): Collection<T>

    override fun getAll(from: Int, to: Int): Collection<T>

    override fun getAll(from: Int, to: Int, sort: List<SortField<*>>): Collection<T>

    override fun getAll(height: Int, from: Int, to: Int): Collection<T>

    override fun getAll(height: Int, from: Int, to: Int, sort: List<SortField<*>>): Collection<T>

    override fun rollback(height: Int)

    override fun truncate()

    fun flushCache()
}
