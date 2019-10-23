package brs.db

interface DerivedTable : Table {
    suspend fun rollback(height: Int)

    suspend fun truncate()

    suspend fun trim(height: Int)

    suspend fun finish()
}
