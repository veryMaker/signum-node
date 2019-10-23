package brs.db

interface ValuesTable<T, V> : DerivedTable {
    suspend fun get(dbKey: BurstKey): List<V>

    suspend fun insert(t: T, values: List<V>)

    override suspend fun rollback(height: Int)

    override suspend fun truncate()
}
