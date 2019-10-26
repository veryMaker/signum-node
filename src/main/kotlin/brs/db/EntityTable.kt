package brs.db

import org.jooq.*

interface EntityTable<T> : DerivedTable {
    /**
     * The number of entities (rows where latest is true)
     */
    val count: Int

    /**
     * The number of rows
     */
    val rowCount: Int

    /**
     * Ensure that the table is available at a particular height
     * @param height The height
     * @throws IllegalArgumentException if the table is not available at this height
     */
    fun ensureAvailable(height: Int)

    /**
     * Get an entity
     * @param dbKey The key identifying the entity
     * @return The entity, or `null` if no entity exists identified by that key
     */
    operator fun get(dbKey: BurstKey): T?

    /**
     * Get an entity at a specific height
     * @param dbKey The key identifying the entity
     * @param height The height at which to get the entity
     * @return The entity, or `null` if no entity exists identified by that key at that height
     */
    operator fun get(dbKey: BurstKey, height: Int): T?

    /**
     * TODO
     */
    fun getBy(condition: Condition): T?

    /**
     * TODO
     */
    fun getBy(condition: Condition, height: Int): T?

    /**
     * TODO
     */
    fun getManyBy(condition: Condition, from: Int, to: Int): Collection<T>

    /**
     * TODO
     */
    fun getManyBy(condition: Condition, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T>

    /**
     * TODO
     */
    fun getManyBy(condition: Condition, height: Int, from: Int, to: Int): Collection<T>

    /**
     * TODO
     */
    fun getManyBy(condition: Condition, height: Int, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T>

    /**
     * TODO
     */
    fun getManyBy(ctx: DSLContext, query: SelectQuery<out Record>, cache: Boolean): Collection<T>

    /**
     * TODO
     */
    fun getAll(from: Int, to: Int): Collection<T>

    /**
     * TODO
     */
    fun getAll(from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T>

    /**
     * TODO
     */
    fun getAll(height: Int, from: Int, to: Int): Collection<T>

    /**
     * TODO
     */
    fun getAll(height: Int, from: Int, to: Int, sort: Collection<SortField<*>>): Collection<T>

    /**
     * Insert an entity into the table
     * @param t The entity
     */
    fun insert(t: T)
}
