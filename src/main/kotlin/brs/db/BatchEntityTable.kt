package brs.db

interface BatchEntityTable<T> : EntityTable<T> {
    /**
     * TODO
     */
    fun finish()
}
