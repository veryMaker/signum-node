package brs.db

interface VersionedEntityTable<T> : DerivedTable, EntityTable<T> {
    override fun rollback(height: Int)

    fun delete(t: T): Boolean

    override fun trim(height: Int)
}
