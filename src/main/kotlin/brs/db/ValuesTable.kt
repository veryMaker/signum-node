package brs.db

interface ValuesTable<T, V> : DerivedTable {
    /**
     * TODO
     */
    fun get(dbKey: BurstKey): List<V>

    /**
     * TODO
     */
    fun insert(t: T, values: List<V>)
}
