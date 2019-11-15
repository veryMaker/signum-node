package brs.services

import brs.db.DerivedTable

interface DerivedTableService {
    /**
     * TODO
     */
    val derivedTables: List<DerivedTable>

    /**
     * TODO
     */
    fun registerDerivedTable(table: DerivedTable)
}