package brs.db

interface VersionedBatchEntityTable<T> : VersionedEntityTable<T> {
    /**
     * TODO
     */
    fun finish()
}
