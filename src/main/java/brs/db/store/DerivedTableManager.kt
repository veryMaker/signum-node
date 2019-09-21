package brs.db.store

import brs.db.DerivedTable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList

class DerivedTableManager {

    private val logger = LoggerFactory.getLogger(DerivedTableManager::class.java)

    val derivedTables = CopyOnWriteArrayList<DerivedTable>()

    fun getDerivedTables(): List<DerivedTable> {
        return derivedTables
    }

    fun registerDerivedTable(table: DerivedTable) {
        logger.info("Registering derived table " + table.javaClass)
        derivedTables.add(table)
    }

}
