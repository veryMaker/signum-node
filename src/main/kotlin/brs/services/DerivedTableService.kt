package brs.services

import brs.db.DerivedTable

interface DerivedTableService {
    /**
     * All registered derived tables
     */
    val derivedTables: List<DerivedTable>

    /**
     * Registers a derived table
     */
    fun registerDerivedTable(table: DerivedTable)
}