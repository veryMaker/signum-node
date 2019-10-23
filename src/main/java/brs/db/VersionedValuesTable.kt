package brs.db

interface VersionedValuesTable<T, V> : ValuesTable<T, V> {
    override suspend fun rollback(height: Int)

    override suspend fun trim(height: Int)
}
