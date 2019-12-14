package brs.db

/**
 * Like a key-value pair table, but stores lists of values.
 */
interface ValuesTable<K, V> : DerivedTable {
    /**
     * TODO
     */
    operator fun get(dbKey: BurstKey): List<V>

    /**
     * TODO
     */
    fun insert(key: K, values: List<V>)
}
