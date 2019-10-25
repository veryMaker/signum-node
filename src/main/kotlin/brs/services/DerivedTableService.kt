package brs.services

import brs.db.DerivedTable

interface DerivedTableService {
    val derivedTables: List<DerivedTable>
    fun registerDerivedTable(table: DerivedTable)
}