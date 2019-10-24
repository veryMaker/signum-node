package brs.db

interface DerivedTable : Table {
    fun rollback(height: Int)

    fun truncate()

    fun trim(height: Int)

    fun finish()
}
