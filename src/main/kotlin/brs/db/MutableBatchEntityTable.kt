package brs.db

interface MutableBatchEntityTable<T> : EntityTable<T>, BatchTable {
    /**
     * Delete an entity
     * @param t The entity
     * @return Whether the entity was deleted
     */
    fun delete(t: T): Boolean
}
