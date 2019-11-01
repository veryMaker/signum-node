package brs.services.impl

import brs.entity.DependencyProvider
import brs.db.BurstKey
import brs.entity.Account
import brs.entity.StatisticsCache
import brs.services.DBCacheService
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.Status
import org.ehcache.config.CacheConfiguration
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder

class DBCacheServiceImpl(private val dp: DependencyProvider) : DBCacheService {
    private val cacheManager: CacheManager
    private val caches = mutableMapOf<String, CacheConfiguration<BurstKey, *>>()

    init {
        caches["account"] = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            BurstKey::class.java,
            Account::class.java,
            ResourcePoolsBuilder.heap(8192)
        ).build()
        var cacheBuilder: CacheManagerBuilder<*> = CacheManagerBuilder.newCacheManagerBuilder()
        for ((key, value) in caches) {
            cacheBuilder = cacheBuilder.withCache(key, value)
        }
        cacheManager = cacheBuilder.build(true)
    }

    override fun close() {
        if (cacheManager.status == Status.AVAILABLE) {
            cacheManager.close()
        }
    }

    private fun <V> getEHCache(name: String, valueClass: Class<V>): Cache<BurstKey, V>? {
        return cacheManager.getCache(name, BurstKey::class.java, valueClass)
    }

    override fun <V> getCache(name: String, valueClass: Class<V>): Cache<BurstKey, V>? {
        val cache = getEHCache(name, valueClass) ?: return null
        return StatisticsCache(cache, name, dp.statisticsService)
    }

    override fun flushCache() {
        for ((key, value) in caches) {
            val cache = getEHCache(key, value.valueType)
            cache?.clear()
        }
    }
}
