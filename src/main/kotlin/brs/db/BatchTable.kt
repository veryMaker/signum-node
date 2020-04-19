package brs.db

/**
 * A BatchTable takes stores operations (such as inserting) in memory and then writes them all to the database in one go when [flushBatch] is called.
 */
interface BatchTable : DerivedTable {
    /**
     * Writes all of the batched writes to the DB.
     */
    fun flushBatch(height: Int)
}
