package brs.db

interface MutableEntityTable<T> : EntityTable<T> {
    /**
     * Delete an entity
     * @param t The entity
     * @return Whether the entity was deleted
     */
    fun delete(t: T): Boolean
}
