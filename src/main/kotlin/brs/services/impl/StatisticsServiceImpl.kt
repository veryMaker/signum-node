package brs.services.impl

import brs.entity.DependencyProvider
import brs.services.StatisticsService
import brs.util.logging.safeInfo
import org.slf4j.LoggerFactory

class StatisticsServiceImpl(private val dp: DependencyProvider) : StatisticsService {
    private var addedBlockCount: Int = 0
    private var firstBlockAdded: Int = 0

    override fun blockAdded() {
        if (addedBlockCount++ == 0) {
            firstBlockAdded = dp.timeService.epochTime
        } else if (addedBlockCount % BLOCK_AVERAGE_PERIOD == 0) {
            logger.safeInfo { "Handling ${"%.2f".format(BLOCK_AVERAGE_PERIOD / (dp.timeService.epochTime - firstBlockAdded).toFloat())} blocks/s" }
            addedBlockCount = 0
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(StatisticsServiceImpl::class.java)

        /**
         * The number of blocks to wait for before taking a block rate average
         */
        private const val BLOCK_AVERAGE_PERIOD = 500
    }
}
