package brs.services.impl

import brs.entity.DependencyProvider
import brs.services.StatisticsService
import brs.util.logging.safeInfo
import org.slf4j.LoggerFactory

class StatisticsServiceImpl(private val dp: DependencyProvider) : StatisticsService {

    private val logger = LoggerFactory.getLogger(StatisticsServiceImpl::class.java)

    private var addedBlockCount: Int = 0
    private var firstBlockAdded: Int = 0

    private val cacheStatistics = mutableMapOf<String, CacheStatisticsOverview>()

    override fun foundObjectInCache(cacheName: String) {
        getCacheStatisticsOverview(cacheName).cacheHit()
    }

    override fun didNotFindObjectInCache(cacheName: String) {
        getCacheStatisticsOverview(cacheName).cacheMiss()
    }

    private fun getCacheStatisticsOverview(cacheName: String): CacheStatisticsOverview {
        if (!this.cacheStatistics.containsKey(cacheName)) {
            this.cacheStatistics[cacheName] = CacheStatisticsOverview(cacheName)
        }

        return cacheStatistics[cacheName]!!
    }

    override fun blockAdded() {
        if (addedBlockCount++ == 0) {
            firstBlockAdded = dp.timeService.epochTime
        } else if (addedBlockCount % 500 == 0) {
            logger.safeInfo { "handling ${String.format("%.2f", 500 / (dp.timeService.epochTime - firstBlockAdded).toFloat())} blocks/s ${cacheStatistics.values.joinToString { cacheInfo -> " " + cacheInfo.cacheInfoAndReset }}" }
            addedBlockCount = 0
        }
    }

    private inner class CacheStatisticsOverview internal constructor(private val cacheName: String) {

        private var cacheHits: Long = 0
        private var cacheMisses: Long = 0

        private var totalCacheHits: Long = 0
        private var totalCacheMisses: Long = 0

        internal val cacheInfoAndReset: String
            get() {
                val hitRatio = if (cacheHits + cacheMisses > 0) cacheHits.toFloat() / (cacheHits + cacheMisses) else null
                val totalHitRatio = if (totalCacheHits + totalCacheMisses > 0) totalCacheHits.toFloat() / (totalCacheHits + totalCacheMisses) else null

                cacheHits = 0
                cacheMisses = 0

                return String.format("%s cache hit ratio now/total:%.2f%%/%.2f%%", cacheName, hitRatio!! * 100, totalHitRatio!! * 100)
            }

        internal fun cacheHit() {
            cacheHits++
            totalCacheHits++
        }

        internal fun cacheMiss() {
            cacheMisses++
            totalCacheMisses++
        }
    }
}
