package brs.services.impl

import brs.db.DerivedTable
import brs.services.DerivedTableService
import brs.util.logging.safeTrace
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList

class DerivedTableServiceImpl : DerivedTableService {
    private val logger = LoggerFactory.getLogger(DerivedTableServiceImpl::class.java)

    override val derivedTables = CopyOnWriteArrayList<DerivedTable>()

    override fun registerDerivedTable(table: DerivedTable) {
        logger.safeTrace { "Registering derived table ${table.javaClass}" }
        derivedTables.add(table)
    }
}
