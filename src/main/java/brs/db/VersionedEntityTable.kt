package brs.db

interface VersionedEntityTable<T> : EntityTable<T> {
    override suspend fun rollback(height: Int)

    suspend fun delete(t: T): Boolean

    override suspend fun trim(height: Int)
}
