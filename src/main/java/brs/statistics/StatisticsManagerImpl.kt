package brs.statistics

import brs.DependencyProvider
import brs.services.TimeService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.HashMap
import java.util.stream.Collectors

class StatisticsManagerImpl(private val dp: DependencyProvider) { // TODO interface

    private val logger = LoggerFactory.getLogger(StatisticsManagerImpl::class.java)

    private var addedBlockCount: Int = 0
    private var firstBlockAdded: Int = 0

    private val cacheStatistics = mutableMapOf<String, CacheStatisticsOverview>()

    fun foundObjectInCache(cacheName: String) {
        getCacheStatisticsOverview(cacheName).cacheHit()
    }

    fun didNotFindObjectInCache(cacheName: String) {
        getCacheStatisticsOverview(cacheName).cacheMiss()
    }

    private fun getCacheStatisticsOverview(cacheName: String): CacheStatisticsOverview {
        if (!this.cacheStatistics.containsKey(cacheName)) {
            this.cacheStatistics[cacheName] = CacheStatisticsOverview(cacheName)
        }

        return cacheStatistics[cacheName]!!
    }

    fun blockAdded() {
        if (addedBlockCount++ == 0) {
            firstBlockAdded = dp.timeService.epochTime
        } else if (addedBlockCount % 500 == 0) {
            val blocksPerSecond = 500 / (dp.timeService.epochTime - firstBlockAdded).toFloat()

            if (logger.isInfoEnabled) {
                val handleText = "handling {} blocks/s" + cacheStatistics.values.map { cacheInfo -> " " + cacheInfo.cacheInfoAndReset }.joinToString()
                logger.info(handleText, String.format("%.2f", blocksPerSecond))
            }

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
