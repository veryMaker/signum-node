package brs.services.impl

import brs.db.DerivedTable
import brs.services.DerivedTableService
import brs.util.logging.safeInfo
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList

class DerivedTableServiceImpl : DerivedTableService {
    private val logger = LoggerFactory.getLogger(DerivedTableServiceImpl::class.java)

    override val derivedTables = CopyOnWriteArrayList<DerivedTable>()

    override fun registerDerivedTable(table: DerivedTable) {
        logger.safeInfo { "Registering derived table ${table.javaClass}" }
        derivedTables.add(table)
    }
}
