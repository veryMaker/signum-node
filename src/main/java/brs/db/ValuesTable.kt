package brs.db

interface ValuesTable<T, V> : DerivedTable {
    operator fun get(dbKey: BurstKey): List<V>

    fun insert(t: T, values: List<V>)

    override fun rollback(height: Int)

    override fun truncate()
}
