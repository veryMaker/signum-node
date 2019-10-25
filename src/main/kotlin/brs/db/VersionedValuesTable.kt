package brs.db

interface VersionedValuesTable<T, V> : ValuesTable<T, V> {
    override fun rollback(height: Int)

    override fun trim(height: Int)
}
