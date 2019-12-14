package brs.db

interface DerivedTable : Table {
    /**
     * Rollback the table to its state at a certain height
     * @param height The height to rollback the table's state to
     */
    fun rollback(height: Int)

    /**
     * TODO
     */
    fun trim(height: Int)
}
